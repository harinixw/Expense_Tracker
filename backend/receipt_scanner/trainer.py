import os
import json
import torch
from transformers import (
    VisionEncoderDecoderModel,
    DonutProcessor,
    Seq2SeqTrainingArguments,
    Seq2SeqTrainer,
)
from datasets import load_dataset

# ==========================================
# 1. CONFIGURATION
# ==========================================
MODEL_ID = "naver-clova-ix/donut-base"
DATASET_PATH = "./dataset" # The folder containing your images and metadata.jsonl
OUTPUT_DIR = "./donut-receipt-parser"
MAX_LENGTH = 256
IMAGE_SIZE = [1280, 960] # Donut's default high-res image input size

# The exact keys you expect in your JSON output
JSON_KEYS = ["brand", "category", "date", "amount"]

# ==========================================
# 2. INITIALIZE MODEL & PROCESSOR
# ==========================================
print("Loading Base Model and Processor...")
processor = DonutProcessor.from_pretrained(MODEL_ID)
model = VisionEncoderDecoderModel.from_pretrained(MODEL_ID)

# Resize image parameters in the processor
processor.image_processor.size = IMAGE_SIZE[::-1] # Donut expects (width, height)
processor.image_processor.do_align_long_axis = False

# ==========================================
# 3. CREATE SPECIAL TOKENS
# ==========================================
# Donut learns structure via special XML-like tokens instead of standard JSON brackets.
# We must add these tokens to the tokenizer's vocabulary.
new_tokens = ["<s_receipt>", "</s_receipt>"]
for key in JSON_KEYS:
    new_tokens.append(f"<s_{key}>")
    new_tokens.append(f"</s_{key}>")

processor.tokenizer.add_special_tokens({"additional_special_tokens": new_tokens})
# New (Fixes the issue)
model.decoder.resize_token_embeddings(len(processor.tokenizer))

# Set the start token to our receipt identifier
model.config.pad_token_id = processor.tokenizer.pad_token_id
model.config.decoder_start_token_id = processor.tokenizer.convert_tokens_to_ids(["<s_receipt>"])[0]

# ==========================================
# 4. DATA PREPROCESSING
# ==========================================
def json2token(obj):
    """
    Converts a standard JSON dictionary into Donut's token sequence.
    Example Input: {"brand": "Walmart", "amount": "10.00"}
    Example Output: <s_brand>Walmart</s_brand><s_amount>10.00</s_amount>
    """
    if type(obj) == dict:
        if len(obj) == 1 and "text_sequence" in obj:
            return obj["text_sequence"]
        else:
            output = ""
            for k, v in obj.items():
                output += f"<s_{k}>" + json2token(v) + f"</s_{k}>"
            return output
    elif type(obj) == list:
        return "".join([json2token(item) for item in obj])
    else:
        return str(obj)

def preprocess_batch(batch):
    """
    Takes a batch of images and ground_truth strings, processes the images into 
    pixel_values, and converts the JSON strings into tokenized labels.
    """
    # 1. Process Images
    pixel_values = processor(
        batch["image"], random_padding=True, return_tensors="pt"
    ).pixel_values

    # 2. Process Labels (Target JSON)
    labels = []
    for target_json_str in batch["ground_truth"]:
        # Parse the string back to a dict
        target_dict = json.loads(target_json_str)
        
        # Convert to Donut token format
        target_sequence = "<s_receipt>" + json2token(target_dict) + "</s_receipt>" + processor.tokenizer.eos_token
        
        # Tokenize the sequence
        target_tokenized = processor.tokenizer(
            target_sequence,
            add_special_tokens=False,
            max_length=MAX_LENGTH,
            padding="max_length",
            truncation=True,
            return_tensors="pt",
        )["input_ids"].squeeze(0)
        
        # Replace padding token id's of the labels by -100 so it's ignored by PyTorch Loss
        target_tokenized[target_tokenized == processor.tokenizer.pad_token_id] = -100
        labels.append(target_tokenized)

    return {"pixel_values": pixel_values, "labels": torch.stack(labels)}

# ==========================================
# 5. LOAD AND PREPARE DATASET
# ==========================================
print("Loading Dataset...")
# This automatically reads the image folder and matches it with metadata.jsonl
dataset = load_dataset("imagefolder", data_dir=DATASET_PATH, split="train")

# In a real scenario, you'd split this into train/test. For now, we train on all.
# dataset = dataset.train_test_split(test_size=0.1) 
# train_dataset = dataset["train"]

print("Preprocessing data (this may take a while depending on dataset size)...")
processed_dataset = dataset.map(
    preprocess_batch,
    batched=True,
    batch_size=2, # Keep low to avoid out-of-memory errors with images
    remove_columns=dataset.column_names # Drop original image/string columns to save RAM
)

# ==========================================
# 6. TRAINING THE MODEL
# ==========================================
training_args = Seq2SeqTrainingArguments(
    output_dir=OUTPUT_DIR,
    num_train_epochs=10,             # Start with 10 epochs
    learning_rate=2e-5,              # Donut needs a small learning rate
    per_device_train_batch_size=2,   # Batch size 2 is safe for most 16GB-24GB GPUs
    weight_decay=0.01,
    logging_steps=10,
    save_total_limit=2,
    eval_strategy="no",        # Change to "epoch" if you added a test split
    save_strategy="epoch",
    predict_with_generate=True,
    fp16=True,                       # Use Mixed Precision to save memory/speed up
)

trainer = Seq2SeqTrainer(
    model=model,
    args=training_args,
    train_dataset=processed_dataset,
)

print("Starting training loop...")
trainer.train()

# ==========================================
# 7. SAVE FINAL MODEL
# ==========================================
print(f"Saving fine-tuned model to {OUTPUT_DIR}")
processor.save_pretrained(OUTPUT_DIR)
model.save_pretrained(OUTPUT_DIR)
print("Done! Your custom receipt parser is ready.")
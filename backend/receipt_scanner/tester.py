import torch
import re
from PIL import Image
from transformers import DonutProcessor, VisionEncoderDecoderModel

# 1. Configuration
# This points to the folder where your trained model was saved
MODEL_PATH = r"C:\Users\parth\Desktop\Parthyo\Expense Tracker\Expense_Tracker\backend\receipt_scanner\donut-receipt-parser" 
TEST_IMAGE_PATH = r"C:\Users\parth\Desktop\Parthyo\Expense Tracker\Expense_Tracker\backend\receipt_scanner\receipt_001.jpg" # Change this to the image you want to test

def main():
    print("Loading your trained model...")
    device = "cuda" if torch.cuda.is_available() else ("xpu" if hasattr(torch, "xpu") and torch.xpu.is_available() else "cpu")
    print(f"Running inference on: {device.upper()}")

    # 2. Load the Processor and Model
    processor = DonutProcessor.from_pretrained(MODEL_PATH)
    model = VisionEncoderDecoderModel.from_pretrained(MODEL_PATH)
    model.to(device)
    model.eval() # Set model to evaluation mode

    # 3. Load the image
    print(f"Reading image: {TEST_IMAGE_PATH}")
    try:
        image = Image.open(TEST_IMAGE_PATH).convert("RGB")
    except FileNotFoundError:
        print(f"Error: Could not find '{TEST_IMAGE_PATH}'. Please put a test image in the folder.")
        return

    # Process image into tensors
    pixel_values = processor(image, return_tensors="pt").pixel_values.to(device)

    # 4. Prepare the starting prompt (<s_receipt>)
    task_prompt = "<s_receipt>"
    decoder_input_ids = processor.tokenizer(
        task_prompt, 
        add_special_tokens=False, 
        return_tensors="pt"
    ).input_ids.to(device)

    print("Analyzing receipt...")
    
    # 5. Generate output
    with torch.no_grad():
        outputs = model.generate(
            pixel_values,
            decoder_input_ids=decoder_input_ids,
            max_length=model.decoder.config.max_position_embeddings,
            pad_token_id=processor.tokenizer.pad_token_id,
            eos_token_id=processor.tokenizer.eos_token_id,
            use_cache=True,
            num_beams=1,
            bad_words_ids=[[processor.tokenizer.unk_token_id]],
            return_dict_in_generate=True,
        )

    # 6. Clean up the output
    sequence = processor.batch_decode(outputs.sequences)[0]
    sequence = sequence.replace(processor.tokenizer.eos_token, "").replace(processor.tokenizer.pad_token, "")
    sequence = re.sub(r"<.*?>", "", sequence, count=1).strip()  
    
    # Convert tokens back to standard JSON
    extracted_data = processor.token2json(sequence)
    
    print("\n=== EXTRACTION RESULTS ===")
    print(extracted_data)

if __name__ == "__main__":
    main()
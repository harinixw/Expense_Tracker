import os
import json
import re
import torch
from PIL import Image
from transformers import DonutProcessor, VisionEncoderDecoderModel

# ==========================================
# 1. CONFIGURATION
# ==========================================
MODEL_PATH = r"C:\Users\parth\Desktop\Parthyo\Expense Tracker\Expense_Tracker\backend\receipt_scanner\donut-receipt-parser" # Your downloaded trained model

def main():
    print("Loading your trained model (this may take a moment)...")
    # Check for Nvidia (cuda), Intel Arc (xpu), or fallback to CPU
    device = "cuda" if torch.cuda.is_available() else ("xpu" if hasattr(torch, "xpu") and torch.xpu.is_available() else "cpu")
    print(f"Running inference on: {device.upper()}")

    try:
        processor = DonutProcessor.from_pretrained(MODEL_PATH)
        model = VisionEncoderDecoderModel.from_pretrained(MODEL_PATH)
        model.to(device)
        model.eval() # Set model to evaluation mode
        print("Model loaded successfully!")
    except Exception as e:
        print(f"Error loading model from {MODEL_PATH}. Did you download it from Colab? Error: {e}")
        return

    print("\n==========================================")
    print("=== INTERACTIVE RECEIPT SCANNER ===")
    print("==========================================")
    print("Type 'quit' or 'exit' at any time to stop.\n")

    # Interactive loop to feed manual images
    while True:
        # Prompt user for an image path
        user_input = input("Enter the path to a receipt image: ").strip()
        
        # Strip quotes just in case the user dragged-and-dropped the file into the terminal
        image_path = user_input.strip("\"'")
        
        if image_path.lower() in ['quit', 'exit']:
            print("Exiting scanner. Goodbye!")
            break
            
        if not os.path.exists(image_path):
            print(f"Error: Could not find '{image_path}'. Please check the path and try again.\n")
            continue

        try:
            # 2. Process Image
            image = Image.open(image_path).convert("RGB")
            pixel_values = processor(image, return_tensors="pt").pixel_values.to(device)
            
            task_prompt = "<s_receipt>"
            decoder_input_ids = processor.tokenizer(
                task_prompt, 
                add_special_tokens=False, 
                return_tensors="pt"
            ).input_ids.to(device)

            print("Analyzing...")

            # 3. Generate Prediction
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

            # 4. Clean and Decode Output
            sequence = processor.batch_decode(outputs.sequences)[0]
            sequence = sequence.replace(processor.tokenizer.eos_token, "").replace(processor.tokenizer.pad_token, "")
            raw_sequence = re.sub(r"<.*?>", "", sequence, count=1).strip()  
            
            predicted_data = processor.token2json(raw_sequence)
            
            # Unwrap nested 'receipt' key if it exists
            if not isinstance(predicted_data, dict):
                predicted_data = {}
            elif "receipt" in predicted_data and isinstance(predicted_data["receipt"], dict):
                predicted_data = predicted_data["receipt"]

            # 5. Print Results
            print("\n--- Extraction Results ---")
            # Using json.dumps to print a beautifully formatted JSON output
            print(json.dumps(predicted_data, indent=4))
            print("--------------------------\n")
            
        except Exception as e:
            print(f"An error occurred while processing this image: {e}\n")

if __name__ == "__main__":
    main()
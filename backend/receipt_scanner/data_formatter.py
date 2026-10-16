import os
import json
import shutil
import time
import google.generativeai as genai

# ==========================================
# 1. PATH & API CONFIGURATION
# ==========================================
# IMPORTANT: Change these to where you extracted the Kaggle SROIE dataset!
SROIE_TRAIN_DIR = r"C:\Users\parth\Desktop\Parthyo\Expense Tracker\Expense_Tracker\backend\receipt_scanner\sroie\test"
SROIE_LABELS_DIR = os.path.join(SROIE_TRAIN_DIR, "entities") 
OUTPUT_DATASET_DIR = r"C:\Users\parth\Desktop\Parthyo\Expense Tracker\Expense_Tracker\backend\receipt_scanner\dataset2"
SROIE_IMAGES_DIR = os.path.join(SROIE_TRAIN_DIR, "img")
CACHE_FILE_PATH = r"C:\Users\parth\Desktop\Parthyo\Expense Tracker\Expense_Tracker\backend\receipt_scanner\category_cache.json"
if not os.path.exists(SROIE_IMAGES_DIR):
    SROIE_IMAGES_DIR = os.path.join(SROIE_TRAIN_DIR, "images")

# Get a free Gemini API Key from: https://aistudio.google.com/app/apikey
GEMINI_API_KEY = "AIzaSyBsVrnvMQqXIeDUog7uATWTocRXBz0Rc1Y"

# Initialize the LLM (Using Flash as it is the fastest and cheapest/free)
genai.configure(api_key=GEMINI_API_KEY)
llm_model = genai.GenerativeModel("gemini-2.5-flash")


# ==========================================
# 2. LLM CATEGORIZATION ENGINE
# ==========================================
ALLOWED_CATEGORIES = ["Groceries", "Dining", "Retail", "Electronics", "Transport", "General"]

# Persistent cache to avoid calling the LLM for the same brand twice!
category_cache = {}

# Load existing cache if it exists
if os.path.exists(CACHE_FILE_PATH):
    try:
        with open(CACHE_FILE_PATH, "r", encoding="utf-8") as f:
            category_cache = json.load(f)
        print(f"Loaded {len(category_cache)} cached categories from disk.")
    except Exception as e:
        print(f"Failed to load cache file: {e}")

def save_cache():
    """Saves the current dictionary to a JSON file so progress isn't lost."""
    with open(CACHE_FILE_PATH, "w", encoding="utf-8") as f:
        json.dump(category_cache, f, indent=4)

def process_brand_batches(brands_list):
    """Sends brands to Gemini in chunks of 50 to avoid rate limits and save time."""
    if not llm_model:
        print("No API key provided. Falling back to 'General' for all.")
        return

    batch_size = 50
    for i in range(0, len(brands_list), batch_size):
        batch = brands_list[i:i + batch_size]
        print(f"\nProcessing Batch {i // batch_size + 1} of {(len(brands_list) - 1) // batch_size + 1} ({len(batch)} brands)...")

        prompt = f"""
        Categorize the following list of store/brand names into exactly ONE of these categories: {', '.join(ALLOWED_CATEGORIES)}.
        Return ONLY a valid JSON object where the keys are the exact brand names provided, and the values are the categories.
        
        Brands to categorize:
        {json.dumps(batch)}
        """

        retries = 3
        success = False
        
        while retries > 0 and not success:
            try:
                # Force Gemini to return structured JSON
                response = llm_model.generate_content(
                    prompt,
                    generation_config=genai.GenerationConfig(response_mime_type="application/json")
                )
                
                # Parse the JSON response
                batch_results = json.loads(response.text)
                
                # Update our cache with the results
                for brand_name, predicted_category in batch_results.items():
                    if predicted_category not in ALLOWED_CATEGORIES:
                        predicted_category = "General"
                    category_cache[brand_name.strip().upper()] = predicted_category

                save_cache()
                success = True
                print(f"  [Success] Categorized {len(batch_results)} brands in this batch.")
                
                # Sleep to avoid hitting the 15 RPM free tier limit
                print("  Waiting 15 seconds to respect rate limits...")
                time.sleep(15) 
                
            except Exception as e:
                retries -= 1
                print(f"\a  [Error] API Batch failed: {e}. Retries left: {retries}")
                time.sleep(50) # Wait before retrying

        if not success:
            print(f"  \a[Failed] Could not categorize this batch after retries. Falling back to 'General'.")
            for b in batch:
                category_cache[b.strip().upper()] = "General"
            save_cache()

def main():
    if not os.path.exists(OUTPUT_DATASET_DIR):
        os.makedirs(OUTPUT_DATASET_DIR)

    print(f"Step 1: Scanning SROIE labels in {SROIE_LABELS_DIR} for unique brands...")
    
    # ---------------------------------------------------------
    # PASS 1: Collect all unique brands from the dataset
    # ---------------------------------------------------------
    all_unique_brands = set()
    label_files = [f for f in os.listdir(SROIE_LABELS_DIR) if f.endswith(".txt")]
    
    for filename in label_files:
        label_path = os.path.join(SROIE_LABELS_DIR, filename)
        with open(label_path, "r", encoding="utf-8") as f:
            try:
                sroie_data = json.load(f)
                brand = sroie_data.get("company", "").strip()
                if brand:
                    all_unique_brands.add(brand)
            except json.JSONDecodeError:
                continue

    # Filter down to only the brands we haven't cached yet
    uncategorized_brands = [b for b in all_unique_brands if b.upper() not in category_cache]
    
    # ---------------------------------------------------------
    # PASS 2: Categorize the missing brands using LLM Batching
    # ---------------------------------------------------------
    if uncategorized_brands:
        print(f"Found {len(uncategorized_brands)} new unique brands to categorize.")
        process_brand_batches(uncategorized_brands)
    else:
        print("All brands are already cached! Proceeding to dataset generation.")

    # ---------------------------------------------------------
    # PASS 3: Generate the final dataset for PyTorch Donut
    # ---------------------------------------------------------
    print("\nStep 2: Building final dataset...")
    metadata_path = os.path.join(OUTPUT_DATASET_DIR, "metadata.jsonl")
    valid_count = 0
    missing_count = 0

    with open(metadata_path, "w", encoding="utf-8") as jsonl_file:
        for filename in label_files:
            base_name = filename.replace(".txt", "")
            image_filename = base_name + ".jpg"
            image_source_path = os.path.join(SROIE_IMAGES_DIR, image_filename)
            
            if not os.path.exists(image_source_path):
                missing_count += 1
                continue

            label_path = os.path.join(SROIE_LABELS_DIR, filename)
            with open(label_path, "r", encoding="utf-8") as f:
                try:
                    sroie_data = json.load(f)
                except json.JSONDecodeError:
                    continue

            brand = sroie_data.get("company", "").strip()
            date = sroie_data.get("date", "")
            amount = sroie_data.get("total", "")
            
            # Fetch category dynamically from our fully populated cache
            brand_key = brand.upper()
            category = category_cache.get(brand_key, "General")

            target_json = {
                "brand": brand,
                "category": category,
                "date": date,
                "amount": amount
            }

            # Copy image to our new dataset folder
            shutil.copy2(image_source_path, os.path.join(OUTPUT_DATASET_DIR, image_filename))

            # Write formatted line
            json_line = {
                "file_name": image_filename,
                "ground_truth": json.dumps(target_json, ensure_ascii=False)
            }
            jsonl_file.write(json.dumps(json_line, ensure_ascii=False) + "\n")
            
            valid_count += 1

    print("\n=== SROIE FORMATTING COMPLETE ===")
    print(f"Successfully formatted {valid_count} receipts.")
    if missing_count > 0:
        print(f"Skipped {missing_count} label files because the matching image was missing.")
    print(f"\nYour intelligent dataset is ready in '{OUTPUT_DATASET_DIR}/'.")
    print("You can now run 'trainer.py' to begin training your Donut model!")

if __name__ == "__main__":
    main()
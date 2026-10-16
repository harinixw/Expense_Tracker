import os
import json
import random
from PIL import Image, ImageDraw, ImageFont

# ==========================================
# 1. SETUP & CONFIGURATION
# ==========================================
DATASET_DIR = r"C:\Users\parth\Desktop\Parthyo\Expense Tracker\Expense_Tracker\backend\receipt_scanner\dataset"
NUM_IMAGES = 50

# Ensure the dataset directory exists
os.makedirs(DATASET_DIR, exist_ok=True)

# Mock data to generate fake receipts
BRANDS = [
    ("Walmart", "Groceries"), 
    ("Target", "Groceries"), 
    ("Best Buy", "Electronics"), 
    ("Starbucks", "Dining"), 
    ("Shell", "Transport"),
    ("Home Depot", "Hardware")
]

ITEMS = ["Milk", "Bread", "Coffee", "USB Cable", "Gasoline", "Hammer", "Eggs", "Laptop"]

# ==========================================
# 2. IMAGE GENERATION FUNCTION
# ==========================================
def create_fake_receipt(filename, brand, category, date, amount):
    # Create a blank white image (resembling a receipt)
    img = Image.new('RGB', (400, 600), color='white')
    draw = ImageDraw.Draw(img)
    
    # Optional: Load a default font. 
    # If this fails, PIL uses a basic fallback bitmap font.
    try:
        font_large = ImageFont.truetype("arial.ttf", 36)
        font_medium = ImageFont.truetype("arial.ttf", 24)
        font_small = ImageFont.truetype("arial.ttf", 16)
    except IOError:
        font_large = font_medium = font_small = ImageFont.load_default()

    # Draw Text on the Image to look like a receipt
    draw.text((120, 30), brand, fill="black", font=font_large)
    draw.text((150, 80), "Store Receipt", fill="black", font=font_small)
    draw.text((20, 130), "-" * 50, fill="black", font=font_medium)
    
    # Random Line Items
    y_offset = 160
    for _ in range(random.randint(2, 5)):
        item = random.choice(ITEMS)
        price = round(random.uniform(2.0, 15.0), 2)
        draw.text((20, y_offset), f"{item}", fill="black", font=font_medium)
        draw.text((300, y_offset), f"${price}", fill="black", font=font_medium)
        y_offset += 40

    draw.text((20, y_offset), "-" * 50, fill="black", font=font_medium)
    
    # Draw Target Fields (Date and Total)
    y_offset += 40
    draw.text((20, y_offset), f"Date: {date}", fill="black", font=font_medium)
    y_offset += 50
    draw.text((20, y_offset), "TOTAL:", fill="black", font=font_large)
    draw.text((250, y_offset), f"${amount}", fill="black", font=font_large)
    
    # Draw a barcode-like bottom
    draw.text((80, y_offset + 80), "||| || ||| | || |||| ||", fill="black", font=font_large)

    # Save to the dataset folder
    filepath = os.path.join(DATASET_DIR, filename)
    img.save(filepath)

# ==========================================
# 3. DATASET GENERATION LOOP
# ==========================================
print(f"Generating {NUM_IMAGES} synthetic receipts...")

metadata_entries = []

for i in range(NUM_IMAGES):
    # 1. Randomize Data
    brand, category = random.choice(BRANDS)
    year = random.randint(2022, 2024)
    month = random.randint(1, 12)
    day = random.randint(1, 28)
    date_str = f"{year}-{month:02d}-{day:02d}"
    amount_str = f"{round(random.uniform(5.00, 150.00), 2):.2f}"
    
    filename = f"receipt_{i:03d}.jpg"
    
    # 2. Draw the image
    create_fake_receipt(filename, brand, category, date_str, amount_str)
    
    # 3. Create the ground_truth JSON payload exactly as Donut expects
    ground_truth_dict = {
        "brand": brand,
        "category": category,
        "date": date_str,
        "amount": amount_str
    }
    
    # Append to metadata list
    metadata_entries.append({
        "file_name": filename,
        "ground_truth": json.dumps(ground_truth_dict) # Must be a stringified JSON object
    })

# ==========================================
# 4. WRITE METADATA.JSONL
# ==========================================
metadata_path = os.path.join(DATASET_DIR, "metadata.jsonl")
with open(metadata_path, "w", encoding="utf-8") as f:
    for entry in metadata_entries:
        f.write(json.dumps(entry) + "\n")

print(f"Done! Created '{DATASET_DIR}' folder with {NUM_IMAGES} images and metadata.jsonl.")
print("You can now run your train_donut.py script!")
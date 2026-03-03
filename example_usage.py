#!/usr/bin/env python3
"""
Example script showing how to use the frame_resize module programmatically
"""

from frame_resize import resize_for_frame_tv
from pathlib import Path

# Example 1: Simple resize with default settings
# resize_for_frame_tv("path/to/image.jpg")

# Example 2: Resize with custom output path
# resize_for_frame_tv("path/to/image.jpg", "output/my_image.jpg")

# Example 3: Resize with white background
# resize_for_frame_tv("path/to/image.jpg", fill_color="white")

# Example 4: Batch process multiple images
# image_folder = Path("images/")
# for image_file in image_folder.glob("*.jpg"):
#     output_file = f"output/{image_file.stem}_frame.jpg"
#     resize_for_frame_tv(str(image_file), output_file)
#     print(f"Processed: {image_file.name}")

if __name__ == "__main__":
    print("This is an example script showing how to use frame_resize module.")
    print("Uncomment the examples above to use them.")
    print("\nFor CLI usage, run: python frame_resize.py --help")

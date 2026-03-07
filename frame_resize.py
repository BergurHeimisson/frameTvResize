#!/usr/bin/env python3
"""
Samsung Pro Frame TV Image Resizer CLI
Converts images to 3840x2160 (16:9) resolution
"""

import argparse
import sys
import subprocess
import tempfile
from pathlib import Path
from PIL import Image, ImageEnhance, ImageDraw


def resize_for_frame_tv(input_path: str, output_path: str = None, fill_color: str = "black",
                         brightness: float = 1.0, border_color: str = None,
                         border_thickness: int = 0) -> str:
    """
    Resize image to Samsung Pro Frame TV dimensions (3840x2160).

    Args:
        input_path: Path to the input image
        output_path: Path to save the resized image (optional, auto-generated if not provided)
        fill_color: Background fill color if scaling with letterbox/pillarbox (default: "black")
        brightness: Brightness/exposure factor; 1.0=original, <1=darker, >1=brighter (default: 1.0)
        border_color: Color of frame border drawn around the image ("black" or "white", default: None)
        border_thickness: Thickness of the border in pixels (default: 0)

    Returns:
        Path to the output file

    Raises:
        FileNotFoundError: If input file doesn't exist
        ValueError: If file is not a valid image
    """
    # Constants for Samsung Pro Frame TV
    TARGET_WIDTH = 3840
    TARGET_HEIGHT = 2160
    TARGET_RATIO = TARGET_WIDTH / TARGET_HEIGHT  # 16:9
    
    # Validate input file
    input_file = Path(input_path)
    if not input_file.exists():
        raise FileNotFoundError(f"Input file not found: {input_path}")
    
    if not input_file.is_file():
        raise ValueError(f"Path is not a file: {input_path}")
    
    # Try to open the image
    try:
        # Check if it's a raw image format
        raw_formats = {'.cr3', '.cr2', '.nef', '.arw', '.raf', '.rwl', '.dng', '.orf', '.rw2'}
        if input_file.suffix.lower() in raw_formats:
            # Try to use exiftool to extract preview
            try:
                # Create a temporary file for the preview
                with tempfile.NamedTemporaryFile(suffix='.jpg', delete=False) as tmp_file:
                    tmp_path = tmp_file.name
                
                # Use exiftool to extract the preview image
                result = subprocess.run(
                    ['exiftool', '-PreviewImage', '-b', str(input_file)],
                    capture_output=True,
                    timeout=10
                )
                
                if result.returncode == 0 and result.stdout:
                    # Write preview to temp file
                    with open(tmp_path, 'wb') as f:
                        f.write(result.stdout)
                    
                    # Load the preview image
                    img = Image.open(tmp_path)
                    img.load()
                    print(f"ℹ  RAW file converted to preview JPEG using exiftool")
                else:
                    raise ValueError("No embedded preview found in RAW file")
            except FileNotFoundError:
                raise ValueError(
                    f"exiftool not found. Please install it to process RAW files:\n"
                    f"  macOS: brew install exiftool\n"
                    f"  Linux: sudo apt-get install exiftool\n"
                    f"  Windows: choco install exiftool\n\n"
                    f"Or manually convert your {input_file.suffix} file first."
                )
            except subprocess.TimeoutExpired:
                raise ValueError("exiftool timed out processing the file")
            except Exception as e:
                raise ValueError(
                    f"Error processing RAW file: {e}\n\n"
                    f"Please convert your {input_file.name} to JPG using:\n"
                    f"  • Canon R5/R6 (CR3): Canon Digital Photo Professional or Adobe Lightroom\n"
                    f"  • Or free tools: Darktable, RawTherapee, or Adobe DNG Converter"
                )
        else:
            # Load standard image format using PIL
            img = Image.open(input_file)
            img.load()  # Verify it's a valid image
    except Exception as e:
        raise ValueError(f"Cannot open image file: {e}")
    
    # Get original image dimensions
    orig_width, orig_height = img.size
    orig_ratio = orig_width / orig_height
    
    # Determine resize strategy
    if orig_ratio > TARGET_RATIO:
        # Image is wider than target ratio - fit to height
        new_height = TARGET_HEIGHT
        new_width = int(TARGET_HEIGHT * orig_ratio)
    else:
        # Image is taller than target ratio - fit to width
        new_width = TARGET_WIDTH
        new_height = int(TARGET_WIDTH / orig_ratio)
    
    # Resize image with high quality
    resized_img = img.resize((new_width, new_height), Image.Resampling.LANCZOS)

    # Apply brightness/exposure adjustment
    if brightness != 1.0:
        enhancer = ImageEnhance.Brightness(resized_img)
        resized_img = enhancer.enhance(max(0.0, brightness))

    # Create final image with padding if needed
    final_img = Image.new("RGB", (TARGET_WIDTH, TARGET_HEIGHT), fill_color)
    
    # Calculate position to center the resized image
    x_offset = (TARGET_WIDTH - new_width) // 2
    y_offset = (TARGET_HEIGHT - new_height) // 2
    
    # Paste the resized image onto the final image
    final_img.paste(resized_img, (x_offset, y_offset))

    # Draw frame border around the image if specified
    if border_color and border_thickness > 0:
        draw = ImageDraw.Draw(final_img)
        draw.rectangle(
            [x_offset, y_offset, x_offset + new_width - 1, y_offset + new_height - 1],
            outline=border_color,
            width=border_thickness
        )

    # Determine output path
    if output_path is None:
        output_path = input_file.stem + "_frame_3840x2160.jpg"
    
    output_file = Path(output_path)
    
    # Save the final image
    final_img.save(output_file, quality=95, optimize=False)
    
    print(f"✓ Image successfully resized!")
    print(f"  Original: {orig_width}x{orig_height}")
    print(f"  Output: {TARGET_WIDTH}x{TARGET_HEIGHT}")
    if brightness != 1.0:
        print(f"  Brightness: {brightness:.2f}x")
    if border_color and border_thickness > 0:
        print(f"  Border: {border_color} ({border_thickness}px)")
    print(f"  Saved to: {output_file.absolute()}")
    
    return str(output_file)


def main():
    """Main entry point for the CLI."""
    parser = argparse.ArgumentParser(
        description="Resize images to Samsung Pro Frame TV dimensions (3840x2160, 16:9)",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python frame_resize.py image.jpg
  python frame_resize.py image.jpg -o output.jpg
  python frame_resize.py image.jpg --fill white
        """
    )
    
    parser.add_argument(
        "image",
        help="Path to the input image"
    )
    
    parser.add_argument(
        "-o", "--output",
        help="Output file path (default: original_filename_frame_3840x2160.jpg)",
        default=None
    )
    
    parser.add_argument(
        "--fill",
        choices=["black", "white", "gray"],
        default="black",
        help="Background fill color for padding (default: black)"
    )

    parser.add_argument(
        "--brightness",
        type=float,
        default=1.0,
        metavar="FACTOR",
        help="Brightness/exposure factor: 1.0=original, 0.5=darker, 2.0=brighter (default: 1.0)"
    )

    parser.add_argument(
        "--border-color",
        choices=["black", "white"],
        default=None,
        help="Frame border color drawn around the image edge"
    )

    parser.add_argument(
        "--border-thickness",
        type=int,
        default=0,
        metavar="PX",
        help="Border thickness in pixels (default: 0)"
    )

    args = parser.parse_args()

    try:
        resize_for_frame_tv(args.image, args.output, args.fill,
                            args.brightness, args.border_color, args.border_thickness)
    except (FileNotFoundError, ValueError) as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Unexpected error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()

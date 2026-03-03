# Samsung Pro Frame TV Image Resizer

A Python CLI tool that resizes and converts images to the correct dimensions for Samsung Pro Frame TV (3840x2160, 16:9 aspect ratio).

## Features

- Automatically resizes images to 3840x2160 pixels
- **Automatic RAW support** (CR3, CR2, NEF, ARW, RAF, etc.) via exiftool
- Maintains original aspect ratio using letterbox/pillarbox if needed
- High-quality image resampling using Lanczos algorithm
- Supports multiple image formats (JPG, PNG, GIF, BMP, and others)
- Customizable background fill color
- Simple command-line interface

## Installation

1. Clone or download this project
2. Install dependencies:

```bash
pip install -r requirements.txt
```

## Usage

### Basic usage (with default settings)

```bash
python frame_resize.py image.jpg
```

This will create a resized image named `image_frame_3840x2160.jpg` in the current directory.

### Specify output file

```bash
python frame_resize.py image.jpg -o my_output.jpg
```

### Customize background fill color

```bash
python frame_resize.py image.jpg --fill white
python frame_resize.py image.jpg --fill gray
```

Available colors: `black` (default), `white`, `gray`

## How it Works

1. **Load Image**: Opens the input image file
2. **Calculate Scaling**: Determines the best way to fit the image to 3840x2160 while maintaining aspect ratio
3. **Resize**: Uses high-quality LANCZOS resampling to resize the image
4. **Pad**: Creates a new 3840x2160 image and centers the resized image with padding on the sides/top/bottom
5. **Save**: Saves the final image as JPG with quality 95

## Output Specifications

- **Resolution**: 3840 × 2160 pixels
- **Aspect Ratio**: 16:9
- **Format**: JPG
- **Quality**: 95

## Examples

```bash
# Resize a JPEG photo
python frame_resize.py landscape.jpg

# Resize a Canon CR3 raw file (automatic preview extraction)
python frame_resize.py photo.cr3

# Resize a Nikon NEF raw file with white background
python frame_resize.py photo.nef -o formatted.jpg --fill white

# Resize a Sony ARW raw file and save with custom name
python frame_resize.py photo.arw -o frame_ready.jpg
```

## Requirements

- Python 3.7+
- Pillow library (image processing)
- exiftool (for automatic RAW image extraction)

### Optional: Installing exiftool
To process RAW files automatically:
```bash
# macOS
brew install exiftool

# Linux (Debian/Ubuntu)
sudo apt-get install exiftool

# Windows (with Chocolatey)
choco install exiftool
```

**Without exiftool**: You can still use standard formats (JPG, PNG, etc.) - RAW files will show an error with conversion instructions.

## License

MIT

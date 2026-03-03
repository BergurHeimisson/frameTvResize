# Architecture - Samsung Pro Frame TV Image Resizer

## Overview

This project is a Python CLI application that resizes and converts images to the correct dimensions and aspect ratio for Samsung Pro Frame TV displays (3840×2160 @ 16:9).

## Project Structure

```
frameTvResize/
├── frame_resize.py          # Main CLI application
├── example_usage.py         # Programmatic usage examples
├── setup.py                 # Package installation configuration
├── requirements.txt         # Project dependencies
├── README.md               # User documentation
├── ARCHITECTURE.md         # This file
└── .gitignore             # Git ignore rules
```

## Component Architecture

### 1. **frame_resize.py** (Main Module)

The core module containing two main components:

#### **`resize_for_frame_tv()` Function**
- **Purpose**: Core image processing logic
- **Input**: Image file path (standard or RAW format), optional output path, optional fill color
- **Process**:
  1. Validate input file exists and is a valid file
  2. Detect file format by extension
  3. For RAW formats (CR3, CR2, NEF, ARW, etc.):
     - Call exiftool to extract embedded preview image
     - Load the preview JPEG into memory
     - Proceed with standard resize workflow
  4. For standard formats:
     - Load image using PIL directly
  5. Calculate scaling factor based on aspect ratio
  6. Resize image using LANCZOS resampling (high quality)
  7. Create 3840×2160 canvas with background color
  8. Center and paste resized image onto canvas
  9. Save output as JPG (quality: 95)
- **Output**: Path to saved output file
- **Error Handling**: 
  - If exiftool not found → instructions for installation
  - If no preview embedded → helpful error message
  - Supports graceful fallback workflow

#### **`main()` Function**
- **Purpose**: CLI entry point
- **Responsibilities**:
  - Parse command-line arguments
  - Validate user input
  - Call `resize_for_frame_tv()` with parsed arguments
  - Handle and display errors gracefully

## Data Flow

```
User Input (CLI Arguments)
    ↓
Argument Parser (ArgumentParser)
    ↓
Input Validation (File existence, format)
    ↓
Image Loading (PIL.Image.open)
    ↓
Aspect Ratio Calculation
    ↓
Image Resizing (LANCZOS resampling)
    ↓
Canvas Creation (3840×2160)
    ↓
Image Centering & Pasting
    ↓
Output Saving (JPG, quality 95)
    ↓
Success Message + File Path
```

## Scaling Logic

The application uses intelligent scaling to preserve aspect ratio:

1. **Calculate original aspect ratio**: `orig_ratio = width / height`
2. **Compare with target ratio (16:9 = 1.777...)**:
   - If `orig_ratio > target_ratio`: Image is wider → fit to height
   - If `orig_ratio ≤ target_ratio`: Image is taller → fit to width
3. **Result**: Resized image maintains original proportions with padding on sides/top/bottom

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **LANCZOS resampling** | Provides highest quality image resizing |
| **Letterbox/Pillarbox approach** | Preserves original image without distortion |
| **JPG format output** | Widely supported, good compression for display |
| **Quality 95** | High quality output with reasonable file size |
| **RGB color space** | Compatible with all display types |
| **Centered image** | Optimal viewing on frame TV |

## Dependencies

```
Pillow >= 10.0.0
  └── PIL.Image - Image loading, resizing, and saving

exiftool >= 0.4.0 (Optional but Recommended)
  └── System tool for RAW image preview extraction
  └── Handles CR3, CR2, NEF, ARW, RAF, RW2, ORF, DNG formats
  └── Installation: brew install exiftool (macOS), apt-get install exiftool (Linux)
```

### RAW Image Format Support via exiftool
The application uses exiftool for automatic RAW processing:

**How It Works**:
1. User provides CR3 or other RAW file
2. App detects RAW format and calls exiftool
3. exiftool extracts embedded preview image (high-quality JPEG)
4. Preview is processed like any standard image
5. Final output is saved as JPG

**Advantages**:
- **No heavy libraries**: exiftool is lightweight command-line tool
- **Fast**: Preview extraction is nearly instantaneous
- **Reliable**: Works with embedded previews from most modern cameras
- **Cross-platform**: macOS, Linux, Windows support
- **Graceful fallback**: If exiftool missing, provides clear installation instructions

**Fallback Workflow** (if exiftool not installed):
- App suggests: `brew install exiftool` (macOS) or `apt-get install exiftool` (Linux)
- Or user can manually convert CR3→JPG using Adobe Lightroom, Canon DPP, etc.

## Configuration

### Target Specifications
- **Resolution**: 3840 × 2160 pixels
- **Aspect Ratio**: 16:9
- **Output Format**: JPG
- **Quality Level**: 95/100
- **Supported Fills**: black, white, gray

### Supported Image Formats

**Directly Supported** (via PIL):
- JPG, JPEG, PNG, GIF, BMP, TIFF, WebP

**Automatically Converted via exiftool** (RAW Formats - embedded preview):
- CR3 (Canon EOS R5/R6 and newer) ✓
- CR2 (Canon EOS DSLR) ✓
- NEF (Nikon) ✓
- ARW (Sony Alpha) ✓
- RAF (Fujifilm) ✓
- RW2 (Panasonic Lumix) ✓
- ORF (Olympus) ✓
- DNG (Adobe Digital Negative) ✓

*Requires exiftool to be installed. Install with: `brew install exiftool` (macOS) or `apt-get install exiftool` (Linux)*

## Usage Interfaces

### Command Line Interface (CLI)
```bash
python frame_resize.py IMAGE_PATH [-o OUTPUT_PATH] [--fill {black,white,gray}]
```

### Python Module Interface
```python
from frame_resize import resize_for_frame_tv

resize_for_frame_tv(
    input_path="image.jpg",
    output_path="output.jpg",  # Optional
    fill_color="black"          # Optional
)
```

## Error Handling

| Error Type | Handling |
|-----------|----------|
| File not found | `FileNotFoundError` with clear message |
| Invalid image format | `ValueError` during PIL loading |
| Invalid file path | `ValueError` when path is not a file |
| Unexpected errors | Generic exception caught and logged |

All errors exit with code 1 and display error message to stderr.

## Performance Characteristics

- **Memory**: Loads images into memory (suitable for files < 500MB)
- **CPU**: LANCZOS resampling is CPU-intensive; processing time scales with image size
- **Typical Processing**: ~1-3 seconds for high-resolution images on modern hardware

## Extensibility Points

1. **Image Format Support**: Already supports any PIL-compatible format
2. **Color Options**: Can easily add more fill colors or gradients
3. **Output Formats**: Can extend to support different output formats
4. **Batch Processing**: Module design allows easy batch processing wrapper
5. **Web Interface**: API can be wrapped for web service

## Installation & Distribution

### Local Development
```bash
pip install -r requirements.txt
python frame_resize.py --help
```

### Package Installation
```bash
pip install -e .
frame-resize image.jpg  # After setup.py installation
```

## Testing Considerations

Key areas to test:
- Various image formats (JPG, PNG, GIF, BMP)
- Different aspect ratios (landscape, portrait, square)
- Edge cases (very small images, very large images)
- All fill color options
- Invalid input handling
- Output file quality and dimensions

## Future Enhancements

- [ ] Batch processing mode
- [ ] Advanced color profiles (ICC support)
- [ ] Multiple output quality presets
- [ ] Image optimization options
- [ ] Web API interface
- [ ] Drag-and-drop GUI
- [ ] Progress indication for batch operations
- [ ] Metadata preservation

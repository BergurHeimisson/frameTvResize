# Frame TV Resizer GUI

A user-friendly Java Swing graphical interface for the Samsung Pro Frame TV Image Resizer. This GUI provides an intuitive way to select, configure, and resize images for your Samsung Frame TV (3840×2160 @ 16:9).

## Features

- 🖼️ **Visual File Selection** - Browse and select images via native file dialog
- 📸 **RAW Support** - Automatic CR3 and other RAW format handling via exiftool
- 🎨 **Color Options** - Choose background fill color (black, white, gray)
- 📊 **Live Logging** - Real-time conversion progress and output display
- ⚡ **Non-Blocking UI** - Background processing keeps interface responsive
- 🔧 **Auto Output Paths** - Intelligent output filename generation
- 📝 **Status Updates** - Clear feedback on conversion status and errors

## Requirements

- **Java**: JRE 11 or higher
- **Maven**: 3.6 or higher (for building)
- **pyenv**: Installed and configured
- **Python**: 3.7+ (via pyenv)
- **Pillow**: Installed in Python environment
- **exiftool**: For RAW image support (optional but recommended)

### Setup Prerequisites

```bash
# Install Java (if not already installed)
brew install openjdk

# Install Maven
brew install maven

# Verify installations
java -version
mvn -version
```

## Installation & Setup

### 1. Build the Application

Navigate to the frameTvResizeUI directory and build:

```bash
cd frameTvResizeUI
./build.sh
```

Or manually with Maven:

```bash
mvn clean package
```

The build creates `/target/FrameTvResizer.jar`

### 2. Run the Application

#### Option A: Using the run script
```bash
./run.sh
```

#### Option B: Direct Java command
```bash
java -jar target/FrameTvResizer.jar
```

## Usage

### Basic Workflow

1. **Select Image**
   - Click "Select Image" button
   - Browse to your JPG, PNG, or RAW file
   - Image path appears in the input field
   - Output filename is auto-generated

2. **Configure Options** (Optional)
   - Change background color if desired
   - Modify output path if needed

3. **Resize**
   - Click "Resize Image" button
   - Watch progress in the log area
   - Conversion completes and displays results

### Supported Formats

**Standard Formats** (Direct PNG/JPEG):
- JPG / JPEG
- PNG
- GIF
- BMP
- TIFF

**RAW Formats** (Converted via exiftool):
- CR3 (Canon EOS R5/R6)
- CR2 (Canon EOS DSLR)
- NEF (Nikon)
- ARW (Sony Alpha)
- RAF (Fujifilm)
- DNG (Adobe Digital Negative)

## Project Structure

```
frameTvResizeUI/
├── pom.xml                          # Maven configuration
├── build.sh                         # Build script
├── run.sh                           # Run script
├── README.md                        # This file
├── ARCHITECTURE.md                  # Technical documentation
└── src/main/java/com/bergur/frametv/
    └── FrameTvResizerUI.java        # Main GUI application
```

## How It Works

```
┌─────────────────┐
│  Frame TV GUI   │
└────────┬────────┘
         │
         │ User selects image
         ↓
┌─────────────────────────┐
│ File Selection Dialog   │
└────────┬────────────────┘
         │
         │ Build command
         ↓
┌──────────────────────────────────┐
│ ProcessBuilder with pyenv/Python │
└────────┬─────────────────────────┘
         │
         │ Execute in background
         ↓
┌────────────────────────────────────┐
│ Python CLI (frame_resize.py)       │
│ - Load image                       │
│ - Extract RAW preview (if needed)  │
│ - Resize to 3840×2160            │
│ - Save as JPG                      │
└────────┬───────────────────────────┘
         │
         │ Capture output
         ↓
┌────────────────────────────────────┐
│ Display Results in GUI              │
│ - Log messages                      │
│ - Status updates                    │
│ - Success/error messages           │
└─────────────────────────────────────┘
```

## Troubleshooting

### Application Won't Start
```bash
# Check Java version (must be 11+)
java -version

# Check if JAR exists
ls target/FrameTvResizer.jar

# Try rebuilding
mvn clean package
```

### "Python not found" Error
```bash
# Ensure pyenv is installed and initialized
which pyenv
pyenv versions

# Set active Python version
pyenv shell 3.9.2
```

### RAW Files Not Converting
```bash
# Install exiftool
brew install exiftool

# Verify it's accessible
which exiftool
```

### Build Fails
```bash
# Clear Maven cache
mvn clean

# Check Maven version (3.6+)
mvn -version

# Rebuild
mvn clean package
```

## Output Examples

### Successful Conversion
```
Selected: IMG_6279.CR3
ℹ  RAW file converted to preview JPEG using exiftool
✓ Image successfully resized!
  Original: 1620x1080
  Output: 3840x2160
  Saved to: /Users/bergurheimisson/ai_code/frameTvResizeUI/IMG_6279_frame_3840x2160.jpg
```

### Configuration
- **Input**: `IMG_6279.CR3` (Canon RAW)
- **Output**: `IMG_6279_frame_3840x2160.jpg`
- **Dimensions**: 1620×1080 → 3840×2160
- **Quality**: JPG 95%
- **Fill Color**: Black (default)

## Integration with Python CLI

This GUI is a wrapper around the Python CLI tool located in `../frameTvResize/`. You can also use the Python CLI directly from terminal:

```bash
cd ../frameTvResize
python frame_resize.py image.jpg
python frame_resize.py image.cr3 -o output.jpg --fill white
```

## Development

### Project Layout
- **Java Source**: `src/main/java/com/bergur/frametv/`
- **Target**: `target/` (created during build)
- **Build Config**: `pom.xml`

### Building with Debug Output
```bash
mvn clean package -X
```

### Running from IDE
In IntelliJ IDEA or Eclipse:
1. Open project
2. Set up Maven
3. Run `FrameTvResizerUI.main()` method

## Architecture Details

See [ARCHITECTURE.md](ARCHITECTURE.md) for:
- Component breakdown
- Data flow diagrams
- Threading model
- UI layout hierarchy
- Design decisions
- Future enhancements

## License

Same as the Python frameTvResize project (MIT)

## Author

Created to complement the Samsung Pro Frame TV Image Resizer Python CLI

## Related Projects

- **Frame TV Resizer (Python CLI)**: `../frameTvResize/`
  - Command-line tool
  - Python-based image processing
  - Pillow + exiftool integration

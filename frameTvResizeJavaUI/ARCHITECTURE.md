# Architecture - Frame TV Resizer UI

## Overview

This project is a Java Swing-based GUI application that serves as a front-end for the Python-based Samsung Pro Frame TV Image Resizer CLI tool. It provides a user-friendly interface to resize images to 3840×2160 (16:9) resolution for Samsung Frame TV displays.

## Project Structure

```
frameTvResizeUI/
├── pom.xml                      # Maven build configuration
├── build.sh                     # Build script
├── run.sh                       # Run script
├── README.md                    # User documentation
├── ARCHITECTURE.md              # This file
└── src/
    └── main/
        └── java/
            └── com/bergur/frametv/
                └── FrameTvResizerUI.java    # Main GUI application
```

## Component Architecture

### Single-File Architecture

The application uses a monolithic design with one main class that handles all GUI functionality:

#### **FrameTvResizerUI.java** (Main Application)

**Class**: `FrameTvResizerUI extends JFrame`

**Responsibilities**:
1. **UI Component Initialization** - Creates all Swing components
2. **Layout Management** - Arranges components in the window
3. **User Interaction Handling** - Processes button clicks and user input
4. **Image Selection** - File chooser dialog for selecting images
5. **Process Management** - Executes the Python CLI tool
6. **Logging & Status Updates** - Displays conversion progress and results
7. **Background Processing** - Uses SwingWorker for non-blocking operations

**Key Methods**:

| Method | Purpose |
|--------|---------|
| `FrameTvResizerUI()` | Constructor - initializes the window and components |
| `initializeComponents()` | Creates all Swing UI elements |
| `setupLayout()` | Arranges components using BoxLayout |
| `selectImage()` | Opens file chooser dialog |
| `resizeImage()` | Executes Python script in background thread |
| `clearFields()` | Resets all input fields |
| `appendLog()` | Adds message to log area |
| `getPythonScriptPath()` | Resolves path to Python script |
| `main()` | Application entry point |

## Data Flow

```
User Action (Select Image)
    ↓
JFileChooser Dialog
    ↓
User Selects Image File
    ↓
inputPathField Updated
    ↓
Output Path Auto-Generated
    ↓
User Clicks "Resize Image"
    ↓
SwingWorker Thread Started
    ↓
ProcessBuilder Creates Python Process
    ↓
Python CLI Executed (pyenv exec python)
    ↓
Output Captured & Logged
    ↓
UI Updated with Results
    ↓
User Can Download Result
```

## UI Layout Hierarchy

```
JFrame (FrameTvResizerUI)
├── JPanel mainPanel (BoxLayout.Y_AXIS)
│   ├── JLabel titleLabel ("Samsung Frame TV Image Resizer")
│   ├── JLabel subtitleLabel ("Resize images to 3840×2160 (16:9)")
│   ├── JPanel inputPanel (titled "Select Input Image")
│   │   └── JPanel inputRow (BorderLayout)
│   │       ├── JTextField inputPathField (CENTER)
│   │       └── JButton selectButton (EAST)
│   ├── JPanel outputPanel (titled "Output File (Optional)")
│   │   └── JTextField outputPathField
│   ├── JPanel optionsPanel (titled "Options")
│   │   └── JComboBox fillColorCombo (black, white, gray)
│   ├── JPanel statusPanel
│   │   ├── JLabel statusLabel
│   │   └── JProgressBar progressBar
│   ├── JPanel buttonPanel (FlowLayout)
│   │   ├── JButton resizeButton
│   │   └── JButton clearButton
│   └── JScrollPane scrollPane
│       └── JTextArea logArea
```

## Configuration & Constants

| Constant | Value | Purpose |
|----------|-------|---------|
| `PYTHON_SCRIPT_PATH` | `../frameTvResize/frame_resize.py` | Relative path to Python CLI script |
| Window Size | 800×700 | Default window dimensions |
| Window Title | "Samsung Pro Frame TV Image Resizer" | Application title |
| Font (Labels) | Arial, 12pt | UI label font |
| Font (Input) | Monaco, 12pt | Text field font |
| Font (Log) | Monaco, 10pt | Log area font |

## Process Management

### Python Script Execution

The application uses Java's `ProcessBuilder` to execute the Python CLI:

```java
ProcessBuilder pb = new ProcessBuilder(
    "pyenv", "exec", "python",     // Use pyenv-managed Python
    pythonScript,                   // Path to Python script
    imagePath,                      // Input image path
    "-o", outputPath,              // Output file path
    "--fill", fillColor            // Background color (optional)
);
pb.directory(pythonScriptDir);     // Set working directory
Process process = pb.start();       // Start process
```

**Key Features**:
- Uses `pyenv exec` to ensure correct Python environment
- Captures stdout/stderr in real-time
- Executes in background thread to prevent UI blocking
- Displays output in log area as it arrives
- Updates status label with progress

## Threading Model

```
UI Thread (EDT)
    ↓
resizeImage() triggered by button click
    ↓
SwingWorker.execute()
    ↓
Background Thread
    ├── Executes Python process
    ├── Reads process output
    ├── Calls publish() to sync with EDT
    └── Updates UI in done()
    ↓
EDT receives updates
    ├── Processes chunks via process()
    ├── Updates log area
    └── Updates status label
```

## Supported Image Formats

**Standard Formats** (Passed to Python CLI):
- JPG, JPEG, PNG, GIF, BMP, TIFF

**RAW Formats** (Auto-converted by Python CLI via exiftool):
- CR3 (Canon EOS R5/R6)
- CR2 (Canon EOS DSLR)
- NEF (Nikon)
- ARW (Sony Alpha)
- RAF (Fujifilm)
- DNG (Adobe Digital Negative)

## Dependencies

### Build-Time
- Java 11+
- Maven 3.6+

### Runtime
- Java Runtime Environment (JRE) 11+
- pyenv (for Python environment management)
- Python 3.7+ (via pyenv)
- Pillow library (Python)
- exiftool (for RAW support)

## Error Handling

| Scenario | Handling |
|----------|----------|
| No image selected | Show warning dialog |
| Script file not found | Runtime error with message |
| Process fails | Display error in log area |
| Subprocess timeout | Caught exception, user notified |
| Invalid image format | Caught by Python script, error displayed |

## Build & Deployment

### Build Process

```
mvn clean package
    ↓
Compiles Java source to target/classes
    ↓
Creates JAR file
    ↓
Shades dependencies (if any)
    ↓
Produces: target/FrameTvResizer.jar
```

### Running the Application

```bash
java -jar target/FrameTvResizer.jar
```

Or use the provided script:
```bash
./run.sh
```

## Performance Characteristics

- **Memory**: ~150MB base + image size during processing
- **Startup Time**: ~2-3 seconds (Java startup overhead)
- **Image Resize**: Depends on image size (delegated to Python)
- **UI Responsiveness**: Maintained via background threading

## Future Enhancement Points

1. **Image Preview** - Display thumbnail of selected image
2. **Batch Processing** - Support multiple image selection
3. **Settings Storage** - Remember last used paths and options
4. **Progress Indication** - Real percentage progress instead of indeterminate
5. **Output Folder Opening** - Open output folder after conversion
6. **Drag & Drop** - Support dragging images onto window
7. **Custom Colors** - Allow RGB color picker instead of predefined
8. **Integration** - Standalone JAR/App packaging for distribution
9. **Dark Mode** - Support system dark theme
10. **Internationalization** - Support multiple languages

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Single File Class** | Simplicity for small GUI application |
| **Java Swing** | No external GUI framework dependency, native Java |
| **SwingWorker** | Non-blocking UI + easy thread-safe updates |
| **ProcessBuilder** | Direct subprocess execution with I/O capture |
| **Relative Path to Python Script** | Keeps UI and CLI tightly coupled in project structure |
| **Maven Build** | Standard Java build tool, easy dependency management |
| **BoxLayout** | Responsive layout that scales with window |

## Integration with Python CLI

The Java GUI is designed to be a thin wrapper around the Python CLI:

```
Java GUI
    ↓
Collects user input
    ↓
Validates input
    ↓
Constructs command line
    ↓
Executes: pyenv exec python ../frameTvResize/frame_resize.py
    ↓
Displays output
    ↓
Shows results
```

The Python CLI remains independent and can be used from terminal directly.

## Testing Scenarios

1. **Image Selection** - Select JPG, PNG, CR3, etc.
2. **Output Path** - Test with custom path and auto-generated
3. **Color Options** - Test black, white, gray backgrounds
4. **Large Images** - Test with high-resolution images
5. **RAW Files** - Test CR3 files with embedded previews
6. **Error Cases** - Invalid paths, missing files, processing failures

## Maintenance Notes

- Keep Python script path relative for portability
- Test after Python CLI updates
- Monitor Java version compatibility
- Update Maven plugins periodically
- Document any UI layout changes

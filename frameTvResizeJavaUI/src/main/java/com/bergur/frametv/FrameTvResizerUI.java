package com.bergur.frametv;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Samsung Pro Frame TV Image Resizer - GUI Front-end
 * Provides a user-friendly interface to resize images for Samsung Frame TV (3840x2160)
 */
public class FrameTvResizerUI extends JFrame {
    private JTextField inputPathField;
    private JTextField outputPathField;
    private JComboBox<String> fillColorCombo;
    private JLabel statusLabel;
    private JButton selectButton;
    private JButton resizeButton;
    private JButton clearButton;
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private JProgressBar progressBar;
    
    private File selectedImage;
    private static final String PYTHON_SCRIPT_PATH = "../frameTvResize/frame_resize.py";
    
    public FrameTvResizerUI() {
        setTitle("Samsung Pro Frame TV Image Resizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        setIconImage(createAppIcon());
        
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        // Input file selection
        inputPathField = new JTextField();
        inputPathField.setEditable(false);
        inputPathField.setBackground(Color.WHITE);
        inputPathField.setFont(new Font("Monaco", Font.PLAIN, 12));
        
        selectButton = new JButton("Select Image");
        selectButton.setFont(new Font("Arial", Font.BOLD, 12));
        selectButton.addActionListener(e -> selectImage());
        
        // Output file path
        outputPathField = new JTextField();
        outputPathField.setFont(new Font("Monaco", Font.PLAIN, 12));
        outputPathField.setToolTipText("Leave empty for auto-generated filename");
        
        // Fill color selection
        fillColorCombo = new JComboBox<>(new String[]{"black", "white", "gray"});
        fillColorCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        fillColorCombo.setSelectedItem("black");
        
        // Status and progress
        statusLabel = new JLabel("Ready to resize images");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(0, 120, 0));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        
        // Action buttons
        resizeButton = new JButton("Resize Image");
        resizeButton.setFont(new Font("Arial", Font.BOLD, 13));
        resizeButton.setBackground(new Color(0, 120, 200));
        resizeButton.setForeground(Color.WHITE);
        resizeButton.addActionListener(e -> resizeImage());
        resizeButton.setEnabled(false);
        
        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 12));
        clearButton.addActionListener(e -> clearFields());
        
        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monaco", Font.PLAIN, 10));
        logArea.setBackground(new Color(240, 240, 240));
        logArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Conversion Log"));
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Samsung Frame TV Image Resizer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        JLabel subtitleLabel = new JLabel("Resize images to 3840×2160 (16:9)");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitleLabel.setForeground(Color.GRAY);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Input section
        JPanel inputPanel = createLabeledPanel("Select Input Image");
        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.add(inputPathField, BorderLayout.CENTER);
        inputRow.add(selectButton, BorderLayout.EAST);
        inputPanel.add(inputRow);
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Output section
        JPanel outputPanel = createLabeledPanel("Output File (Optional)");
        outputPanel.add(outputPathField);
        mainPanel.add(outputPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Options section
        JPanel optionsPanel = createLabeledPanel("Options");
        JPanel optionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        optionsRow.add(new JLabel("Background Color:"));
        optionsRow.add(fillColorCombo);
        optionsPanel.add(optionsRow);
        mainPanel.add(optionsPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Progress and status
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(progressBar);
        mainPanel.add(statusPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(resizeButton);
        buttonPanel.add(clearButton);
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Log area
        mainPanel.add(scrollPane);
        
        // Set main content pane
        setContentPane(mainPanel);
    }
    
    private JPanel createLabeledPanel(String label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(label));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        return panel;
    }
    
    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        // Add image file filters
        String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "tiff", "cr3", "cr2", "nef", "arw", "raf", "dng"};
        for (String ext : imageExtensions) {
            fileChooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(ext.toUpperCase() + " files", ext)
            );
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImage = fileChooser.getSelectedFile();
            inputPathField.setText(selectedImage.getAbsolutePath());
            resizeButton.setEnabled(true);
            
            // Auto-generate output path
            String outputName = selectedImage.getName().replaceFirst("[.][^.]+$", "") + "_frame_3840x2160.jpg";
            outputPathField.setText(new File(selectedImage.getParent(), outputName).getAbsolutePath());
            
            appendLog("Selected: " + selectedImage.getName());
        }
    }
    
    private void resizeImage() {
        if (selectedImage == null) {
            JOptionPane.showMessageDialog(this, "Please select an image first", "No Image Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        resizeButton.setEnabled(false);
        selectButton.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("Processing...");
        statusLabel.setForeground(Color.ORANGE);
        
        // Run in background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Starting conversion...");
                    
                    // Build command
                    String pythonScript = getPythonScriptPath();
                    String outputPath = outputPathField.getText().isEmpty() ? null : outputPathField.getText();
                    String fillColor = (String) fillColorCombo.getSelectedItem();
                    
                    ProcessBuilder pb = new ProcessBuilder(
                        "pyenv", "exec", "python", pythonScript,
                        selectedImage.getAbsolutePath()
                    );
                    
                    // Add output path if specified
                    if (outputPath != null) {
                        pb.command().add("-o");
                        pb.command().add(outputPath);
                    }
                    
                    // Add fill color if not default
                    if (!"black".equals(fillColor)) {
                        pb.command().add("--fill");
                        pb.command().add(fillColor);
                    }
                    
                    // Redirect to parent directory of script
                    pb.directory(new File(getPythonScriptDir()));
                    pb.redirectErrorStream(true);
                    
                    Process process = pb.start();
                    
                    // Read output
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        publish(line);
                    }
                    
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        publish("✓ Conversion completed successfully!");
                    } else {
                        publish("✗ Conversion failed with exit code: " + exitCode);
                    }
                    
                } catch (Exception e) {
                    publish("Error: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    appendLog(chunk);
                }
            }
            
            @Override
            protected void done() {
                progressBar.setVisible(false);
                resizeButton.setEnabled(true);
                selectButton.setEnabled(true);
                statusLabel.setText("Conversion complete");
                statusLabel.setForeground(new Color(0, 120, 0));
            }
        };
        
        worker.execute();
    }
    
    private void clearFields() {
        selectedImage = null;
        inputPathField.setText("");
        outputPathField.setText("");
        fillColorCombo.setSelectedItem("black");
        logArea.setText("");
        statusLabel.setText("Ready to resize images");
        statusLabel.setForeground(new Color(0, 120, 0));
        resizeButton.setEnabled(false);
    }
    
    private void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private String getPythonScriptPath() {
        // Get path relative to the UI project
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path scriptPath = currentDir.resolve(PYTHON_SCRIPT_PATH).toAbsolutePath();
        return scriptPath.toString();
    }
    
    private String getPythonScriptDir() {
        Path scriptPath = Paths.get(getPythonScriptPath());
        return scriptPath.getParent().toString();
    }
    
    private Image createAppIcon() {
        // Create a simple icon (you can replace with a real icon file)
        return new ImageIcon(new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB)).getImage();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrameTvResizerUI frame = new FrameTvResizerUI();
            frame.setVisible(true);
        });
    }
}

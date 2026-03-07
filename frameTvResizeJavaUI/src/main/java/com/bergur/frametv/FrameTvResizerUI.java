package com.bergur.frametv;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.internet.MimeBodyPart;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.LookAndFeel;

/**
 * Samsung Pro Frame TV Image Resizer - GUI Front-end
 * Provides a user-friendly interface to resize images for Samsung Frame TV (3840x2160)
 */
public class FrameTvResizerUI extends JFrame {
    private JTextField inputPathField;
    private JTextField outputPathField;
    private JTextField recipientEmailField;
    private JComboBox<String> fillColorCombo;
    private JLabel statusLabel;
    private JButton selectButton;
    private JButton resizeButton;
    private JButton clearButton;
    private JButton gmailSettingsButton;
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private JProgressBar progressBar;
    private JSlider brightnessSlider;
    private JLabel brightnessValueLabel;
    private JComboBox<String> borderColorCombo;
    private JSpinner borderThicknessSpinner;

    private File selectedImage;
    private volatile File lastOutputFile;
    private volatile boolean lastConversionSuccess;
    private final Preferences prefs = Preferences.userNodeForPackage(FrameTvResizerUI.class);

    private static final String PYTHON_SCRIPT_PATH = "../frame_resize.py";
    private static final String PREF_GMAIL_ADDRESS = "gmail_address";
    private static final String PREF_GMAIL_APP_PASSWORD = "gmail_app_password";
    private static final String PREF_RECIPIENT_EMAIL = "recipient_email";
    private static final String PREF_LAST_DIRECTORY = "last_directory";
    private static final String PREF_THEME = "theme";
    private static final String DEFAULT_THEME = "com.formdev.flatlaf.FlatDarculaLaf";

    private static final Map<String, String> THEMES = new LinkedHashMap<>();
    static {
        THEMES.put("Flat Light",      "com.formdev.flatlaf.FlatLightLaf");
        THEMES.put("Flat Dark",       "com.formdev.flatlaf.FlatDarkLaf");
        THEMES.put("IntelliJ",        "com.formdev.flatlaf.FlatIntelliJLaf");
        THEMES.put("Darcula",         "com.formdev.flatlaf.FlatDarculaLaf");
        THEMES.put("Arc Dark",        "com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme");
        THEMES.put("Dracula",         "com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme");
        THEMES.put("Gruvbox Dark",    "com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkMediumIJTheme");
        THEMES.put("Monokai Pro",     "com.formdev.flatlaf.intellijthemes.FlatMonokaiProIJTheme");
        THEMES.put("Moonlight",       "com.formdev.flatlaf.intellijthemes.FlatMoonlightIJTheme");
        THEMES.put("Nord",            "com.formdev.flatlaf.intellijthemes.FlatNordIJTheme");
        THEMES.put("One Dark",        "com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme");
        THEMES.put("Solarized Dark",  "com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme");
        THEMES.put("Solarized Light", "com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme");
        THEMES.put("Xcode Dark",      "com.formdev.flatlaf.intellijthemes.FlatXcodeDarkIJTheme");
    }

    public FrameTvResizerUI() {
        setTitle("Samsung Pro Frame TV Image Resizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 920);
        setLocationRelativeTo(null);
        setResizable(true);
        setIconImage(createAppIcon());

        initializeComponents();
        setupLayout();
        setJMenuBar(createMenuBar());
    }

    private void initializeComponents() {
        inputPathField = new JTextField();
        inputPathField.setEditable(false);
        inputPathField.setBackground(Color.WHITE);
        inputPathField.setFont(new Font("Monaco", Font.PLAIN, 12));

        selectButton = new JButton("Select Image");
        selectButton.setFont(new Font("Arial", Font.BOLD, 12));
        selectButton.addActionListener(e -> selectImage());

        outputPathField = new JTextField();
        outputPathField.setFont(new Font("Monaco", Font.PLAIN, 12));
        outputPathField.setToolTipText("Leave empty for auto-generated filename");

        recipientEmailField = new JTextField(prefs.get(PREF_RECIPIENT_EMAIL, ""));
        recipientEmailField.setFont(new Font("Monaco", Font.PLAIN, 12));
        recipientEmailField.setToolTipText("Email address to send the resized image to");

        fillColorCombo = new JComboBox<>(new String[]{"black", "white", "gray"});
        fillColorCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        fillColorCombo.setSelectedItem("black");

        statusLabel = new JLabel("Ready to resize images");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        resizeButton = new JButton("Resize Image");
        resizeButton.setFont(new Font("Arial", Font.BOLD, 13));
        resizeButton.addActionListener(e -> resizeImage());
        resizeButton.setEnabled(false);

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 12));
        clearButton.addActionListener(e -> clearFields());

        gmailSettingsButton = new JButton("Gmail Settings");
        gmailSettingsButton.setFont(new Font("Arial", Font.PLAIN, 12));
        gmailSettingsButton.addActionListener(e -> showCredentialsDialog(true));

        brightnessSlider = new JSlider(-100, 100, 0);
        brightnessSlider.setMajorTickSpacing(50);
        brightnessSlider.setMinorTickSpacing(10);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPreferredSize(new Dimension(220, 40));
        brightnessValueLabel = new JLabel("+0%");
        brightnessValueLabel.setPreferredSize(new Dimension(45, 20));
        brightnessSlider.addChangeListener(e -> {
            int val = brightnessSlider.getValue();
            brightnessValueLabel.setText((val >= 0 ? "+" : "") + val + "%");
        });

        borderColorCombo = new JComboBox<>(new String[]{"None", "Black", "White"});
        borderColorCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        borderThicknessSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));
        borderThicknessSpinner.setPreferredSize(new Dimension(70, borderThicknessSpinner.getPreferredSize().height));
        borderThicknessSpinner.setEnabled(false);
        borderColorCombo.addActionListener(e ->
            borderThicknessSpinner.setEnabled(!"None".equals(borderColorCombo.getSelectedItem()))
        );

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monaco", Font.PLAIN, 10));
        logArea.setBackground(new Color(240, 240, 240));
        logArea.setForeground(Color.BLACK);
        logArea.setCaretColor(Color.BLACK);
        logArea.setOpaque(true);
        logArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Conversion Log"));
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

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
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        JPanel bgColorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        bgColorRow.add(new JLabel("Background Color:"));
        bgColorRow.add(fillColorCombo);
        optionsPanel.add(bgColorRow);

        JPanel brightnessRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        brightnessRow.add(new JLabel("Brightness:"));
        brightnessRow.add(brightnessSlider);
        brightnessRow.add(brightnessValueLabel);
        optionsPanel.add(brightnessRow);

        JPanel borderRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        borderRow.add(new JLabel("Border:"));
        borderRow.add(borderColorCombo);
        borderRow.add(new JLabel("Thickness:"));
        borderRow.add(borderThicknessSpinner);
        borderRow.add(new JLabel("px"));
        optionsPanel.add(borderRow);

        mainPanel.add(optionsPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Email section
        JPanel emailPanel = createLabeledPanel("Send by Email (Gmail)");
        JPanel emailRow = new JPanel(new BorderLayout(10, 0));
        emailRow.add(new JLabel("Recipient: "), BorderLayout.WEST);
        emailRow.add(recipientEmailField, BorderLayout.CENTER);
        emailRow.add(gmailSettingsButton, BorderLayout.EAST);
        emailPanel.add(emailRow);
        mainPanel.add(emailPanel);
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

        mainPanel.add(scrollPane);

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
        String lastDir = prefs.get(PREF_LAST_DIRECTORY, null);
        if (lastDir != null) fileChooser.setCurrentDirectory(new File(lastDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "tiff", "cr3", "cr2", "nef", "arw", "raf", "dng"};
        for (String ext : imageExtensions) {
            fileChooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(ext.toUpperCase() + " files", ext)
            );
        }

        ImagePreviewPanel preview = new ImagePreviewPanel();
        fileChooser.setAccessory(preview);
        fileChooser.addPropertyChangeListener(preview);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImage = fileChooser.getSelectedFile();
            prefs.put(PREF_LAST_DIRECTORY, selectedImage.getParent());
            inputPathField.setText(selectedImage.getAbsolutePath());
            resizeButton.setEnabled(true);

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
        resizeButton.repaint();
        selectButton.setEnabled(false);
        selectButton.repaint();
        progressBar.setVisible(true);
        statusLabel.setText("Processing...");
        statusLabel.setForeground(Color.ORANGE);
        lastConversionSuccess = false;

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Starting conversion...");

                    String pythonScript = getPythonScriptPath();
                    String outputPath = outputPathField.getText().isEmpty() ? null : outputPathField.getText();
                    String fillColor = (String) fillColorCombo.getSelectedItem();
                    int brightnessVal = brightnessSlider.getValue();
                    String borderColor = (String) borderColorCombo.getSelectedItem();
                    int borderThickness = (Integer) borderThicknessSpinner.getValue();

                    ProcessBuilder pb = new ProcessBuilder(
                        "/opt/homebrew/bin/pyenv", "exec", "python", pythonScript,
                        selectedImage.getAbsolutePath()
                    );

                    if (outputPath != null) {
                        pb.command().add("-o");
                        pb.command().add(outputPath);
                    }

                    if (!"black".equals(fillColor)) {
                        pb.command().add("--fill");
                        pb.command().add(fillColor);
                    }

                    if (brightnessVal != 0) {
                        double factor = 1.0 + brightnessVal / 100.0;
                        pb.command().add("--brightness");
                        pb.command().add(String.format(java.util.Locale.US, "%.2f", factor));
                    }

                    if (!"None".equals(borderColor) && borderThickness > 0) {
                        pb.command().add("--border-color");
                        pb.command().add(borderColor.toLowerCase());
                        pb.command().add("--border-thickness");
                        pb.command().add(String.valueOf(borderThickness));
                    }

                    pb.directory(new File(getPythonScriptDir()));
                    pb.redirectErrorStream(true);

                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        publish(line);
                    }

                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        publish("✓ Conversion completed successfully!");
                        lastConversionSuccess = true;
                        lastOutputFile = outputPath != null ? new File(outputPath) : guessOutputFile();
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
                resizeButton.repaint();
                selectButton.setEnabled(true);
                selectButton.repaint();
                statusLabel.setText("Conversion complete");

                if (lastConversionSuccess && lastOutputFile != null) {
                    String recipient = recipientEmailField.getText().trim();
                    if (!recipient.isEmpty()) {
                        prefs.put(PREF_RECIPIENT_EMAIL, recipient);
                        sendEmailAsync(lastOutputFile, recipient);
                    }
                }
            }
        };

        worker.execute();
    }

    private File guessOutputFile() {
        if (selectedImage == null) return null;
        String outputName = selectedImage.getName().replaceFirst("[.][^.]+$", "") + "_frame_3840x2160.jpg";
        return new File(selectedImage.getParent(), outputName);
    }

    private void sendEmailAsync(File file, String recipient) {
        appendLog("Preparing to send email to " + recipient + "...");
        statusLabel.setText("Sending email...");
        statusLabel.setForeground(new Color(0, 80, 160));

        new Thread(() -> {
            try {
                String[] creds = getOrPromptCredentials();
                if (creds == null) {
                    SwingUtilities.invokeLater(() -> appendLog("Email cancelled — no Gmail credentials provided."));
                    return;
                }
                sendEmail(file, recipient, creds[0], creds[1]);
                SwingUtilities.invokeLater(() -> {
                    appendLog("✓ Email sent to " + recipient);
                    statusLabel.setText("Email sent");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("✗ Email failed: " + e.getMessage());
                    statusLabel.setText("Email failed");
                    statusLabel.setForeground(Color.RED);
                });
            }
        }, "email-sender").start();
    }

    private void sendEmail(File file, String to, String fromEmail, String appPassword) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Samsung Frame TV - Resized Image: " + file.getName());

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Please find attached your resized image for the Samsung Frame TV (3840×2160).\n\nFile: " + file.getName());

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.attachFile(file);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);
        Transport.send(message);
    }

    /** Returns {gmailAddress, appPassword}, or null if the user cancelled. */
    private String[] getOrPromptCredentials() {
        String savedEmail = prefs.get(PREF_GMAIL_ADDRESS, "");
        String savedPassword = prefs.get(PREF_GMAIL_APP_PASSWORD, "");

        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            return new String[]{savedEmail, savedPassword};
        }

        return showCredentialsDialog(false);
    }

    /**
     * Shows the Gmail credentials dialog.
     * @param forceShow if true, always show (for Settings button); returns null on cancel.
     */
    private String[] showCredentialsDialog(boolean forceShow) {
        String savedEmail = prefs.get(PREF_GMAIL_ADDRESS, "");
        String savedPassword = prefs.get(PREF_GMAIL_APP_PASSWORD, "");

        JTextField emailField = new JTextField(savedEmail, 25);
        JPasswordField passwordField = new JPasswordField(savedPassword, 25);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Gmail address:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("App Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel hint = new JLabel("<html><small>Use a Gmail App Password (Google Account → Security → App Passwords)</small></html>");
        hint.setForeground(Color.GRAY);
        panel.add(hint, gbc);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Gmail Credentials", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                prefs.put(PREF_GMAIL_ADDRESS, email);
                prefs.put(PREF_GMAIL_APP_PASSWORD, password);
                if (forceShow) appendLog("Gmail credentials saved.");
                return new String[]{email, password};
            }
        }
        return null;
    }

    private void clearFields() {
        selectedImage = null;
        inputPathField.setText("");
        outputPathField.setText("");
        fillColorCombo.setSelectedItem("black");
        brightnessSlider.setValue(0);
        brightnessValueLabel.setText("+0%");
        borderColorCombo.setSelectedItem("None");
        borderThicknessSpinner.setValue(50);
        borderThicknessSpinner.setEnabled(false);
        logArea.setText("");
        statusLabel.setText("Ready to resize images");
        resizeButton.setEnabled(false);
    }

    private void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String getPythonScriptPath() {
        try {
            // Resolve relative to the JAR file's location (target/), not user.dir
            java.net.URL url = FrameTvResizerUI.class.getProtectionDomain().getCodeSource().getLocation();
            Path jarDir = Paths.get(url.toURI()).getParent(); // .../target/
            return jarDir.resolve("../../frame_resize.py").normalize().toAbsolutePath().toString();
        } catch (Exception e) {
            // Fallback: user.dir relative
            return Paths.get(System.getProperty("user.dir")).resolve(PYTHON_SCRIPT_PATH).toAbsolutePath().toString();
        }
    }

    private String getPythonScriptDir() {
        Path scriptPath = Paths.get(getPythonScriptPath());
        return scriptPath.getParent().toString();
    }

    private Image createAppIcon() {
        return new ImageIcon(new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB)).getImage();
    }

    private class ImagePreviewPanel extends JPanel implements java.beans.PropertyChangeListener {
        private final int SIZE = 220;
        private final Set<String> RAW_EXTS = new HashSet<>(
            Arrays.asList("cr3", "cr2", "nef", "arw", "raf", "dng")
        );

        private BufferedImage thumb;
        private String status = "No image selected";
        private SwingWorker<BufferedImage, Void> currentWorker;

        ImagePreviewPanel() {
            setPreferredSize(new Dimension(SIZE + 20, SIZE + 20));
            setBorder(BorderFactory.createTitledBorder("Preview"));
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                load((File) evt.getNewValue());
            }
        }

        private void load(File file) {
            if (currentWorker != null) currentWorker.cancel(true);
            thumb = null;
            if (file == null || !file.isFile()) {
                status = "No image selected";
                repaint();
                return;
            }
            status = "Loading...";
            repaint();

            currentWorker = new SwingWorker<BufferedImage, Void>() {
                @Override
                protected BufferedImage doInBackground() throws Exception {
                    String ext = ext(file);
                    return RAW_EXTS.contains(ext) ? loadRaw(file) : loadStandard(file);
                }
                @Override
                protected void done() {
                    if (!isCancelled()) {
                        try { thumb = get(); } catch (Exception ignored) {}
                        status = thumb != null ? "" : "Cannot preview";
                        repaint();
                    }
                }
            };
            currentWorker.execute();
        }

        private BufferedImage loadStandard(File f) throws Exception {
            BufferedImage img = ImageIO.read(f);
            return img != null ? scale(img) : null;
        }

        private BufferedImage loadRaw(File f) throws Exception {
            File tmp = File.createTempFile("frametv_prev_", ".jpg");
            tmp.deleteOnExit();
            try {
                new ProcessBuilder(
                    "sips", "-s", "format", "jpeg",
                    "-z", String.valueOf(SIZE * 2), String.valueOf(SIZE * 2),
                    f.getAbsolutePath(), "--out", tmp.getAbsolutePath()
                ).redirectErrorStream(true).start().waitFor();
                if (tmp.length() > 0) {
                    BufferedImage img = ImageIO.read(tmp);
                    return img != null ? scale(img) : null;
                }
            } finally {
                tmp.delete();
            }
            return null;
        }

        private BufferedImage scale(BufferedImage img) {
            int w = img.getWidth(), h = img.getHeight();
            if (w <= SIZE && h <= SIZE) return img;
            float s = Math.min((float) SIZE / w, (float) SIZE / h);
            int nw = Math.round(w * s), nh = Math.round(h * s);
            BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, nw, nh, null);
            g.dispose();
            return out;
        }

        private String ext(File f) {
            String n = f.getName().toLowerCase();
            int i = n.lastIndexOf('.');
            return i >= 0 ? n.substring(i + 1) : "";
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth(), h = getHeight();
            if (thumb != null) {
                g.drawImage(thumb, (w - thumb.getWidth()) / 2, (h - thumb.getHeight()) / 2, null);
            } else {
                FontMetrics fm = g.getFontMetrics();
                g.drawString(status, (w - fm.stringWidth(status)) / 2, h / 2);
            }
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu appearanceMenu = new JMenu("Appearance");
        JMenu themeMenu = new JMenu("Theme");

        String currentTheme = prefs.get(PREF_THEME, DEFAULT_THEME);
        ButtonGroup group = new ButtonGroup();

        for (Map.Entry<String, String> entry : THEMES.entrySet()) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(entry.getKey());
            item.setSelected(entry.getValue().equals(currentTheme));
            item.addActionListener(e -> switchTheme(entry.getValue()));
            group.add(item);
            themeMenu.add(item);
        }

        appearanceMenu.add(themeMenu);
        menuBar.add(appearanceMenu);
        return menuBar;
    }

    private void switchTheme(String className) {
        prefs.put(PREF_THEME, className);
        try {
            LookAndFeel laf = (LookAndFeel) Class.forName(className)
                .getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(laf);
            FlatLaf.updateUI();
        } catch (Exception e) {
            appendLog("Theme error: " + e.getMessage());
        }
    }

    private static void applyTheme(String className) {
        try {
            LookAndFeel laf = (LookAndFeel) Class.forName(className)
                .getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            FlatDarculaLaf.setup();
        }
    }

    public static void main(String[] args) {
        Preferences prefs = Preferences.userNodeForPackage(FrameTvResizerUI.class);
        applyTheme(prefs.get(PREF_THEME, DEFAULT_THEME));
        SwingUtilities.invokeLater(() -> {
            FrameTvResizerUI frame = new FrameTvResizerUI();
            frame.setVisible(true);
        });
    }
}

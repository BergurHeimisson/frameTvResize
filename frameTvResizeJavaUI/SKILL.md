# Reusable Java Swing Skills

## 1. Send Email with Attachment via Gmail SMTP (JavaMail)

**Dependency:**
```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>
```

**Implementation:**
```java
Properties props = new Properties();
props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");
props.put("mail.smtp.host", "smtp.gmail.com");
props.put("mail.smtp.port", "587");

Session session = Session.getInstance(props, new Authenticator() {
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(fromEmail, appPassword);
    }
});

Message message = new MimeMessage(session);
message.setFrom(new InternetAddress(fromEmail));
message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
message.setSubject("Subject here");

MimeBodyPart textPart = new MimeBodyPart();
textPart.setText("Body text here");

MimeBodyPart attachmentPart = new MimeBodyPart();
attachmentPart.attachFile(file); // java.io.File

Multipart multipart = new MimeMultipart();
multipart.addBodyPart(textPart);
multipart.addBodyPart(attachmentPart);
message.setContent(multipart);
Transport.send(message);
```

**Notes:**
- Requires a Gmail **App Password**, not the account password (Google Account → Security → App Passwords)
- Run on a background thread to avoid blocking the UI

---

## 2. Persist User Preferences (Java Preferences API)

Stores key/value data per user on the local machine. On macOS stored in `~/Library/Preferences/`. Never touches the filesystem inside the project directory.

```java
Preferences prefs = Preferences.userNodeForPackage(MyApp.class);

// Save
prefs.put("key", value);

// Read with fallback
String value = prefs.get("key", "default");
```

---

## 3. Image Preview Accessory in JFileChooser

Attach a custom panel to `JFileChooser` that previews the selected file asynchronously.

```java
JFileChooser chooser = new JFileChooser();
ImagePreviewPanel preview = new ImagePreviewPanel();
chooser.setAccessory(preview);
chooser.addPropertyChangeListener(preview);
chooser.showOpenDialog(parent);
```

The preview panel listens for `JFileChooser.SELECTED_FILE_CHANGED_PROPERTY` and loads the thumbnail in a `SwingWorker`:

```java
class ImagePreviewPanel extends JPanel implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
            loadAsync((File) evt.getNewValue());
        }
    }
}
```

**RAW format preview (macOS):** Use the built-in `sips` tool to convert to a temp JPEG:
```java
new ProcessBuilder("sips", "-s", "format", "jpeg",
    "-z", "440", "440", rawFile.getAbsolutePath(),
    "--out", tmpFile.getAbsolutePath())
    .redirectErrorStream(true).start().waitFor();
```
Supports: CR3, CR2, NEF, ARW, RAF, DNG.

---

## 4. FlatLaf Theming with Runtime Theme Switcher

**Dependencies:**
```xml
<dependency>
    <groupId>com.formdev</groupId>
    <artifactId>flatlaf</artifactId>
    <version>3.7</version>
</dependency>
<dependency>
    <groupId>com.formdev</groupId>
    <artifactId>flatlaf-intellij-themes</artifactId>
    <version>3.7</version>
</dependency>
```

**Apply on startup (with saved preference):**
```java
String theme = prefs.get("theme", "com.formdev.flatlaf.FlatDarculaLaf");
UIManager.setLookAndFeel((LookAndFeel) Class.forName(theme)
    .getDeclaredConstructor().newInstance());
```

**Switch theme at runtime (no restart):**
```java
UIManager.setLookAndFeel((LookAndFeel) Class.forName(className)
    .getDeclaredConstructor().newInstance());
FlatLaf.updateUI();
```

**Pin component colors so theme cannot override them:**
```java
textArea.setBackground(Color.WHITE);
textArea.setForeground(Color.BLACK);
textArea.setOpaque(true);
```

---

## 5. Background Task with SwingWorker

Run long operations off the EDT while publishing progress to the UI:

```java
SwingWorker<Result, String> worker = new SwingWorker<>() {
    protected Result doInBackground() throws Exception {
        publish("Starting...");
        // long-running work
        publish("Done.");
        return result;
    }
    protected void process(List<String> chunks) {
        chunks.forEach(logArea::append); // runs on EDT
    }
    protected void done() {
        // runs on EDT after doInBackground completes
    }
};
worker.execute();
```

---

## 6. Run External Process (pyenv / shell tools)

When launching from a GUI (no shell PATH), always use full binary paths:

```java
ProcessBuilder pb = new ProcessBuilder(
    "/opt/homebrew/bin/pyenv", "exec", "python", scriptPath, arg1
);
pb.directory(workingDir);
pb.redirectErrorStream(true);
Process p = pb.start();

BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
String line;
while ((line = reader.readLine()) != null) {
    publish(line); // inside SwingWorker
}
int exitCode = p.waitFor();
```

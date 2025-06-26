package ide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;

import org.fife.ui.rtextarea.Gutter;
import org.xml.sax.InputSource;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public class LinterManager {
    private static void clearOldHighlights(GhostTextPane textArea, Gutter gutter) {
        textArea.removeAllLineHighlights();
        if (gutter != null) gutter.removeAllTrackingIcons();
    }
    private static Path writeCodeToTempFile(String code) throws IOException {
        Path path = Files.createTempFile("temp", ".java");
        Files.write(path, code.getBytes());
        return path;
    }
    private static Checker createConfiguredChecker(GhostTextPane textArea, Gutter gutter) throws Exception {
        Checker checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());
        InputSource configSource = new InputSource(new File("google_checks.xml").toURI().toString());
        Configuration fileConfig = ConfigurationLoader.loadConfiguration(
            configSource,
            new PropertiesExpander(System.getProperties()),
            ConfigurationLoader.IgnoredModulesOptions.EXECUTE
        );
        checker.configure(fileConfig);

        checker.addListener(new AuditListener() {
            @Override public void auditStarted(AuditEvent e) {}
            @Override public void auditFinished(AuditEvent e) {}
            @Override public void fileStarted(AuditEvent e) {}
            @Override public void fileFinished(AuditEvent e) {}

            @Override
            public void addError(AuditEvent event) {
                try {
                    highlightError(textArea, gutter, event);
                } catch (Exception e) {
                    System.err.println("Error highlighting: " + e.getMessage());
                }
            }

            @Override
            public void addException(AuditEvent event, Throwable throwable) {
                System.err.println("Checkstyle Exception: " + throwable.getMessage());
            }
        });

        return checker;
    }
    private static void highlightError(GhostTextPane textArea, Gutter gutter, AuditEvent event)
            throws BadLocationException {
        int line = event.getLine() - 1;
        int col = Math.max(0, event.getColumn() - 1);
        int lineStart = textArea.getLineStartOffset(line);
        int lineEnd = textArea.getLineEndOffset(line);
        String lineText = textArea.getText(lineStart, lineEnd - lineStart);

        int tokenStart = col, tokenEnd = col;
        if (Character.isJavaIdentifierPart(lineText.charAt(col))) {
            while (tokenStart > 0 && Character.isJavaIdentifierPart(lineText.charAt(tokenStart - 1))) tokenStart--;
            while (tokenEnd < lineText.length() && Character.isJavaIdentifierPart(lineText.charAt(tokenEnd))) tokenEnd++;
        } else {
            tokenEnd = tokenStart + 1;
        }

        int start = Math.max(lineStart + tokenStart, 0);
        int end = Math.min(lineStart + tokenEnd, textArea.getDocument().getLength());

        textArea.getHighlighter().addHighlight(start, end, new UnderlineHighlightPainter(Color.RED));
        gutter.addLineTrackingIcon(line, UIManager.getIcon("OptionPane.warningIcon"), event.getMessage());
    }
    public static void runCheckstyleAndHighlight(Editor editor) {
        if (editor == null) return;
        GhostTextPane textArea = editor.getTextArea();
        Gutter gutter = editor.getGutter();
        try {
            clearOldHighlights(textArea, gutter);
            String code = textArea.getText();
            if (code.isEmpty()) return;

            Path tempPath = writeCodeToTempFile(code);
            Checker checker = createConfiguredChecker(textArea, gutter);
            checker.process(Collections.singletonList(tempPath.toFile()));
            checker.destroy();
        } catch (Exception ex) {
            System.err.println("Error running checkstyle: " + ex.getMessage());
        }
    }
    public static JDialog ModifyLinterSettings(JFrame parent){
        var dialog = new JDialog(parent, "Linter Settings", true);
        dialog.setSize(250,100);
        dialog.setLocationRelativeTo(parent);
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Choose Checkstyle config:");
        JTextField configPathField = new JTextField("google_checks.xml");
        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                configPathField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        JPanel inputRow = new JPanel(new BorderLayout());
        inputRow.add(configPathField, BorderLayout.CENTER);
        inputRow.add(browseBtn, BorderLayout.EAST);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            String path = configPathField.getText().trim();
            System.out.println("Saved config path: " + path);
            dialog.dispose();
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(inputRow, BorderLayout.CENTER);
        panel.add(saveBtn, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        return dialog;
    }
}
package ide;

import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
public final class Terminal extends RSyntaxTextArea{
    private static final long serialVersionUID = 1L;
    private transient Process process;
    private boolean isRunning = false;
    private File currentDir;
    private String prompt = "> ";
    private int inputStart = 0;
    
    JButton copyBtn = new JButton("Copy");
    public Terminal(String projectRoot) {
        append("Welcome to the HSIDE terminal!\n");
        copyBtn.addActionListener((ActionEvent e) -> {
            String selectedText = Terminal.this.getSelectedText();
            if (selectedText != null && !selectedText.isEmpty()) {
                StringSelection selection = new StringSelection(selectedText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            }
        });
        add(copyBtn);
        setEditable(true);
        currentDir = new File(projectRoot);
        updatePrompt();
        String os = getCurrentOS();
        try {
            switch (os) {
                case "Windows" -> setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
                case "Unix" -> setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
                case "Unknown" -> setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                default -> throw new AssertionError();
            }
        } catch (Exception e) {
            append("[ERR] Failed to set syntax style: " + e.getMessage() + "\n");
        }
        this.setCurrentLineHighlightColor(new Color(0, 0, 0, 0));
        this.setBackground(new Color(50, 50, 50));
        this.setForeground(Color.WHITE);
        this.setCaretColor(Color.WHITE);
        this.setFont(new Font("Consolas", Font.PLAIN, 12));
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    if (getCaretPosition() < inputStart) {
                        setCaretPosition(getText().length());
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        e.consume();
                        String fullText = getText();
                        String command = fullText.substring(inputStart).trim();
                        append("\n");
                        handleCommand(command);
                    }
                } catch (Exception ex) {
                    append("[ERR] Terminal error: " + ex.getMessage() + "\n");
                    updatePrompt();
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {
                try {
                    // Prevent typing into output area
                    if (getCaretPosition() < inputStart) {
                        setCaretPosition(getText().length());
                    }
                } catch (Exception ex) {
                    append("[ERR] Terminal error: " + ex.getMessage() + "\n");
                    updatePrompt();
                }
            }
        });
    }
    private String getCurrentOS(){
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "Windows";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            return "Unix";
        }
        return "Unknown";
    }
    private void handleCommand(String command) {
        if (command.isEmpty()) {
            updatePrompt();
            return;
        }

        if (command.startsWith("cd")) {
            String[] parts = command.split("\\s+", 2);
            if (parts.length == 2) {
                File newDir = new File(parts[1]);
                if (!newDir.isAbsolute()) {
                    newDir = new File(currentDir, parts[1]);
                }
                if (newDir.exists() && newDir.isDirectory()) {
                    currentDir = newDir;
                } else {
                    append("Directory does not exist: " + parts[1] + "\n");
                }
            } else {
                append("Usage: cd <directory>\n");
            }
            updatePrompt();
            return;
        }

        runCommand(command);
    }

    private void runCommand(String command) {
        if (isRunning) {
            append("Process already running...\n");
            updatePrompt();
            return;
        }

        isRunning = true;
        append("Running: " + command + "\n");
        String os = getCurrentOS();
        try {
            switch (os) {
                case "Windows" -> process = Runtime.getRuntime().exec(new String[]{"cmd", "/c", command}, null, currentDir);
                case "Unix" -> process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command}, null, currentDir);
                default -> {
                    append("Unsupported OS.\n");
                    isRunning = false;
                    updatePrompt();
                    return;
                }
            }
        } catch (IOException e) {
            append("Failed to start process: " + e.getMessage() + "\n");
            isRunning = false;
            updatePrompt();
            return;
        }
        
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String outputLine = line;
                    SwingUtilities.invokeLater(() -> append(outputLine + "\n"));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> append("Error reading output: " + e.getMessage() + "\n"));
            }
        }).start();

        new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                SwingUtilities.invokeLater(() -> {
                    append("Process exited with code " + exitCode + "\n");
                    isRunning = false;
                    updatePrompt();
                });
            } catch (InterruptedException e) {
                SwingUtilities.invokeLater(() -> append("Process was interrupted.\n"));
                isRunning = false;
                updatePrompt();
            }
        }).start();
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String outputLine = line;
                    SwingUtilities.invokeLater(() -> append("[ERR] " + outputLine + "\n"));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> append("Error reading stderr: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    private void updatePrompt() {
        prompt = currentDir.getAbsolutePath() + " > ";
        append(prompt);
        inputStart = getText().length();
    }

    @Override
    public void append(String str) {
        super.append(str);
        setCaretPosition(getText().length());
    }

    public void stop() {
        if (isRunning && process != null) {
            process.destroy();
            append("\nProcess terminated.\n");
            isRunning = false;
            updatePrompt();
        }
    }
    @Override
    public String getSelectedText() {
        String selectedText ="";
        try {
            selectedText = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
            System.err.println("Failed to get clipboard contents: " + e.getMessage());
        }
        return selectedText;
    }

}

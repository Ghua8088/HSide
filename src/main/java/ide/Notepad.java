package ide;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;

import org.xml.sax.InputSource;

import org.fife.ui.rtextarea.Gutter;
import com.formdev.flatlaf.FlatDarkLaf;
import com.vdurmont.emoji.EmojiParser;

import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
public final class Notepad extends JFrame {
    private static final long serialVersionUID = 1L;
    private transient AIClient aiClient = AIBridge.getInstance();
    private NotepadManager manager;
    private LayoutManager layoutManager;
    private File currentProjectRoot;
    private JTree fileTree;
    private final Terminal terminal;
    private DefaultTreeModel treeModel;
    private JSplitPane splitPane;
    private JSplitPane editorTerminalSplitPane;
    private final transient MouseAdapter mouseAdapter;
    private ImageIcon icon;
    private ImageIcon darkmodeIcon;
    private ImageIcon lightmodeIcon;
    
    // AI settings state
    private String[] availableModels = aiClient.getAvailableModels();
    private String selectedModel = aiClient.getModel();
    private String ollamaPort = aiClient.getPort();
    
    // Find/Replace components
    private JDialog findReplaceDialog;
    private JTextField findField, replaceField;
    private JButton findNextBtn, replaceBtn, replaceAllBtn, closeBtn;
    public Notepad() {
        NotificationsHandler.init(this);
        setFocusTraversalKeysEnabled(true);
        
        // Initialize preferences manager first
        PreferencesManager prefs = PreferencesManager.getInstance();
        
        // Initialize theme manager
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.setMainFrame(this);
        
        // Load saved theme from preferences
        String savedTheme = prefs.getTheme();
        themeManager.applyTheme(savedTheme);
        
        // Initialize icons
        icon = loadIcon("/icons/HSIDE.png", 24, false);
        darkmodeIcon = loadIcon("/icons/darkmode.png", 16, false);
        lightmodeIcon = loadIcon("/icons/lightmode.png", 16, true);
        
        // Load window preferences
        setSize(prefs.getWindowWidth(), prefs.getWindowHeight());
        
        if (prefs.getWindowX() != -1 && prefs.getWindowY() != -1) {
            setLocation(prefs.getWindowX(), prefs.getWindowY());
        } else {
            setLocationRelativeTo(null);
        }
        
        if (prefs.isWindowMaximized()) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeCheck();
            }
        });
        setIconImage(icon.getImage());
        
        // Initialize manager
        manager = new NotepadManager(this);
        
        // Setup file tree
        currentProjectRoot = new File(System.getProperty("user.dir"));
        DefaultMutableTreeNode rootNode = createFileTree(currentProjectRoot);
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new FileTree(treeModel);
        
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    splitPane.setDividerLocation(200);
                    TreePath path = fileTree.getSelectionPath();
                    if (path != null) {
                        StringBuilder filePath = new StringBuilder(currentProjectRoot.getAbsolutePath());
                        Object[] nodes = path.getPath();
                        for (int i = 1; i < nodes.length; i++) { // skip root
                            filePath.append(File.separator).append(nodes[i].toString());
                        }
                        File selectedFile = new File(filePath.toString());
                        try {
                            String mimeType = Files.probeContentType(selectedFile.toPath());
                            if (mimeType == null || !mimeType.startsWith("text")) {
                                NotificationsHandler.showError("Unsupported file type: " + mimeType);
                                return;
                            }
                        } catch (IOException ex) {
                            NotificationsHandler.showError("Could not determine file type.");
                            return;
                        }
                        if (selectedFile.isFile()) {
                            String absPath = selectedFile.getAbsolutePath();
                            Editor existing = findEditorTabByFilePath(absPath);
                            if (existing != null) {
                                manager.getTabbedPane().setSelectedComponent(existing);
                            } else {
                                try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                                    Editor editor = manager.addNewEditorTab(selectedFile.getName());
                                    editor.setFilePath(absPath);
                                    StringBuilder sb = new StringBuilder();
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        sb.append(line).append("\n");
                                    }
                                    editor.setText(sb.toString());
                                    manager.getTabbedPane().setSelectedComponent(editor);
                                } catch (IOException ex) {
                                    NotificationsHandler.showError("Failed to open file: " + ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        };
        fileTree.addMouseListener(mouseAdapter);
        
        // Setup terminal
        terminal = new Terminal(currentProjectRoot.getAbsolutePath());
        JScrollPane terminalScroll = new JScrollPane(terminal);
        editorTerminalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, manager.getTabbedPane(), terminalScroll);
        editorTerminalSplitPane.setDividerLocation(600);
        
        // Create main horizontal split pane for file tree and editor/terminal
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileTree, editorTerminalSplitPane);
        splitPane.setDividerLocation(200);
        
        // Initialize layout manager
        layoutManager = new LayoutManager(this, fileTree, terminal, splitPane, editorTerminalSplitPane, manager.getTabbedPane());
        
        // Setup find/replace dialog
        setupFindReplaceDialog();
        
        // Set layout manager reference and add layout buttons to menu bar
        manager.setLayoutManager(layoutManager);
        manager.setLayoutButtons(
            layoutManager.getLeftPanelToggle(),
            layoutManager.getBottomPanelToggle(),
            layoutManager.getRightPanelToggle()
        );
        
        // Create a panel to hold menu bar and layout buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(manager.getMenuBar(), BorderLayout.CENTER);
        topPanel.add(manager.getLayoutButtonPanel(), BorderLayout.EAST);
        
        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(manager.getFooter(), BorderLayout.SOUTH);
        
        // Setup linter timer
        Timer timer = new Timer(1 * 60 * 1000, e -> LinterManager.runCheckstyleAndHighlight(getCurrentEditor()));
        timer.start();
        
        setVisible(true);
    }

    private void setupFindReplaceDialog() {
        findReplaceDialog = new JDialog(this, "Find/Replace", false);
        findReplaceDialog.setSize(400, 150);
        findReplaceDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        findReplaceDialog.add(new JLabel("Find:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        findField = new JTextField(20);
        findReplaceDialog.add(findField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        findReplaceDialog.add(new JLabel("Replace:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        replaceField = new JTextField(20);
        findReplaceDialog.add(replaceField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        findNextBtn = new JButton("Find Next");
        replaceBtn = new JButton("Replace");
        replaceAllBtn = new JButton("Replace All");
        closeBtn = new JButton("Close");
        btnPanel.add(findNextBtn);
        btnPanel.add(replaceBtn);
        btnPanel.add(replaceAllBtn);
        btnPanel.add(closeBtn);
        findReplaceDialog.add(btnPanel, gbc);
        
        closeBtn.addActionListener(e -> findReplaceDialog.setVisible(false));
        findNextBtn.addActionListener(e -> findNext());
        replaceBtn.addActionListener(e -> replaceCurrent());
        replaceAllBtn.addActionListener(e -> replaceAll());
        
        findReplaceDialog.setLocationRelativeTo(this);
    }
    
    public void showFindDialog() {
        findReplaceDialog.setTitle("Find");
        replaceField.setEnabled(false);
        replaceBtn.setEnabled(false);
        replaceAllBtn.setEnabled(false);
        findReplaceDialog.setVisible(true);
    }
    
    public void showReplaceDialog() {
        findReplaceDialog.setTitle("Find/Replace");
        replaceField.setEnabled(true);
        replaceBtn.setEnabled(true);
        replaceAllBtn.setEnabled(true);
        findReplaceDialog.setVisible(true);
    }
    
    public void setProjectRoot(File root) {
        currentProjectRoot = root;
        DefaultMutableTreeNode rootNode = createFileTree(root);
        treeModel.setRoot(rootNode);
    }
    private void reopenClosedTab() {
        // This is now handled by NotepadManager
    }
    private void runCheckstyleAndHighlight() {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        GhostTextPane textArea = editor.getTextArea();
        Gutter gutter = editor.getGutter();
        try {
            textArea.removeAllLineHighlights();
            if (gutter != null) gutter.removeAllTrackingIcons();
            String code = textArea.getText();
            if (code.isEmpty()) return;
            java.nio.file.Path path = java.nio.file.Files.createTempFile("temp", ".java");
            java.nio.file.Files.write(path, code.getBytes());
            Checker checker = new Checker();
            checker.setModuleClassLoader(Checker.class.getClassLoader());
            Configuration fileConfig = ConfigurationLoader.loadConfiguration(
                new InputSource("google_checks.xml"),
                new PropertiesExpander(System.getProperties()),
                ConfigurationLoader.IgnoredModulesOptions.EXECUTE
            );
            checker.configure(fileConfig);
            checker.addListener(new AuditListener() {
                @Override
                public void auditStarted(AuditEvent event) {}
                @Override
                public void auditFinished(AuditEvent event) {}
                @Override
                public void fileStarted(AuditEvent event) {}
                @Override
                public void fileFinished(AuditEvent event) {}
                @Override
                public void addError(AuditEvent event) {
                    int line = event.getLine() - 1;
                    int col = event.getColumn() - 1; // Checkstyle columns are 1-based
                    try {
                        int lineStart = textArea.getLineStartOffset(line);
                        int lineEnd = textArea.getLineEndOffset(line);
                        String lineText = textArea.getText(lineStart, lineEnd - lineStart);
                        if (col < 0 || col >= lineText.length()) {
                            col = 0;
                        }
                        int tokenStart = col;
                        int tokenEnd = col;
                        if (Character.isJavaIdentifierPart(lineText.charAt(col))) {
                            while (tokenStart > 0 && Character.isJavaIdentifierPart(lineText.charAt(tokenStart - 1))) {
                                tokenStart--;
                            }
                            while (tokenEnd < lineText.length() && Character.isJavaIdentifierPart(lineText.charAt(tokenEnd))) {
                                tokenEnd++;
                            }
                        } else {
                            tokenEnd = tokenStart + 1;
                        }
                        int start = lineStart + tokenStart;
                        int end = lineStart + tokenEnd;
                        if (start >= end || start < 0 || end > textArea.getDocument().getLength()) {
                            start = lineStart;
                            end = lineEnd;
                        }
                        Highlighter highlighter = textArea.getHighlighter();
                        highlighter.addHighlight(start, end, new UnderlineHighlightPainter(Color.RED));
                        Icon icon = UIManager.getIcon("OptionPane.warningIcon");
                        gutter.addLineTrackingIcon(line, icon, event.getMessage());
                    } catch (BadLocationException | IndexOutOfBoundsException e) {
                        System.err.println("Error highlighting: " + e.getMessage());
                    }
                }
                @Override
                public void addException(AuditEvent event, Throwable throwable) {
                    System.err.println("Error: " + throwable.getMessage());
                }
            });
            checker.process(Collections.singletonList(new File(path.toString())));
            checker.destroy();
        } catch (CheckstyleException | IOException ex) {
            System.err.println("Error running checkstyle: " + ex.getMessage());
        }
    }
    void setCounts(Editor editor) {
        manager.updateCounts(editor);
    }
    
    void save() {
        // This is now handled by NotepadManager
    }
    void closeCheck() {
        // Save window preferences before closing
        saveWindowPreferences();
        
        Editor editor = getCurrentEditor();
        if (editor != null && Boolean.FALSE.equals(editor.getSave()) && !editor.getText().isBlank()) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes.\nDo you want to save before closing?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            switch (result) {
                case JOptionPane.YES_OPTION -> {
                    // Save functionality is now in NotepadManager
                    System.exit(0);
                }
                case JOptionPane.NO_OPTION -> System.exit(0);
                case JOptionPane.CANCEL_OPTION -> { /* Do nothing */ }
            }
        } else {
            System.exit(0);
        }
    }
    
    private void saveWindowPreferences() {
        PreferencesManager prefs = PreferencesManager.getInstance();
        
        // Save window size and position
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            prefs.setWindowMaximized(true);
        } else {
            prefs.setWindowMaximized(false);
            prefs.setWindowWidth(getWidth());
            prefs.setWindowHeight(getHeight());
            prefs.setWindowX(getX());
            prefs.setWindowY(getY());
        }
    }
    private DefaultMutableTreeNode createFileTree(File dir) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir.getName());
        File[] files = dir.listFiles();
        if (files != null) {
            for (File currFile : files) {
                if (currFile.isDirectory()) {
                    node.add(createFileTree(currFile));
                } else {
                    node.add(new DefaultMutableTreeNode(currFile.getName()));
                }
            }
        }
        return node;
    }
    private void openFile(String path) {
        // This is now handled by NotepadManager
    }
    
    void findNext() {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        String findText = findField.getText();
        if (findText.isEmpty()) return;
        String content = editor.getText();
        int start = editor.getTextArea().getSelectionEnd();
        int idx = content.indexOf(findText, start);
        if (idx == -1 && start > 0) {
            // Wrap around
            idx = content.indexOf(findText);
        }
        if (idx != -1) {
            editor.getTextArea().requestFocus();
            editor.getTextArea().select(idx, idx + findText.length());
        } else {
            NotificationsHandler.showInfo("Text not found.");
        }
    }
    
    void replaceCurrent() {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        String findText = findField.getText();
        String replaceText = replaceField.getText();
        if (findText.isEmpty()) return;
        int selStart = editor.getTextArea().getSelectionStart();
        int selEnd = editor.getTextArea().getSelectionEnd();
        if (selStart != selEnd && editor.getTextArea().getSelectedText().equals(findText)) {
            try {
                editor.getTextArea().getDocument().remove(selStart, findText.length());
                editor.getTextArea().getDocument().insertString(selStart, replaceText, null);
                editor.getTextArea().select(selStart, selStart + replaceText.length());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        findNext();
    }
    
    void replaceAll() {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        String findText = findField.getText();
        String replaceText = replaceField.getText();
        if (findText.isEmpty()) return;
        String content = editor.getText();
        int count = 0;
        int idx = content.indexOf(findText);
        while (idx != -1) {
            count++;
            content = content.substring(0, idx) + replaceText + content.substring(idx + findText.length());
            idx = content.indexOf(findText, idx + replaceText.length());
        }
        editor.setText(content);
        if (count == 0) {
            NotificationsHandler.showInfo("No occurrences replaced.");
        } else {
            NotificationsHandler.showInfo(count + " occurrences replaced.");
        }
    }
    public static void main(String[] args) {
        OllamaInstaller.checkAndInstall();
        SwingUtilities.invokeLater(Notepad::new);
    }    
    private Editor findEditorTabByFilePath(String filePath) {
        for (int i = 0; i < manager.getTabbedPane().getTabCount(); i++) {
            Editor ed = (Editor) manager.getTabbedPane().getComponentAt(i);
            if (filePath.equals(ed.getFilePath())) {
                return ed;
            }
        }
        return null;
    }
    
    public Editor getCurrentEditor() {
        return manager.getCurrentEditor();
    }
    
    public NotepadManager getManager() {
        return manager;
    }
   public ImageIcon loadIcon(String path, int size, boolean invert) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            BufferedImage original = ImageIO.read(resource);

            // Invert colors if needed
            if (invert) {
                for (int y = 0; y < original.getHeight(); y++) {
                    for (int x = 0; x < original.getWidth(); x++) {
                        int rgba = original.getRGB(x, y);
                        Color col = new Color(rgba, true);
                        Color inv = new Color(255 , 0, 0, col.getAlpha());
                        original.setRGB(x, y, inv.getRGB());
                    }
                }
            }

            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(original, 0, 0, size, size, null);
            g2.dispose();
            return new ImageIcon(scaled);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

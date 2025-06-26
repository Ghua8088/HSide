package ide;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections; 

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;

import org.xml.sax.InputSource;

import org.fife.ui.rtextarea.Gutter;


import com.formdev.flatlaf.FlatLightLaf;
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
public final class Notepad extends JFrame implements ActionListener{
    private static final long serialVersionUID = 1L;
    private transient AIClient aiClient=AIBridge.getInstance();
    private ArrayList<Editor> RecentlyClosedEditor ;
    JMenuBar menubar; 
    JMenu file,edit,help,AI,fonts,fontStyle,fontsize,linterSettings;
    JMenuItem newFile,openFile,saveFile,exit,bold,italic,underline,fontcollection,github,AISettings,modifyLinterSettings;
    JButton viewmode;
    private JTabbedPane tabbedPane;
    JButton confirmsize,confirmfont;
    private File currentProjectRoot;
    JPanel fontSize;
    JComboBox<String> fontselect;
    private JTree fileTree;
    private final Terminal terminal;
    private DefaultTreeModel treeModel;
    private JSplitPane splitPane;
    JLabel fontpreview;
    String fontS[]=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();//gets all Font Names
    JTextField fontSizeN; 
    JFileChooser fileChooser; 
    JDialog fontSelector; 
    JDialog aiSettingsDialog; 
    JPanel footer;
    JLabel word_count,line_count,character_count,support,font_size;
    ImageIcon icon;
    Font f;
    boolean saved=false;
    // AI settings state
    private String[] availableModels = aiClient.getAvailableModels();
    private String selectedModel = aiClient.getModel();
    private String ollamaPort = aiClient.getport();
    JMenuItem findItem, replaceItem;
    JDialog findReplaceDialog;
    JTextField findField, replaceField;
    JButton findNextBtn, replaceBtn, replaceAllBtn, closeBtn;
    private final transient MouseAdapter  mouseAdapter;
    private transient TabReorderHandler reorderHandler;
    public Notepad(){
        saved=false;
        NotificationsHandler.init(this);
        RecentlyClosedEditor = new ArrayList<>();
        setFocusTraversalKeysEnabled(true);
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Tree.leftChildIndent", 5);  
            UIManager.put("Tree.rightChildIndent", 5);
        } catch (UnsupportedLookAndFeelException e) {
            
        }
        SwingUtilities.updateComponentTreeUI(getRootPane());
        f=new Font("Arial",Font.PLAIN,20);
        icon=new ImageIcon("HSIDE.png");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        setSize((int) (width * 0.8), (int) (height * 0.8));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeCheck();
            }
        });
        setLocationRelativeTo(null);
        setIconImage(icon.getImage());
        menubar=new JMenuBar();
        menubar.setBackground(new Color(30,30,30));
        menubar.setForeground(Color.WHITE);
        footer=new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        line_count=new JLabel("Line Count: 0");
        word_count=new JLabel("Word Count: 0");
        character_count=new JLabel("Character Count: 0");
        support=new JLabel("UTF-16");
        footer.add(word_count);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(character_count);
        footer.add(Box.createHorizontalGlue());
        footer.add(support);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(line_count);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        file=new JMenu("File");
        edit=new JMenu("Edit");
        help=new JMenu("Help");
        AI=new JMenu("AI");
        viewmode=new JButton(EmojiParser.parseToUnicode(":night_with_stars:")); // Default to dark emoji
        viewmode.setToolTipText("Toggle Dark Mode");
        newFile=new JMenuItem("New");
        openFile=new JMenuItem("Open");
        saveFile=new JMenuItem("Save");
        exit=new JMenuItem("Exit");
        AISettings=new JMenuItem("AI Settings");
        linterSettings=new JMenu("Linter Settings");
        modifyLinterSettings=new JMenuItem("Modify Linter Settings");
        linterSettings.add(modifyLinterSettings);
        modifyLinterSettings.addActionListener(e -> {
            var linterDialog = LinterManager.ModifyLinterSettings(this);
            linterDialog.setVisible(true);
        });
        tabbedPane = new JTabbedPane();
        reorderHandler= new TabReorderHandler(tabbedPane);
        tabbedPane.addMouseListener(reorderHandler);
        tabbedPane.addMouseMotionListener(reorderHandler);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        addNewEditorTab("Untitled");
        currentProjectRoot = new File(System.getProperty("user.dir"));
        DefaultMutableTreeNode rootNode = createFileTree(currentProjectRoot);
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new FileTree(treeModel);
        mouseAdapter= new MouseAdapter() {
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
                                tabbedPane.setSelectedComponent(existing);
                            } else {
                                try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                                    Editor editor = addNewEditorTab(selectedFile.getName());
                                    editor.setFilePath(absPath); // Track the file path in the editor
                                    StringBuilder sb = new StringBuilder();
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        sb.append(line).append("\n");
                                    }
                                    editor.setText(sb.toString());
                                    tabbedPane.setSelectedComponent(editor);
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
        terminal = new Terminal(currentProjectRoot.getAbsolutePath());
        JScrollPane terminalScroll = new JScrollPane(terminal);
        JSplitPane terminalcodearea = new JSplitPane(JSplitPane.VERTICAL_SPLIT,tabbedPane, terminalScroll);
        terminalcodearea.setDividerLocation(600);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,  fileTree ,terminalcodearea);
        splitPane.setDividerLocation(200);
        fileChooser=new JFileChooser();
        fonts=new JMenu("Fonts");
        fontselect=new JComboBox<>(fontS);
        fontcollection=new JMenuItem("fontcollection");
        fontStyle=new JMenu("Style");
        bold = new JCheckBoxMenuItem("Bold");
        italic = new JCheckBoxMenuItem("Italic");
        underline = new JCheckBoxMenuItem("Underline");
        fontSize=new JPanel();
        fontSize.setLayout(new GridLayout(1,3));
        font_size=new JLabel("size:");
        fontSizeN=new JTextField("20");
        confirmsize=new JButton("▶");
        fontsize=new JMenu("size");
        fontsize.add(fontSize);
        confirmsize.setToolTipText("Confirm Font Size");
        fontpreview=new JLabel("AaBbCc 123");
        fontpreview.setFont(f);
        fontpreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        fontSelector=new JDialog(this, "Font Selector", true);
        fontSelector.setSize(300,300);
        fontSelector.getContentPane().setBackground(new Color(45,45,45));
        fontSelector.getContentPane().setForeground(Color.WHITE);
        confirmfont=new JButton("▶");
        github=new JMenuItem("git reference");
        fontcollection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,KeyEvent.ALT_DOWN_MASK));
        fontSelector.add(fontselect,BorderLayout.NORTH);
        fontpreview.setFont(f);
        fontpreview.setBackground(new Color(45,45,45));
        fontpreview.setForeground(Color.WHITE);
        fontSelector.add(fontpreview,BorderLayout.CENTER);
        fontSelector.add(confirmfont,BorderLayout.SOUTH);
        fontselect.addItemListener((ItemEvent e) -> { 
            fontpreview.setFont(new Font((String)fontselect.getSelectedItem(),Font.PLAIN,15));
        });
        fontSize.add(font_size);
        fontSize.add(fontSizeN);
        fontSize.add(confirmsize);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,2));
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,2));
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,2));
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,2));
        menubar.add(file);
        help.add(github);
        menubar.add(edit);
        menubar.add(help);
        menubar.add(viewmode, BorderLayout.EAST);
        file.add(newFile);
        file.addSeparator();
        file.add(openFile);
        file.add(saveFile);
        file.addSeparator();
        file.add(exit);
        fontStyle.add(bold);
        fontStyle.add(italic);
        fontStyle.add(underline);
        fonts.add(fontStyle);
        fonts.add(fontcollection);
        fonts.add(fontsize);
        edit.add(fonts);
        edit.add(linterSettings);
        add(menubar,BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(footer,BorderLayout.SOUTH);
        
        AI.add(AISettings);
        menubar.add(AI);
        // AI Settings Dialog setup
        aiSettingsDialog = new JDialog(this, "AI Settings", true);
        aiSettingsDialog.setSize(350, 200);
        aiSettingsDialog.setLayout(new BorderLayout());
        JPanel aiPanel = new JPanel();
        aiPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        aiPanel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> modelCombo = new JComboBox<>(availableModels);
        modelCombo.setSelectedItem(selectedModel);
        aiPanel.add(modelCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        aiPanel.add(new JLabel("Ollama Port:"), gbc);
        gbc.gridx = 1;
        JTextField portField = new JTextField(ollamaPort,10);
        aiPanel.add(portField, gbc);
        aiSettingsDialog.add(aiPanel, BorderLayout.CENTER);
        JPanel aiButtonPanel = new JPanel();
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        aiButtonPanel.add(okBtn);
        aiButtonPanel.add(cancelBtn);
        aiSettingsDialog.add(aiButtonPanel, BorderLayout.SOUTH);
        okBtn.addActionListener(e -> {
            selectedModel = (String) modelCombo.getSelectedItem();
            aiClient.setModel(selectedModel);
            ollamaPort = portField.getText();
            aiClient.setport(ollamaPort);
            aiSettingsDialog.setVisible(false);
        });
        cancelBtn.addActionListener(e -> aiSettingsDialog.setVisible(false));
        AISettings.addActionListener(e -> {
            modelCombo.setSelectedItem(selectedModel);
            portField.setText(ollamaPort);
            aiSettingsDialog.setLocationRelativeTo(this);
            aiSettingsDialog.setVisible(true);
        });
        findItem = new JMenuItem("Find");
        replaceItem = new JMenuItem("Replace");
        findItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        replaceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK));
        edit.add(findItem);
        edit.add(replaceItem);

        // Find/Replace Dialog
        findReplaceDialog = new JDialog(this, "Find/Replace", false);
        findReplaceDialog.setSize(400, 150);
        findReplaceDialog.setLayout(new GridBagLayout());
        GridBagConstraints frgbc = new GridBagConstraints();
        frgbc.insets = new Insets(5, 5, 5, 5);
        frgbc.gridx = 0; frgbc.gridy = 0; frgbc.anchor = GridBagConstraints.EAST;
        findReplaceDialog.add(new JLabel("Find:"), frgbc);
        frgbc.gridx = 1; frgbc.anchor = GridBagConstraints.WEST;
        findField = new JTextField(20);
        findReplaceDialog.add(findField, frgbc);
        frgbc.gridx = 0; frgbc.gridy = 1; frgbc.anchor = GridBagConstraints.EAST;
        findReplaceDialog.add(new JLabel("Replace:"), frgbc);
        frgbc.gridx = 1; frgbc.anchor = GridBagConstraints.WEST;
        replaceField = new JTextField(20);
        findReplaceDialog.add(replaceField, frgbc);
        frgbc.gridx = 0; frgbc.gridy = 2; frgbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        findNextBtn = new JButton("Find Next");
        replaceBtn = new JButton("Replace");
        replaceAllBtn = new JButton("Replace All");
        closeBtn = new JButton("Close");
        btnPanel.add(findNextBtn);
        btnPanel.add(replaceBtn);
        btnPanel.add(replaceAllBtn);
        btnPanel.add(closeBtn);
        findReplaceDialog.add(btnPanel, frgbc);
        findItem.addActionListener(e -> {
            findReplaceDialog.setTitle("Find");
            replaceField.setEnabled(false);
            replaceBtn.setEnabled(false);
            replaceAllBtn.setEnabled(false);
            findReplaceDialog.setVisible(true);
        });
        replaceItem.addActionListener(e -> {
            findReplaceDialog.setTitle("Find/Replace");
            replaceField.setEnabled(true);
            replaceBtn.setEnabled(true);
            replaceAllBtn.setEnabled(true);
            findReplaceDialog.setVisible(true);
        });
        initializeListeners();
        closeBtn.addActionListener(e -> findReplaceDialog.setVisible(false));
        findNextBtn.addActionListener(e -> findNext());
        replaceBtn.addActionListener(e -> replaceCurrent());
        replaceAllBtn.addActionListener(e -> replaceAll());
        setVisible(true);
        Timer timer = new Timer(1 * 60 * 1000, e -> LinterManager.runCheckstyleAndHighlight(getCurrentEditor()));
        timer.start();
        ShortcutRegistry.ApplyListener("shift ctrl T",getRootPane(), () -> reopenClosedTab());
        ShortcutRegistry.ApplyListener("ctrl W",getRootPane(), () -> closeTab());
        ShortcutRegistry.ApplyListener("ctrl S",getRootPane(), () -> save());
        for (int i = 0; i <= 9; i++) {
            final int index = (i == 0 ? 9 : i - 1);
            String keyStroke = "ctrl " + i;
            ShortcutRegistry.ApplyListener(keyStroke, getRootPane(), () -> openTab(index));
        }
    }

    private void reload(){
        System.out.println("Reloading...");
    }
    private void initializeListeners(){
        viewmode.addActionListener(this);
        confirmfont.addActionListener(this);
        fontcollection.addActionListener(this);
        newFile.addActionListener(this);
        openFile.addActionListener(this);
        saveFile.addActionListener(this);
        fonts.addActionListener(this);
        exit.addActionListener(this);
        bold.addActionListener(this);
        italic.addActionListener(this);
        underline.addActionListener(this);
        confirmsize.addActionListener(this);
        github.addActionListener(this);
        fontSelector.setLocationRelativeTo(this);
        findReplaceDialog.setLocationRelativeTo(this);
    }
    private void reopenClosedTab(){
        if (!RecentlyClosedEditor.isEmpty()){
            Editor ed = RecentlyClosedEditor.remove(0);
            if (ed != null){
                String title = ed.getFilePath() != null ? new File(ed.getFilePath()).getName() : "Untitled";
                addTabWithCloseButton(title, ed);
                tabbedPane.setSelectedComponent(ed);
                System.out.println("reopened: "+ed);
            }
        }
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
    void setCounts(Editor editor){
        HashMap<String,String> counts = editor.updatecounts();
        word_count.setText(counts.get("word_count"));
        character_count.setText(counts.get("character_count"));
        line_count.setText(counts.get("line_count")); 
    }
    void save() {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        File targetFile = editor.getFilePath() != null
            ? new File(editor.getFilePath())
            : null;
        if (targetFile == null) {
            int result = fileChooser.showSaveDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;
            targetFile = fileChooser.getSelectedFile();
            editor.setFilePath(targetFile.getAbsolutePath()); 
            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), targetFile.getName());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile))) {
            String text = editor.getText();
            bw.write(text);
            word_count.setText(editor.getWordCountLabel());
            character_count.setText(editor.getCharacterCountLabel());
            line_count.setText(editor.getLineCountLabel());
            saved = true;
        } catch (IOException ex) {
            System.out.println("Exception saving file: " + ex);
        }
    }
    void closeCheck() {
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
                    save();
                    System.exit(0);
                }
                case JOptionPane.NO_OPTION -> System.exit(0);
                case JOptionPane.CANCEL_OPTION -> { /* Do nothing */ }
            }
        } else {
            System.exit(0);
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
    private void openFile(String path){
        Editor newEditor = addNewEditorTab(path);
        SyntaxHelper.setSyntaxStyleByExtension(newEditor.getTextArea(), path);
        FileInputStream fis;
        try {
            fis=new FileInputStream(path);
        } catch (FileNotFoundException ex) {
            System.out.println("File not found: "+path);
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while((line=br.readLine())!=null){
                sb.append(line).append("\n");
            }
            newEditor.setText(sb.toString());
        }catch(IOException ex){
            System.out.println("Error opening file:"+ex);
        }
        tabbedPane.setSelectedComponent(newEditor);
        setCounts(newEditor);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Editor editor = getCurrentEditor();
        if(e.getSource()==newFile){
            addNewEditorTab("Untitled");
        }
        else if(e.getSource()==openFile){
            int result=fileChooser.showOpenDialog(this);
            if(result==JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile().isDirectory()){
                File selectedDir = fileChooser.getSelectedFile();
                currentProjectRoot = selectedDir;
                DefaultMutableTreeNode rootNode = createFileTree(selectedDir);
                treeModel.setRoot(rootNode);
            }else{
                File readfile=fileChooser.getSelectedFile();
                openFile(readfile.getAbsolutePath());
            }
        }else if(e.getSource()==saveFile){
            save();
        }else if(e.getSource()==bold||e.getSource()==italic||e.getSource()==underline){
            if (editor == null) return;
            int style = Font.PLAIN;
            if (bold.isSelected()) style |= Font.BOLD;
            if (italic.isSelected()) style |= Font.ITALIC;
            Font baseFont = editor.getTextArea().getFont().deriveFont(style);
            if (underline.isSelected()) {
                HashMap<TextAttribute, Object> attributes = new HashMap<>(baseFont.getAttributes());
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                editor.getTextArea().setFont(baseFont.deriveFont(attributes));
            } else {
                editor.getTextArea().setFont(baseFont);
            }
        }else if(e.getSource()==confirmsize){
            if (editor == null) return;
            editor.getTextArea().setFont(new Font("Arial",Font.PLAIN,Integer.parseInt(fontSizeN.getText())));
        }else if(e.getSource()==exit){
            closeCheck();
            System.exit(0);
        }else if(e.getSource()==fontcollection){
            fontSelector.setVisible(true);
        }else if(e.getSource()==confirmfont){
            if (editor == null) return;
            editor.getTextArea().setFont(new Font((String)fontselect.getSelectedItem(),Font.PLAIN,Integer.parseInt(fontSizeN.getText())));
            fontSelector.setVisible(false);
        }else if(e.getSource()==viewmode){
            boolean toDark = viewmode.getText().equals(EmojiParser.parseToUnicode(":sunny:"));
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Editor ed = (Editor) tabbedPane.getComponentAt(i);
                Font currentFont = ed.getTextArea().getFont();
                if(toDark){
                    ed.getTextArea().setBackground(new Color(45,45,45));
                    ed.getTextArea().setForeground(new Color(190,190,190,190));
                    ed.getTextArea().setCaretColor(Color.WHITE);
                    ed.getTextArea().setDarkMode(true);
                    ed.getTextArea().setCurrentLineHighlightColor(new Color(10,10,10,10));
                    Gutter gutter = ed.getGutter();
                    gutter.setBackground(new Color(30, 30, 30));
                    gutter.setLineNumberColor(new Color(0, 255, 239));
                    gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 10));
                    gutter.setBorderColor(new Color(60, 60, 60));
                    gutter.setCurrentLineNumberColor(new Color(50, 255, 239));
                } else {
                    ed.getTextArea().setBackground(new Color(255,255,240));
                    ed.getTextArea().setForeground(Color.BLACK);
                    ed.getTextArea().setCaretColor(Color.BLACK);
                    ed.getTextArea().setDarkMode(false);
                    ed.getTextArea().setCurrentLineHighlightColor(new Color(255,255,200,80));
                    Gutter gutter = ed.getGutter();
                    gutter.setBackground(new Color(245, 245, 245));
                    gutter.setLineNumberColor(new Color(80, 80, 80));
                    gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 10));
                    gutter.setBorderColor(new Color(200, 200, 200));
                    gutter.setCurrentLineNumberColor(new Color(120, 80, 80));
                }
                // Save the font for reapplication after UI update
                ed.getTextArea().putClientProperty("savedFont", currentFont);
            }
            if(toDark){
                viewmode.setText(EmojiParser.parseToUnicode(":night_with_stars:"));
                menubar.setBackground(new Color(30,30,30));
                menubar.setForeground(Color.WHITE);
                footer.setBackground(new Color(30,30,30));
                footer.setForeground(Color.WHITE);
                fontSelector.getContentPane().setBackground(new Color(45,45,45));
                fontSelector.getContentPane().setForeground(Color.WHITE);
                fontpreview.setBackground(new Color(45,45,45));
                fontpreview.setForeground(Color.WHITE);
                try { UIManager.setLookAndFeel(new FlatDarkLaf()); SwingUtilities.updateComponentTreeUI(this); } 
                catch (UnsupportedLookAndFeelException | RuntimeException ex) {
                    NotificationsHandler.showError("Failed to set Dark Mode: " + ex.getMessage());
                }
            }else{
                viewmode.setText(EmojiParser.parseToUnicode(":sunny:"));
                menubar.setBackground(null);
                menubar.setForeground(null);
                footer.setBackground(null);
                footer.setForeground(null);
                fontSelector.getContentPane().setBackground(Color.WHITE);
                fontSelector.getContentPane().setForeground(Color.BLACK);
                fontpreview.setBackground(Color.WHITE);
                fontpreview.setForeground(Color.BLACK);
                try { UIManager.setLookAndFeel(new FlatLightLaf()); SwingUtilities.updateComponentTreeUI(this); }
                catch (UnsupportedLookAndFeelException | RuntimeException ex) {
                    NotificationsHandler.showError("Failed to set Light Mode: " + ex.getMessage());
                }
            }
            // Reapply the font for each tab after UI update
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Editor ed = (Editor) tabbedPane.getComponentAt(i);
                Font savedFont = (Font) ed.getTextArea().getClientProperty("savedFont");
                if (savedFont != null) {
                    ed.getTextArea().setFont(savedFont);
                }
            }
        }else if(e.getSource()==github){
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start " + "https://github.com/Ghua8088?tab=repositories"});
            } catch (IOException e1) {
                System.out.println("exception loading URL:"+e1);
            }
            System.out.println("Redirecting to GITHUB " + EmojiParser.parseToUnicode(":heart_eyes:"));
        }else if(e.getSource()==findItem || e.getSource()==replaceItem){
            findReplaceDialog.setVisible(true);
        }
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
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.out.println("Failed to initialize FlatLaf");
        }
        SwingUtilities.invokeLater(Notepad::new);
    }    
    private Editor findEditorTabByFilePath(String filePath) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Editor ed = (Editor) tabbedPane.getComponentAt(i);
            if (filePath.equals(ed.getFilePath())) {
                return ed;
            }
        }
        return null;
    }
    private void closeTab(){
            Editor ed = (Editor) tabbedPane.getSelectedComponent();
            if (ed != null){
                RecentlyClosedEditor.add(ed);
                System.out.println("Closed: "+ed);
                System.out.println("History: "+RecentlyClosedEditor);
            }
            int closeIdx = tabbedPane.indexOfComponent(ed); 
            if (closeIdx != -1) tabbedPane.remove(closeIdx);
    }
    private void openTab(int index){
        Editor ed = (Editor) tabbedPane.getComponentAt(index);
        if (ed != null){
            tabbedPane.setSelectedComponent(ed);
        }
    }
    private void addTabWithCloseButton(String title, Editor editor) {
        tabbedPane.addTab(title, editor);
        int idx = tabbedPane.indexOfComponent(editor);
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabPanel.setOpaque(false);
        JLabel tabTitle = new JLabel(title);
        JButton closeBtn = new JButton(" X ");
        closeBtn.setMargin(new Insets(0, 5, 0, 5));
        closeBtn.setFocusable(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder());
        closeBtn.setContentAreaFilled(false);
        closeBtn.setOpaque(false);
        closeBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD,10));
        closeBtn.addActionListener(e -> {
            closeTab();
        });
        tabPanel.add(tabTitle);
        tabPanel.add(closeBtn);
        tabbedPane.setTabComponentAt(idx, tabPanel);
    }
    private Editor addNewEditorTab(String title) {
        Editor editor = new Editor("");
        // Style the gutter for this editor
        Gutter gutter = editor.getGutter();
        gutter.setBackground(new Color(30, 30, 30));
        gutter.setLineNumberColor(new Color(0, 255, 239));
        gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 10));
        gutter.setBorderColor(new Color(60, 60, 60));
        gutter.setCurrentLineNumberColor(new Color(50, 255, 239));
        addTabWithCloseButton(title, editor);
        tabbedPane.setSelectedComponent(editor);
        return editor;
    }
    private Editor getCurrentEditor() {
        return (Editor) tabbedPane.getSelectedComponent();
    }
}

package ide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotepadManager {
    private final Notepad notepad;
    private final ThemeManager themeManager;
    private LayoutManager layoutManager;
    private final ArrayList<Editor> recentlyClosedEditors;
    private final JTabbedPane tabbedPane;
    private final JMenuBar menuBar;
    private final JPanel footer;
    private final JLabel wordCount, lineCount, characterCount, positionCount, support;
    private final JFileChooser fileChooser;
    private final JPanel layoutButtonPanel;
    
    public NotepadManager(Notepad notepad) {
        this.notepad = notepad;
        this.themeManager = ThemeManager.getInstance();
        this.recentlyClosedEditors = new ArrayList<>();
        this.tabbedPane = new JTabbedPane();
        this.menuBar = new JMenuBar();
        this.footer = new JPanel();
        this.wordCount = new JLabel("Word Count: 0");
        this.lineCount = new JLabel("Line Count: 0");
        this.characterCount = new JLabel("Character Count: 0");
        this.positionCount = new JLabel("ln 1 col 1");
        this.support = new JLabel("UTF-16");
        this.fileChooser = new JFileChooser();
        this.layoutButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        
        initializeComponents();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Initialize menu bar
        setupMenuBar();
        
        // Initialize footer
        setupFooter();
        
        // Initialize tabbed pane
        setupTabbedPane();
        // Initialize file chooser
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }
    
    private void setupMenuBar() {
        menuBar.setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width, 24));
        
        // Create menus
        JMenu fileMenu = createFileMenu();
        JMenu editMenu = createEditMenu();
        JMenu helpMenu = createHelpMenu();
        JMenu aiMenu = createAIMenu();
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        menuBar.add(aiMenu);
    }
    
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newFile = new JMenuItem("New");
        JMenuItem openFile = new JMenuItem("Open");
        JMenuItem saveFile = new JMenuItem("Save");
        JMenuItem exit = new JMenuItem("Exit");
        
        // Set accelerators
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 2));
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, 2));
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 2));
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 2));
        
        // Add action listeners
        newFile.addActionListener(e -> newFile());
        openFile.addActionListener(e -> openFile());
        saveFile.addActionListener(e -> saveFile());
        exit.addActionListener(e -> exit());
        
        fileMenu.add(newFile);
        fileMenu.addSeparator();
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.addSeparator();
        fileMenu.add(exit);
        
        return fileMenu;
    }
    
    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        
        JMenu fontsMenu = createFontsMenu();
        JMenu themeMenu = createThemeMenu();
        JMenu linterSettingsMenu = new JMenu("Linter Settings");
        JMenuItem modifyLinterSettings = new JMenuItem("Modify Linter Settings");
        JMenuItem preferencesItem = new JMenuItem("Preferences");
        JMenuItem findItem = new JMenuItem("Find");
        JMenuItem replaceItem = new JMenuItem("Replace");
        
        // Set accelerators
        preferencesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        findItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        replaceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK));
        
        // Add action listeners
        modifyLinterSettings.addActionListener(e -> {
            var linterDialog = LinterManager.ModifyLinterSettings(notepad);
            linterDialog.setVisible(true);
        });
        preferencesItem.addActionListener(e -> showPreferencesDialog());
        findItem.addActionListener(e -> notepad.showFindDialog());
        replaceItem.addActionListener(e -> notepad.showReplaceDialog());
        
        linterSettingsMenu.add(modifyLinterSettings);
        editMenu.add(fontsMenu);
        editMenu.add(themeMenu);
        editMenu.add(linterSettingsMenu);
        editMenu.addSeparator();
        editMenu.add(preferencesItem);
        editMenu.addSeparator();
        editMenu.add(findItem);
        editMenu.add(replaceItem);
        
        return editMenu;
    }
    
    private JMenu createFontsMenu() {
        JMenu fontsMenu = new JMenu("Fonts");
        
        JMenu fontStyleMenu = new JMenu("Style");
        JCheckBoxMenuItem bold = new JCheckBoxMenuItem("Bold");
        JCheckBoxMenuItem italic = new JCheckBoxMenuItem("Italic");
        JCheckBoxMenuItem underline = new JCheckBoxMenuItem("Underline");
        
        JMenuItem fontCollection = new JMenuItem("Font Collection");
        fontCollection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK));
        
        JMenu fontSizeMenu = new JMenu("Size");
        JPanel fontSizePanel = new JPanel(new GridLayout(1, 3));
        JLabel fontSizeLabel = new JLabel("Size:");
        JTextField fontSizeField = new JTextField("20");
        JButton confirmSize = new JButton("▶");
        
        fontSizePanel.add(fontSizeLabel);
        fontSizePanel.add(fontSizeField);
        fontSizePanel.add(confirmSize);
        fontSizeMenu.add(fontSizePanel);
        
        // Add action listeners
        bold.addActionListener(e -> applyFontStyle());
        italic.addActionListener(e -> applyFontStyle());
        underline.addActionListener(e -> applyFontStyle());
        fontCollection.addActionListener(e -> showFontSelector());
        confirmSize.addActionListener(e -> applyFontSize(fontSizeField.getText()));
        
        fontStyleMenu.add(bold);
        fontStyleMenu.add(italic);
        fontStyleMenu.add(underline);
        
        fontsMenu.add(fontStyleMenu);
        fontsMenu.add(fontCollection);
        fontsMenu.add(fontSizeMenu);
        
        return fontsMenu;
    }
    
    private JMenu createThemeMenu() {
        JMenu themeMenu = new JMenu("Theme");
        
        // Get available themes from ThemeManager
        Map<String, ThemeManager.ThemeInfo> themes = themeManager.getAvailableThemes();
        
        // Create a button group for radio button selection
        ButtonGroup themeGroup = new ButtonGroup();
        
        for (Map.Entry<String, ThemeManager.ThemeInfo> entry : themes.entrySet()) {
            String themeName = entry.getKey();
            ThemeManager.ThemeInfo themeInfo = entry.getValue();
            
            JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(themeName);
            themeItem.setSelected(themeName.equals(themeManager.getCurrentTheme()));
            
            themeItem.addActionListener(e -> {
                themeManager.applyTheme(themeName);
                applyThemeToAllEditors();
            });
            
            themeGroup.add(themeItem);
            themeMenu.add(themeItem);
        }
        
        return themeMenu;
    }
    
    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        JMenuItem github = new JMenuItem("Git Reference");
        github.addActionListener(e -> openGitHub());
        helpMenu.add(github);
        return helpMenu;
    }
    
    private JMenu createAIMenu() {
        JMenu aiMenu = new JMenu("AI");
        JMenuItem aiSettings = new JMenuItem("AI Settings");
        JMenuItem aiChat = new JMenuItem("AI Chat");
        
        aiSettings.addActionListener(e -> showAISettings());
        aiChat.addActionListener(e -> toggleAIChat());
        
        aiMenu.add(aiSettings);
        aiMenu.addSeparator();
        aiMenu.add(aiChat);
        return aiMenu;
    }
    
    private void toggleAIChat() {
        if (layoutManager != null) {
            layoutManager.toggleRightPanel();
        }
    }
    
    private void setupFooter() {
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.add(wordCount);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(characterCount);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(positionCount);
        footer.add(Box.createHorizontalGlue());
        footer.add(support);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(lineCount);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
    
    private void setupTabbedPane() {
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        
        // Add tab reorder handler
        TabReorderHandler reorderHandler = new TabReorderHandler(tabbedPane);
        tabbedPane.addMouseListener(reorderHandler);
        tabbedPane.addMouseMotionListener(reorderHandler);
        
        // Add tab change listener to update counts when switching tabs
        tabbedPane.addChangeListener(e -> {
            Editor currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                updateCounts(currentEditor);
            }
        });
        
        // Add initial tab
        addNewEditorTab("Untitled");
    }    
    private void setupEventHandlers() {
        // Setup shortcuts
        ShortcutRegistry.ApplyListener("shift ctrl T", notepad.getRootPane(), this::reopenClosedTab);
        ShortcutRegistry.ApplyListener("ctrl W", notepad.getRootPane(), this::closeTab);
        ShortcutRegistry.ApplyListener("ctrl S", notepad.getRootPane(), this::saveFile);
        
        // Layout toggle shortcuts
        ShortcutRegistry.ApplyListener("ctrl shift L", notepad.getRootPane(), this::toggleLeftPanel);
        ShortcutRegistry.ApplyListener("ctrl shift B", notepad.getRootPane(), this::toggleBottomPanel);
        ShortcutRegistry.ApplyListener("ctrl shift R", notepad.getRootPane(), this::toggleRightPanel);
        
        // Setup tab shortcuts (Ctrl+1-9)
        for (int i = 0; i <= 9; i++) {
            final int index = (i == 0 ? 9 : i - 1);
            String keyStroke = "ctrl " + i;
            ShortcutRegistry.ApplyListener(keyStroke, notepad.getRootPane(), () -> openTab(index));
        }
    }
    
    // Public methods for Notepad to access
    public JMenuBar getMenuBar() { return menuBar; }
    public JTabbedPane getTabbedPane() { return tabbedPane; }
    public JPanel getFooter() { return footer; }
    public JFileChooser getFileChooser() { return fileChooser; }
    public JPanel getLayoutButtonPanel() { return layoutButtonPanel; }
    
    public void setLayoutManager(LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }
    
    public void setLayoutButtons(JButton leftToggle, JButton bottomToggle, JButton rightToggle) {
        layoutButtonPanel.removeAll();
        layoutButtonPanel.add(leftToggle);
        layoutButtonPanel.add(bottomToggle);
        layoutButtonPanel.add(rightToggle);
        layoutButtonPanel.revalidate();
        layoutButtonPanel.repaint();
    }
    
    private void toggleLeftPanel() {
        if (layoutManager != null) {
            layoutManager.toggleFileTree();
        }
    }
    
    private void toggleBottomPanel() {
        if (layoutManager != null) {
            layoutManager.toggleTerminal();
        }
    }
    
    private void toggleRightPanel() {
        if (layoutManager != null) {
            layoutManager.toggleRightPanel();
        }
    }
    
    // Action methods
    private void newFile() {
        addNewEditorTab("Untitled");
    }
    
    private void openFile() {
        int result = fileChooser.showOpenDialog(notepad);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.isDirectory()) {
                // Handle directory selection for file tree
                notepad.setProjectRoot(selectedFile);
            } else {
                openFile(selectedFile.getAbsolutePath());
            }
        }
    }
    
    private void openFile(String path) {
        Editor newEditor = addNewEditorTab(new File(path).getName());
        SyntaxHelper.setSyntaxStyleByExtension(newEditor.getTextArea(), path);
        
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            newEditor.setText(sb.toString());
            newEditor.setFilePath(path);
        } catch (IOException ex) {
            NotificationsHandler.showError("Error opening file: " + ex.getMessage());
        }
        
        tabbedPane.setSelectedComponent(newEditor);
        // Counts will be updated automatically via the callback
    }
    
    private void saveFile() {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        
        File targetFile = editor.getFilePath() != null ? new File(editor.getFilePath()) : null;
        if (targetFile == null) {
            int result = fileChooser.showSaveDialog(notepad);
            if (result != JFileChooser.APPROVE_OPTION) return;
            targetFile = fileChooser.getSelectedFile();
            editor.setFilePath(targetFile.getAbsolutePath());
            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), targetFile.getName());
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile))) {
            bw.write(editor.getText());
            editor.setSave(true);
            updateCounts(editor);
        } catch (IOException ex) {
            NotificationsHandler.showError("Error saving file: " + ex.getMessage());
        }
    }
    
    private void exit() {
        notepad.closeCheck();
    }
    
    private void showThemeSelector() {
        JDialog themeDialog = new JDialog(notepad, "Select Theme", true);
        themeDialog.setLayout(new BorderLayout());
        themeDialog.setSize(300, 400);
        themeDialog.setLocationRelativeTo(notepad);
        
        JPanel themePanel = new JPanel();
        themePanel.setLayout(new BoxLayout(themePanel, BoxLayout.Y_AXIS));
        
        // Get available themes
        Map<String, ThemeManager.ThemeInfo> themes = themeManager.getAvailableThemes();
        ButtonGroup themeGroup = new ButtonGroup();
        
        for (Map.Entry<String, ThemeManager.ThemeInfo> entry : themes.entrySet()) {
            String themeName = entry.getKey();
            ThemeManager.ThemeInfo themeInfo = entry.getValue();
            
            JRadioButton themeButton = new JRadioButton(themeName);
            themeButton.setSelected(themeName.equals(themeManager.getCurrentTheme()));
            
            themeButton.addActionListener(e -> {
                themeManager.applyTheme(themeName);
                applyThemeToAllEditors();
            });
            
            themeGroup.add(themeButton);
            themePanel.add(themeButton);
            themePanel.add(Box.createVerticalStrut(5));
        }
        
        JScrollPane scrollPane = new JScrollPane(themePanel);
        themeDialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> themeDialog.dispose());
        themeDialog.add(closeButton, BorderLayout.SOUTH);
        
        themeDialog.setVisible(true);
    }
    
    private void toggleTheme() {
        themeManager.toggleDarkMode();
        applyThemeToAllEditors();
    }    
    private void applyThemeToAllEditors() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Editor editor = (Editor) tabbedPane.getComponentAt(i);
            themeManager.applyEditorTheme(editor);
        }
    }
    
    private void applyFontStyle() {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        
        // Get current font
        Font currentFont = editor.getTextArea().getFont();
        int currentStyle = currentFont.getStyle();
        int newStyle = currentStyle;
        
        // Toggle styles based on menu state
        // This would need to track the state of bold/italic/underline checkboxes
        // For now, let's implement a simple font style selector
        showFontStyleSelector(editor);
    }
    
    private void showFontStyleSelector(Editor editor) {
        JDialog styleDialog = new JDialog(notepad, "Font Style", true);
        styleDialog.setLayout(new BorderLayout());
        styleDialog.setSize(300, 200);
        styleDialog.setLocationRelativeTo(notepad);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Font family selection
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Font Family:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        String[] fontFamilies = {"Consolas", "Monaco", "Courier New", "Arial", "Times New Roman", "Verdana"};
        JComboBox<String> fontFamilyCombo = new JComboBox<>(fontFamilies);
        fontFamilyCombo.setSelectedItem(editor.getTextArea().getFont().getFamily());
        mainPanel.add(fontFamilyCombo, gbc);
        
        // Font size selection
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Font Size:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        Integer[] fontSizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 32, 36, 40, 48, 56, 64, 72};
        JComboBox<Integer> fontSizeCombo = new JComboBox<>(fontSizes);
        fontSizeCombo.setSelectedItem(editor.getTextArea().getFont().getSize());
        mainPanel.add(fontSizeCombo, gbc);
        
        // Style checkboxes
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Style:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox boldCheck = new JCheckBox("Bold");
        JCheckBox italicCheck = new JCheckBox("Italic");
        stylePanel.add(boldCheck);
        stylePanel.add(italicCheck);
        mainPanel.add(stylePanel, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Cancel");
        
        applyButton.addActionListener(e -> {
            try {
                String family = (String) fontFamilyCombo.getSelectedItem();
                int size = (Integer) fontSizeCombo.getSelectedItem();
                int style = Font.PLAIN;
                if (boldCheck.isSelected()) style |= Font.BOLD;
                if (italicCheck.isSelected()) style |= Font.ITALIC;
                
                Font newFont = new Font(family, style, size);
                editor.getTextArea().setFont(newFont);
                
                NotificationsHandler.showInfo("Font applied successfully!");
                styleDialog.dispose();
            } catch (Exception ex) {
                NotificationsHandler.showError("Failed to apply font: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> styleDialog.dispose());
        
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        
        styleDialog.add(mainPanel, BorderLayout.CENTER);
        styleDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        styleDialog.setVisible(true);
    }
    
    private void applyFontSize(String size) {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        try {
            int fontSize = Integer.parseInt(size);
            Font currentFont = editor.getTextArea().getFont();
            Font newFont = new Font(currentFont.getFamily(), currentFont.getStyle(), fontSize);
            editor.getTextArea().setFont(newFont);
            NotificationsHandler.showInfo("Font size applied: " + fontSize);
        } catch (NumberFormatException e) {
            NotificationsHandler.showError("Invalid font size: " + size);
        }
    }
    
    private void showFontSelector() {
        Editor editor = getCurrentEditor();
        if (editor == null) return;
        
        // Use the same font style selector for font collection
        showFontStyleSelector(editor);
    }
    
    private void showAISettings() {
        JDialog aiDialog = new JDialog(notepad, "AI Settings", true);
        aiDialog.setLayout(new BorderLayout());
        aiDialog.setSize(500, 400);
        aiDialog.setLocationRelativeTo(notepad);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Get AI client
        AIClient aiClient = AIBridge.getInstance();
        
        // Provider selection
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Provider:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        AIClient.Provider[] providers = aiClient.getAvailableProviders();
        JComboBox<AIClient.Provider> providerCombo = new JComboBox<>(providers);
        providerCombo.setSelectedItem(aiClient.getCurrentProvider());
        mainPanel.add(providerCombo, gbc);
        
        // API Key setting
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("API Key:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        JPasswordField apiKeyField = new JPasswordField(aiClient.getApiKey(), 30);
        mainPanel.add(apiKeyField, gbc);
        
        // Base URL setting
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Base URL:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField urlField = new JTextField(aiClient.getBaseUrl(), 30);
        mainPanel.add(urlField, gbc);
        
        // Model selection
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Model:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        String[] availableModels = aiClient.getAvailableModels();
        JComboBox<String> modelCombo = new JComboBox<>(availableModels);
        modelCombo.setSelectedItem(aiClient.getModel());
        mainPanel.add(modelCombo, gbc);
        
        // Port setting (only for Ollama)
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Port (Ollama):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        JTextField portField = new JTextField(aiClient.getPort(), 10);
                 mainPanel.add(portField, gbc);
         
         // Initial setup based on current provider
         AIClient.Provider currentProvider = aiClient.getCurrentProvider();
         boolean isOllama = currentProvider == AIClient.Provider.OLLAMA;
         
         // Hide API key field for Ollama initially
         apiKeyField.setEnabled(!isOllama);
         apiKeyField.setVisible(!isOllama);
         mainPanel.getComponent(2).setVisible(!isOllama); // API Key label
         mainPanel.getComponent(3).setVisible(!isOllama); // API Key field
         
         // Show port field only for Ollama initially
         portField.setEnabled(isOllama);
         portField.setVisible(isOllama);
         mainPanel.getComponent(8).setVisible(isOllama); // Port label
         mainPanel.getComponent(9).setVisible(isOllama); // Port field
         
         // Provider change listener
         providerCombo.addActionListener(e -> {
             AIClient.Provider selectedProvider = (AIClient.Provider) providerCombo.getSelectedItem();
             aiClient.setProvider(selectedProvider);
             
             // Update URL field
             urlField.setText(aiClient.getBaseUrl());
             
             // Update model combo
             modelCombo.removeAllItems();
             String[] models = aiClient.getAvailableModels();
             for (String model : models) {
                 modelCombo.addItem(model);
             }
             if (models.length > 0) {
                 modelCombo.setSelectedItem(models[0]);
             }
             
             // Show/hide fields based on provider
             boolean isOllamaProvider = selectedProvider == AIClient.Provider.OLLAMA;
             
             // API Key field - hide for Ollama, show for others
             apiKeyField.setEnabled(!isOllamaProvider);
             apiKeyField.setVisible(!isOllamaProvider);
             mainPanel.getComponent(2).setVisible(!isOllamaProvider); // API Key label
             mainPanel.getComponent(3).setVisible(!isOllamaProvider); // API Key field
             
             // Port field - show only for Ollama
             portField.setEnabled(isOllamaProvider);
             portField.setVisible(isOllamaProvider);
             mainPanel.getComponent(8).setVisible(isOllamaProvider); // Port label
             mainPanel.getComponent(9).setVisible(isOllamaProvider); // Port field
             
             // Adjust layout
             aiDialog.pack();
             aiDialog.setLocationRelativeTo(notepad);
         });
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JButton testButton = new JButton("Test Connection");
        
                 saveButton.addActionListener(e -> {
             try {
                 AIClient.Provider selectedProvider = (AIClient.Provider) providerCombo.getSelectedItem();
                 aiClient.setProvider(selectedProvider);
                 
                 // Only set API key for non-Ollama providers
                 if (selectedProvider != AIClient.Provider.OLLAMA) {
                     aiClient.setApiKey(new String(apiKeyField.getPassword()));
                 } else {
                     aiClient.setApiKey(""); // Clear API key for Ollama
                 }
                 
                 aiClient.setBaseUrl(urlField.getText());
                 aiClient.setModel((String) modelCombo.getSelectedItem());
                 aiClient.setPort(portField.getText());
                 NotificationsHandler.showInfo("AI settings saved successfully!");
                 aiDialog.dispose();
             } catch (Exception ex) {
                 NotificationsHandler.showError("Failed to save AI settings: " + ex.getMessage());
             }
         });
        
                 testButton.addActionListener(e -> {
             try {
                 // Temporarily set the settings for testing
                 AIClient.Provider originalProvider = aiClient.getCurrentProvider();
                 String originalApiKey = aiClient.getApiKey();
                 String originalUrl = aiClient.getBaseUrl();
                 String originalModel = aiClient.getModel();
                 
                 AIClient.Provider selectedProvider = (AIClient.Provider) providerCombo.getSelectedItem();
                 aiClient.setProvider(selectedProvider);
                 
                 // Only set API key for non-Ollama providers
                 if (selectedProvider != AIClient.Provider.OLLAMA) {
                     aiClient.setApiKey(new String(apiKeyField.getPassword()));
                 } else {
                     aiClient.setApiKey(""); // Clear API key for Ollama
                 }
                 
                 aiClient.setBaseUrl(urlField.getText());
                 aiClient.setModel((String) modelCombo.getSelectedItem());
                 
                 // Test with a simple prompt
                 String testResponse = aiClient.getAISuggestion("Hello", 5);
                 if (testResponse != null && !testResponse.isEmpty()) {
                     NotificationsHandler.showInfo("Connection successful! AI is responding.");
                 } else {
                     NotificationsHandler.showError("Connection failed. Please check your settings.");
                 }
                 
                 // Restore original settings
                 aiClient.setProvider(originalProvider);
                 aiClient.setApiKey(originalApiKey);
                 aiClient.setBaseUrl(originalUrl);
                 aiClient.setModel(originalModel);
                 
             } catch (Exception ex) {
                 NotificationsHandler.showError("Connection test failed: " + ex.getMessage());
             }
         });
        
        cancelButton.addActionListener(e -> aiDialog.dispose());
        
        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        aiDialog.add(mainPanel, BorderLayout.CENTER);
        aiDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        aiDialog.setVisible(true);
    }
    
    private void openGitHub() {
        try {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start https://github.com/Ghua8088?tab=repositories"});
        } catch (IOException e) {
            NotificationsHandler.showError("Failed to open GitHub");
        }
    }
    
    private void showPreferencesDialog() {
        PreferencesDialog dialog = new PreferencesDialog(notepad);
        dialog.setVisible(true);
    }
    
    private void reopenClosedTab() {
        if (!recentlyClosedEditors.isEmpty()) {
            Editor editor = recentlyClosedEditors.remove(0);
            if (editor != null) {
                String title = editor.getFilePath() != null ? new File(editor.getFilePath()).getName() : "Untitled";
                addTabWithCloseButton(title, editor);
                tabbedPane.setSelectedComponent(editor);
            }
        }
    }
    
    private void closeTab() {
        Editor editor = (Editor) tabbedPane.getSelectedComponent();
        if (editor != null) {
            recentlyClosedEditors.add(editor);
            int closeIdx = tabbedPane.indexOfComponent(editor);
            if (closeIdx != -1) tabbedPane.remove(closeIdx);
        }
    }
    
    private void openTab(int index) {
        if (index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }
    
    public Editor addNewEditorTab(String title) {
        Editor editor = new Editor("");
        themeManager.applyEditorTheme(editor);
        
        // Set up callback to update counts when editor content changes
        editor.setOnCountsChanged(() -> updateCounts(editor));
        
        addTabWithCloseButton(title, editor);
        tabbedPane.setSelectedComponent(editor);
        
        // Initialize counts for the new editor
        updateCounts(editor);
        
        return editor;
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
        closeBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 10));
        closeBtn.addActionListener(e -> closeTab());
        
        tabPanel.add(tabTitle);
        tabPanel.add(closeBtn);
        tabbedPane.setTabComponentAt(idx, tabPanel);
    }
    
    public Editor getCurrentEditor() {
        return (Editor) tabbedPane.getSelectedComponent();
    }
    
    public void updateCounts(Editor editor) {
        if (editor == null) return;
        HashMap<String, String> counts = editor.updatecounts();
        wordCount.setText(counts.get("word_count"));
        characterCount.setText(counts.get("character_count"));
        lineCount.setText(counts.get("line_count"));
        positionCount.setText(counts.get("position_count"));
    }
    
    public void applyThemeToEditor(Editor editor) {
        themeManager.applyEditorTheme(editor);
    }
} 
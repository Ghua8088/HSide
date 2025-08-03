package ide;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PreferencesDialog extends JDialog {
    private final PreferencesManager prefs;
    private final Notepad notepad;
    
    // AI Settings
    private JComboBox<String> aiProviderCombo;
    private JTextField aiModelField;
    private JTextField aiBaseUrlField;
    private JTextField aiApiKeyField;
    private JTextField aiPortField;
    
    // Theme Settings
    private JComboBox<String> themeCombo;
    
    // Layout Settings
    private JCheckBox fileTreeVisibleCheck;
    private JCheckBox terminalVisibleCheck;
    private JCheckBox rightPanelVisibleCheck;
    
    // Font Settings
    private JComboBox<String> fontFamilyCombo;
    private JSpinner fontSizeSpinner;
    private JComboBox<String> fontStyleCombo;
    
    // Window Settings
    private JSpinner windowWidthSpinner;
    private JSpinner windowHeightSpinner;
    private JCheckBox windowMaximizedCheck;
    
    // Editor Settings
    private JSpinner tabSizeSpinner;
    private JCheckBox showLineNumbersCheck;
    private JCheckBox wordWrapCheck;
    private JCheckBox autoSaveCheck;
    private JSpinner autoSaveIntervalSpinner;
    
    // Chat Settings
    private JSpinner chatHistorySizeSpinner;
    private JCheckBox autoScrollChatCheck;
    private JSpinner chatPanelWidthSpinner;
    
    public PreferencesDialog(Notepad notepad) {
        super(notepad, "Preferences", true);
        this.notepad = notepad;
        this.prefs = PreferencesManager.getInstance();
        
        setupDialog();
        loadCurrentPreferences();
    }
    
    private void setupDialog() {
        setSize(600, 700);
        setLocationRelativeTo(notepad);
        setLayout(new BorderLayout());
        
        // Create main panel with scroll pane
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Add preference sections
        mainPanel.add(createAISettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createThemeSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createLayoutSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createFontSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createWindowSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createEditorSettingsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createChatSettingsPanel());
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createAISettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("AI Settings"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // AI Provider
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Provider:"), gbc);
        gbc.gridx = 1;
        aiProviderCombo = new JComboBox<>(new String[]{"Ollama", "OpenAI", "Claude", "Gemini", "Custom"});
        panel.add(aiProviderCombo, gbc);
        
        // AI Model
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1;
        aiModelField = new JTextField(20);
        panel.add(aiModelField, gbc);
        
        // Base URL
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Base URL:"), gbc);
        gbc.gridx = 1;
        aiBaseUrlField = new JTextField(20);
        panel.add(aiBaseUrlField, gbc);
        
        // API Key
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("API Key:"), gbc);
        gbc.gridx = 1;
        aiApiKeyField = new JTextField(20);
        panel.add(aiApiKeyField, gbc);
        
        // Port
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        aiPortField = new JTextField(20);
        panel.add(aiPortField, gbc);
        
        return panel;
    }
    
    private JPanel createThemeSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Theme Settings"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Theme
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        themeCombo = new JComboBox<>(new String[]{"Dark", "Light", "One Dark", "Dracula", "Monokai Pro", "Cyan Light"});
        panel.add(themeCombo, gbc);
        
        return panel;
    }
    
    private JPanel createLayoutSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Layout Settings"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // File Tree Visible
        gbc.gridx = 0; gbc.gridy = 0;
        fileTreeVisibleCheck = new JCheckBox("Show File Tree");
        panel.add(fileTreeVisibleCheck, gbc);
        
        // Terminal Visible
        gbc.gridx = 0; gbc.gridy = 1;
        terminalVisibleCheck = new JCheckBox("Show Terminal");
        panel.add(terminalVisibleCheck, gbc);
        
        // Right Panel Visible
        gbc.gridx = 0; gbc.gridy = 2;
        rightPanelVisibleCheck = new JCheckBox("Show AI Chat Panel");
        panel.add(rightPanelVisibleCheck, gbc);
        
        return panel;
    }
    
    private JPanel createFontSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Font Settings"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Font Family
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Font Family:"), gbc);
        gbc.gridx = 1;
        fontFamilyCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        panel.add(fontFamilyCombo, gbc);
        
        // Font Size
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Font Size:"), gbc);
        gbc.gridx = 1;
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(13, 8, 72, 1));
        panel.add(fontSizeSpinner, gbc);
        
        // Font Style
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Font Style:"), gbc);
        gbc.gridx = 1;
        fontStyleCombo = new JComboBox<>(new String[]{"PLAIN", "BOLD", "ITALIC"});
        panel.add(fontStyleCombo, gbc);
        
        return panel;
    }
    
    private JPanel createWindowSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Window Settings"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Window Width
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Window Width:"), gbc);
        gbc.gridx = 1;
        windowWidthSpinner = new JSpinner(new SpinnerNumberModel(1200, 400, 3000, 50));
        panel.add(windowWidthSpinner, gbc);
        
        // Window Height
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Window Height:"), gbc);
        gbc.gridx = 1;
        windowHeightSpinner = new JSpinner(new SpinnerNumberModel(800, 300, 2000, 50));
        panel.add(windowHeightSpinner, gbc);
        
        // Window Maximized
        gbc.gridx = 0; gbc.gridy = 2;
        windowMaximizedCheck = new JCheckBox("Start Maximized");
        panel.add(windowMaximizedCheck, gbc);
        
        return panel;
    }
    
    private JPanel createEditorSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Editor Settings"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Tab Size
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Tab Size:"), gbc);
        gbc.gridx = 1;
        tabSizeSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 8, 1));
        panel.add(tabSizeSpinner, gbc);
        
        // Show Line Numbers
        gbc.gridx = 0; gbc.gridy = 1;
        showLineNumbersCheck = new JCheckBox("Show Line Numbers");
        panel.add(showLineNumbersCheck, gbc);
        
        // Word Wrap
        gbc.gridx = 0; gbc.gridy = 2;
        wordWrapCheck = new JCheckBox("Word Wrap");
        panel.add(wordWrapCheck, gbc);
        
        // Auto Save
        gbc.gridx = 0; gbc.gridy = 3;
        autoSaveCheck = new JCheckBox("Auto Save");
        panel.add(autoSaveCheck, gbc);
        
        // Auto Save Interval
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Auto Save Interval (ms):"), gbc);
        gbc.gridx = 1;
        autoSaveIntervalSpinner = new JSpinner(new SpinnerNumberModel(30000, 5000, 300000, 5000));
        panel.add(autoSaveIntervalSpinner, gbc);
        
        return panel;
    }
    
    private JPanel createChatSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Chat Settings"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Chat History Size
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Chat History Size:"), gbc);
        gbc.gridx = 1;
        chatHistorySizeSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        panel.add(chatHistorySizeSpinner, gbc);
        
        // Auto Scroll Chat
        gbc.gridx = 0; gbc.gridy = 1;
        autoScrollChatCheck = new JCheckBox("Auto Scroll Chat");
        panel.add(autoScrollChatCheck, gbc);
        
        // Chat Panel Width
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Chat Panel Width (px):"), gbc);
        gbc.gridx = 1;
        chatPanelWidthSpinner = new JSpinner(new SpinnerNumberModel(350, 200, 800, 50));
        panel.add(chatPanelWidthSpinner, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JButton resetButton = new JButton("Reset to Defaults");
        
        saveButton.addActionListener(e -> savePreferences());
        cancelButton.addActionListener(e -> dispose());
        resetButton.addActionListener(e -> resetToDefaults());
        
        panel.add(resetButton);
        panel.add(cancelButton);
        panel.add(saveButton);
        
        return panel;
    }
    
    private void loadCurrentPreferences() {
        // AI Settings
        aiProviderCombo.setSelectedItem(prefs.getAIProvider());
        aiModelField.setText(prefs.getAIModel());
        aiBaseUrlField.setText(prefs.getAIBaseUrl());
        aiApiKeyField.setText(prefs.getAIApiKey());
        aiPortField.setText(prefs.getAIPort());
        
        // Theme Settings
        themeCombo.setSelectedItem(prefs.getTheme());
        
        // Layout Settings
        fileTreeVisibleCheck.setSelected(prefs.isFileTreeVisible());
        terminalVisibleCheck.setSelected(prefs.isTerminalVisible());
        rightPanelVisibleCheck.setSelected(prefs.isRightPanelVisible());
        
        // Font Settings
        fontFamilyCombo.setSelectedItem(prefs.getFontFamily());
        fontSizeSpinner.setValue(prefs.getFontSize());
        fontStyleCombo.setSelectedItem(prefs.getFontStyle());
        
        // Window Settings
        windowWidthSpinner.setValue(prefs.getWindowWidth());
        windowHeightSpinner.setValue(prefs.getWindowHeight());
        windowMaximizedCheck.setSelected(prefs.isWindowMaximized());
        
        // Editor Settings
        tabSizeSpinner.setValue(prefs.getTabSize());
        showLineNumbersCheck.setSelected(prefs.isShowLineNumbers());
        wordWrapCheck.setSelected(prefs.isWordWrap());
        autoSaveCheck.setSelected(prefs.isAutoSave());
        autoSaveIntervalSpinner.setValue(prefs.getAutoSaveInterval());
        
        // Chat Settings
        chatHistorySizeSpinner.setValue(prefs.getChatHistorySize());
        autoScrollChatCheck.setSelected(prefs.isAutoScrollChat());
        chatPanelWidthSpinner.setValue(prefs.getChatPanelWidth());
    }
    
    private void savePreferences() {
        try {
            // AI Settings
            prefs.setAIProvider((String) aiProviderCombo.getSelectedItem());
            prefs.setAIModel(aiModelField.getText());
            prefs.setAIBaseUrl(aiBaseUrlField.getText());
            prefs.setAIApiKey(aiApiKeyField.getText());
            prefs.setAIPort(aiPortField.getText());
            
            // Theme Settings
            String selectedTheme = (String) themeCombo.getSelectedItem();
            prefs.setTheme(selectedTheme);
            ThemeManager.getInstance().applyTheme(selectedTheme);
            
            // Layout Settings
            prefs.setFileTreeVisible(fileTreeVisibleCheck.isSelected());
            prefs.setTerminalVisible(terminalVisibleCheck.isSelected());
            prefs.setRightPanelVisible(rightPanelVisibleCheck.isSelected());
            
            // Font Settings
            prefs.setFontFamily((String) fontFamilyCombo.getSelectedItem());
            prefs.setFontSize((Integer) fontSizeSpinner.getValue());
            prefs.setFontStyle((String) fontStyleCombo.getSelectedItem());
            
            // Window Settings
            prefs.setWindowWidth((Integer) windowWidthSpinner.getValue());
            prefs.setWindowHeight((Integer) windowHeightSpinner.getValue());
            prefs.setWindowMaximized(windowMaximizedCheck.isSelected());
            
            // Editor Settings
            prefs.setTabSize((Integer) tabSizeSpinner.getValue());
            prefs.setShowLineNumbers(showLineNumbersCheck.isSelected());
            prefs.setWordWrap(wordWrapCheck.isSelected());
            prefs.setAutoSave(autoSaveCheck.isSelected());
            prefs.setAutoSaveInterval((Integer) autoSaveIntervalSpinner.getValue());
            
            // Chat Settings
            prefs.setChatHistorySize((Integer) chatHistorySizeSpinner.getValue());
            prefs.setAutoScrollChat(autoScrollChatCheck.isSelected());
            prefs.setChatPanelWidth((Integer) chatPanelWidthSpinner.getValue());
            
            // Update AI Client with new settings
            AIClient aiClient = AIBridge.getInstance();
            aiClient.setProvider(AIClient.Provider.valueOf(prefs.getAIProvider().toUpperCase()));
            aiClient.setModel(prefs.getAIModel());
            aiClient.setBaseUrl(prefs.getAIBaseUrl());
            aiClient.setApiKey(prefs.getAIApiKey());
            aiClient.setPort(prefs.getAIPort());
            
            NotificationsHandler.showInfo("Preferences saved successfully!");
            dispose();
            
        } catch (Exception e) {
            NotificationsHandler.showError("Error saving preferences: " + e.getMessage());
        }
    }
    
    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to reset all preferences to defaults?",
            "Reset Preferences",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            prefs.resetToDefaults();
            loadCurrentPreferences();
            NotificationsHandler.showInfo("Preferences reset to defaults!");
        }
    }
} 
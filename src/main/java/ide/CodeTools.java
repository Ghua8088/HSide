package ide;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * CodeTools provides utilities for code manipulation and automatic linting
 * in the HSide IDE. It includes tools for replacing code in place and
 * integrating with the existing linter.
 */
public class CodeTools {
    private static CodeTools instance;
    private final NotepadManager notepadManager;
    private boolean autoLintEnabled = true;
    private int lintDelay = 1000; // milliseconds
    
    private CodeTools(NotepadManager notepadManager) {
        this.notepadManager = notepadManager;
    }
    
    public static CodeTools getInstance(NotepadManager notepadManager) {
        if (instance == null) {
            instance = new CodeTools(notepadManager);
        }
        return instance;
    }
    
    /**
     * Replace code in the current editor at the specified position
     * @param startPosition Start position for replacement
     * @param endPosition End position for replacement
     * @param newCode New code to insert
     * @return true if replacement was successful
     */
    public boolean replaceCodeInCurrentEditor(int startPosition, int endPosition, String newCode) {
        Editor currentEditor = notepadManager.getCurrentEditor();
        if (currentEditor == null) {
            return false;
        }
        
        try {
            GhostTextPane textArea = currentEditor.getTextArea();
            textArea.getDocument().remove(startPosition, endPosition - startPosition);
            textArea.getDocument().insertString(startPosition, newCode, null);
            
            // Auto-lint after replacement if enabled
            if (autoLintEnabled) {
                scheduleLint(currentEditor);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error replacing code: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Replace code in all open editors
     * @param pattern Pattern to match (regex)
     * @param replacement Replacement text
     * @return Number of replacements made
     */
    public int replaceCodeInAllEditors(String pattern, String replacement) {
        int totalReplacements = 0;
        JTabbedPane tabbedPane = notepadManager.getTabbedPane();
        
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component component = tabbedPane.getComponentAt(i);
            if (component instanceof Editor) {
                Editor editor = (Editor) component;
                int replacements = replaceCodeInEditor(editor, pattern, replacement);
                totalReplacements += replacements;
            }
        }
        
        return totalReplacements;
    }
    
    /**
     * Replace code in a specific editor
     * @param editor The editor to modify
     * @param pattern Pattern to match (regex)
     * @param replacement Replacement text
     * @return Number of replacements made
     */
    public int replaceCodeInEditor(Editor editor, String pattern, String replacement) {
        if (editor == null) return 0;
        
        try {
            GhostTextPane textArea = editor.getTextArea();
            String currentText = textArea.getText();
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(currentText);
            
            int replacements = 0;
            StringBuilder newText = new StringBuilder();
            int lastEnd = 0;
            
            while (matcher.find()) {
                newText.append(currentText, lastEnd, matcher.start());
                newText.append(replacement);
                lastEnd = matcher.end();
                replacements++;
            }
            
            newText.append(currentText, lastEnd, currentText.length());
            
            if (replacements > 0) {
                textArea.setText(newText.toString());
                
                // Auto-lint after replacement if enabled
                if (autoLintEnabled) {
                    scheduleLint(editor);
                }
            }
            
            return replacements;
        } catch (Exception e) {
            System.err.println("Error replacing code in editor: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Insert code at the current caret position in the current editor
     * @param code Code to insert
     * @return true if insertion was successful
     */
    public boolean insertCodeAtCaret(String code) {
        Editor currentEditor = notepadManager.getCurrentEditor();
        if (currentEditor == null) {
            return false;
        }
        
        try {
            GhostTextPane textArea = currentEditor.getTextArea();
            int caretPosition = textArea.getCaretPosition();
            textArea.getDocument().insertString(caretPosition, code, null);
            
            // Auto-lint after insertion if enabled
            if (autoLintEnabled) {
                scheduleLint(currentEditor);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error inserting code: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all open editors
     * @return List of open editors
     */
    public List<Editor> getOpenEditors() {
        List<Editor> editors = new ArrayList<>();
        JTabbedPane tabbedPane = notepadManager.getTabbedPane();
        
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component component = tabbedPane.getComponentAt(i);
            if (component instanceof Editor) {
                editors.add((Editor) component);
            }
        }
        
        return editors;
    }
    
    /**
     * Get the current editor
     * @return Current editor or null if none
     */
    public Editor getCurrentEditor() {
        return notepadManager.getCurrentEditor();
    }
    
    /**
     * Run linting on the current editor
     */
    public void lintCurrentEditor() {
        Editor currentEditor = getCurrentEditor();
        if (currentEditor != null) {
            LinterManager.runCheckstyleAndHighlight(currentEditor);
        }
    }
    
    /**
     * Run linting on all open editors
     */
    public void lintAllEditors() {
        List<Editor> editors = getOpenEditors();
        for (Editor editor : editors) {
            LinterManager.runCheckstyleAndHighlight(editor);
        }
    }
    
    /**
     * Schedule linting with a delay to avoid excessive linting during typing
     * @param editor Editor to lint
     */
    private void scheduleLint(Editor editor) {
        Timer timer = new Timer(lintDelay, e -> {
            LinterManager.runCheckstyleAndHighlight(editor);
            ((Timer) e.getSource()).stop();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Enable or disable automatic linting
     * @param enabled true to enable, false to disable
     */
    public void setAutoLintEnabled(boolean enabled) {
        this.autoLintEnabled = enabled;
    }
    
    /**
     * Check if automatic linting is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isAutoLintEnabled() {
        return autoLintEnabled;
    }
    
    /**
     * Set the delay before auto-linting (in milliseconds)
     * @param delay Delay in milliseconds
     */
    public void setLintDelay(int delay) {
        this.lintDelay = delay;
    }
    
    /**
     * Get the current lint delay
     * @return Delay in milliseconds
     */
    public int getLintDelay() {
        return lintDelay;
    }
    
    /**
     * Show a dialog for code replacement with pattern matching
     */
    public void showReplaceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(notepadManager.getTabbedPane()), 
                                   "Replace Code", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(notepadManager.getTabbedPane());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Pattern field
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Pattern (regex):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        JTextField patternField = new JTextField(30);
        mainPanel.add(patternField, gbc);
        
        // Replacement field
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Replacement:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        JTextField replacementField = new JTextField(30);
        mainPanel.add(replacementField, gbc);
        
        // Scope selection
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Scope:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        String[] scopes = {"Current Editor", "All Open Editors"};
        JComboBox<String> scopeCombo = new JComboBox<>(scopes);
        mainPanel.add(scopeCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton replaceButton = new JButton("Replace");
        JButton cancelButton = new JButton("Cancel");
        
        replaceButton.addActionListener(e -> {
            String pattern = patternField.getText();
            String replacement = replacementField.getText();
            String scope = (String) scopeCombo.getSelectedItem();
            
            if (pattern.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a pattern", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int replacements = 0;
            if ("Current Editor".equals(scope)) {
                Editor currentEditor = getCurrentEditor();
                if (currentEditor != null) {
                    replacements = replaceCodeInEditor(currentEditor, pattern, replacement);
                }
            } else {
                replacements = replaceCodeInAllEditors(pattern, replacement);
            }
            
            JOptionPane.showMessageDialog(dialog, 
                "Replaced " + replacements + " occurrence(s)", 
                "Replace Complete", 
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(replaceButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    /**
     * Show settings dialog for CodeTools
     */
    public void showSettingsDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(notepadManager.getTabbedPane()), 
                                   "Code Tools Settings", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(notepadManager.getTabbedPane());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Auto-lint checkbox
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Auto-lint after changes:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        JCheckBox autoLintCheckBox = new JCheckBox("", autoLintEnabled);
        mainPanel.add(autoLintCheckBox, gbc);
        
        // Lint delay
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Lint delay (ms):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        JSpinner delaySpinner = new JSpinner(new SpinnerNumberModel(lintDelay, 100, 5000, 100));
        mainPanel.add(delaySpinner, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            autoLintEnabled = autoLintCheckBox.isSelected();
            lintDelay = (Integer) delaySpinner.getValue();
            dialog.dispose();
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
} 
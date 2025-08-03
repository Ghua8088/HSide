package ide;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ThemeManager {
    private static ThemeManager instance;
    private boolean isDarkMode = true;
    private final Map<String, ThemeInfo> availableThemes;
    private String currentTheme = "Dark";
    private JFrame mainFrame;
    
    public static class ThemeInfo {
        public final String name;
        public final String className;
        public final boolean isDark;
        
        public ThemeInfo(String name, String className, boolean isDark) {
            this.name = name;
            this.className = className;
            this.isDark = isDark;
        }
    }
    
    private ThemeManager() {
        availableThemes = new HashMap<>();
        initializeThemes();
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public void setMainFrame(JFrame frame) {
        this.mainFrame = frame;
    }
    
    private void initializeThemes() {
        // Dark themes
        availableThemes.put("Dark", new ThemeInfo("Dark", "FlatDarkLaf", true));
        availableThemes.put("One Dark", new ThemeInfo("One Dark", "FlatOneDarkIJTheme", true));
        availableThemes.put("Dracula", new ThemeInfo("Dracula", "FlatDraculaIJTheme", true));
        availableThemes.put("Monokai Pro", new ThemeInfo("Monokai Pro", "FlatMonokaiProIJTheme", true));
        
        // Light themes
        availableThemes.put("Light", new ThemeInfo("Light", "FlatLightLaf", false));
        availableThemes.put("Cyan Light", new ThemeInfo("Cyan Light", "FlatCyanLightIJTheme", false));
    }
    
    public void applyTheme(String themeName) {
        ThemeInfo theme = availableThemes.get(themeName);
        if (theme == null) {
            System.err.println("Theme not found: " + themeName);
            return;
        }
        
        try {
            switch (theme.className) {
                case "FlatDarkLaf":
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
                case "FlatLightLaf":
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;
                case "FlatOneDarkIJTheme":
                    FlatOneDarkIJTheme.setup();
                    break;
                case "FlatDraculaIJTheme":
                    FlatDraculaIJTheme.setup();
                    break;
                case "FlatMonokaiProIJTheme":
                    FlatMonokaiProIJTheme.setup();
                    break;

                case "FlatCyanLightIJTheme":
                    FlatCyanLightIJTheme.setup();
                    break;
                default:
                    System.err.println("Unknown theme: " + theme.className);
                    return;
            }
            
            currentTheme = themeName;
            isDarkMode = theme.isDark;
            
            // Save theme preference
            PreferencesManager.getInstance().setTheme(themeName);
            
            // Update UI components
            if (mainFrame != null) {
                SwingUtilities.updateComponentTreeUI(mainFrame);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to apply theme " + themeName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            applyTheme("Dark");
        } else {
            applyTheme("Light");
        }
    }
    
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    public String getCurrentTheme() {
        return currentTheme;
    }
    
    public Map<String, ThemeInfo> getAvailableThemes() {
        return new HashMap<>(availableThemes);
    }
    
    public void applyEditorTheme(Editor editor) {
        if (editor == null) return;
        
        if (isDarkMode) {
            applyDarkEditorTheme(editor);
        } else {
            applyLightEditorTheme(editor);
        }
    }
    
    private void applyDarkEditorTheme(Editor editor) {
        editor.getTextArea().setBackground(new Color(45, 45, 45));
        editor.getTextArea().setForeground(new Color(190, 190, 190));
        editor.getTextArea().setCaretColor(Color.WHITE);
        editor.getTextArea().setDarkMode(true);
        editor.getTextArea().setCurrentLineHighlightColor(new Color(10, 10, 10, 10));
        
        var gutter = editor.getGutter();
        gutter.setBackground(new Color(30, 30, 30));
        gutter.setLineNumberColor(new Color(0, 255, 239));
        gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 10));
        gutter.setBorderColor(new Color(60, 60, 60));
        gutter.setCurrentLineNumberColor(new Color(50, 255, 239));
    }
    
    private void applyLightEditorTheme(Editor editor) {
        editor.getTextArea().setBackground(new Color(255, 255, 240));
        editor.getTextArea().setForeground(Color.BLACK);
        editor.getTextArea().setCaretColor(Color.BLACK);
        editor.getTextArea().setDarkMode(false);
        editor.getTextArea().setCurrentLineHighlightColor(new Color(255, 255, 200, 80));
        
        var gutter = editor.getGutter();
        gutter.setBackground(new Color(245, 245, 245));
        gutter.setLineNumberColor(new Color(80, 80, 80));
        gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 10));
        gutter.setBorderColor(new Color(200, 200, 200));
        gutter.setCurrentLineNumberColor(new Color(120, 80, 80));
    }
} 
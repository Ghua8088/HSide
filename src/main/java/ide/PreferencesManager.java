package ide;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class PreferencesManager {
    private static PreferencesManager instance;
    private JSONObject preferences;
    private Path preferencesFilePath;
    
    private PreferencesManager() {
        initializePreferencesPath();
        loadPreferences();
    }
    
    private void initializePreferencesPath() {
        try {
            // Try to use user's home directory first
            String userHome = System.getProperty("user.home");
            Path appDataDir = Paths.get(userHome, ".hside");
            
            // Create the directory if it doesn't exist
            if (!Files.exists(appDataDir)) {
                Files.createDirectories(appDataDir);
            }
            
            preferencesFilePath = appDataDir.resolve("hside_preferences.json");
            System.out.println("Preferences will be saved to: " + preferencesFilePath);
        } catch (Exception e) {
            // Fallback to current directory if home directory is not accessible
            System.out.println("Warning: Could not create preferences directory, using current directory");
            preferencesFilePath = Paths.get("hside_preferences.json");
        }
    }
    

    
    public static PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }
        return instance;
    }
    
    private void loadPreferences() {
        try {
            if (Files.exists(preferencesFilePath)) {
                String content = new String(Files.readAllBytes(preferencesFilePath), StandardCharsets.UTF_8);
                preferences = new JSONObject(content);
            } else {
                preferences = new JSONObject();
                setDefaultPreferences();
            }
        } catch (Exception e) {
            System.out.println("Error loading preferences: " + e.getMessage());
            preferences = new JSONObject();
            setDefaultPreferences();
        }
    }
    
    private void setDefaultPreferences() {
        // AI Settings
        preferences.put("aiProvider", "Ollama");
        preferences.put("aiModel", "llama2");
        preferences.put("aiBaseUrl", "http://localhost:11434");
        preferences.put("aiApiKey", "");
        preferences.put("aiPort", "11434");
        
        // Theme Settings
        preferences.put("theme", "Dark");
        
        // Layout Settings
        preferences.put("fileTreeVisible", true);
        preferences.put("terminalVisible", true);
        preferences.put("rightPanelVisible", false);
        
        // Font Settings
        preferences.put("fontFamily", "Segoe UI");
        preferences.put("fontSize", 13);
        preferences.put("fontStyle", "PLAIN");
        
        // Window Settings
        preferences.put("windowWidth", 1200);
        preferences.put("windowHeight", 800);
        preferences.put("windowX", -1);
        preferences.put("windowY", -1);
        preferences.put("windowMaximized", false);
        
        // Editor Settings
        preferences.put("tabSize", 4);
        preferences.put("showLineNumbers", true);
        preferences.put("wordWrap", true);
        preferences.put("autoSave", true);
        preferences.put("autoSaveInterval", 30000); // 30 seconds
        
        // Chat Settings
        preferences.put("chatHistorySize", 100);
        preferences.put("autoScrollChat", true);
        preferences.put("chatPanelWidth", 350);
        
        savePreferences();
    }
    
    public void savePreferences() {
        try {
            String content = preferences.toString(2); // Pretty print with 2 spaces
            Files.write(preferencesFilePath, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Preferences saved successfully to: " + preferencesFilePath);
        } catch (Exception e) {
            System.out.println("Error saving preferences: " + e.getMessage());
        }
    }
    
    // AI Settings
    public void setAIProvider(String provider) {
        preferences.put("aiProvider", provider);
        savePreferences();
    }
    
    public String getAIProvider() {
        return preferences.optString("aiProvider", "Ollama");
    }
    
    public void setAIModel(String model) {
        preferences.put("aiModel", model);
        savePreferences();
    }
    
    public String getAIModel() {
        return preferences.optString("aiModel", "llama2");
    }
    
    public void setAIBaseUrl(String baseUrl) {
        preferences.put("aiBaseUrl", baseUrl);
        savePreferences();
    }
    
    public String getAIBaseUrl() {
        return preferences.optString("aiBaseUrl", "http://localhost:11434");
    }
    
    public void setAIApiKey(String apiKey) {
        preferences.put("aiApiKey", apiKey);
        savePreferences();
    }
    
    public String getAIApiKey() {
        return preferences.optString("aiApiKey", "");
    }
    
    public void setAIPort(String port) {
        preferences.put("aiPort", port);
        savePreferences();
    }
    
    public String getAIPort() {
        return preferences.optString("aiPort", "11434");
    }
    
    // Theme Settings
    public void setTheme(String theme) {
        preferences.put("theme", theme);
        savePreferences();
    }
    
    public String getTheme() {
        return preferences.optString("theme", "Dark");
    }
    
    // Layout Settings
    public void setFileTreeVisible(boolean visible) {
        preferences.put("fileTreeVisible", visible);
        savePreferences();
    }
    
    public boolean isFileTreeVisible() {
        return preferences.optBoolean("fileTreeVisible", true);
    }
    
    public void setTerminalVisible(boolean visible) {
        preferences.put("terminalVisible", visible);
        savePreferences();
    }
    
    public boolean isTerminalVisible() {
        return preferences.optBoolean("terminalVisible", true);
    }
    
    public void setRightPanelVisible(boolean visible) {
        preferences.put("rightPanelVisible", visible);
        savePreferences();
    }
    
    public boolean isRightPanelVisible() {
        return preferences.optBoolean("rightPanelVisible", false);
    }
    
    // Font Settings
    public void setFontFamily(String fontFamily) {
        preferences.put("fontFamily", fontFamily);
        savePreferences();
    }
    
    public String getFontFamily() {
        return preferences.optString("fontFamily", "Segoe UI");
    }
    
    public void setFontSize(int fontSize) {
        preferences.put("fontSize", fontSize);
        savePreferences();
    }
    
    public int getFontSize() {
        return preferences.optInt("fontSize", 13);
    }
    
    public void setFontStyle(String fontStyle) {
        preferences.put("fontStyle", fontStyle);
        savePreferences();
    }
    
    public String getFontStyle() {
        return preferences.optString("fontStyle", "PLAIN");
    }
    
    // Window Settings
    public void setWindowWidth(int width) {
        preferences.put("windowWidth", width);
        savePreferences();
    }
    
    public int getWindowWidth() {
        return preferences.optInt("windowWidth", 1200);
    }
    
    public void setWindowHeight(int height) {
        preferences.put("windowHeight", height);
        savePreferences();
    }
    
    public int getWindowHeight() {
        return preferences.optInt("windowHeight", 800);
    }
    
    public void setWindowX(int x) {
        preferences.put("windowX", x);
        savePreferences();
    }
    
    public int getWindowX() {
        return preferences.optInt("windowX", -1);
    }
    
    public void setWindowY(int y) {
        preferences.put("windowY", y);
        savePreferences();
    }
    
    public int getWindowY() {
        return preferences.optInt("windowY", -1);
    }
    
    public void setWindowMaximized(boolean maximized) {
        preferences.put("windowMaximized", maximized);
        savePreferences();
    }
    
    public boolean isWindowMaximized() {
        return preferences.optBoolean("windowMaximized", false);
    }
    
    // Editor Settings
    public void setTabSize(int tabSize) {
        preferences.put("tabSize", tabSize);
        savePreferences();
    }
    
    public int getTabSize() {
        return preferences.optInt("tabSize", 4);
    }
    
    public void setShowLineNumbers(boolean show) {
        preferences.put("showLineNumbers", show);
        savePreferences();
    }
    
    public boolean isShowLineNumbers() {
        return preferences.optBoolean("showLineNumbers", true);
    }
    
    public void setWordWrap(boolean wrap) {
        preferences.put("wordWrap", wrap);
        savePreferences();
    }
    
    public boolean isWordWrap() {
        return preferences.optBoolean("wordWrap", true);
    }
    
    public void setAutoSave(boolean autoSave) {
        preferences.put("autoSave", autoSave);
        savePreferences();
    }
    
    public boolean isAutoSave() {
        return preferences.optBoolean("autoSave", true);
    }
    
    public void setAutoSaveInterval(int interval) {
        preferences.put("autoSaveInterval", interval);
        savePreferences();
    }
    
    public int getAutoSaveInterval() {
        return preferences.optInt("autoSaveInterval", 30000);
    }
    
    // Chat Settings
    public void setChatHistorySize(int size) {
        preferences.put("chatHistorySize", size);
        savePreferences();
    }
    
    public int getChatHistorySize() {
        return preferences.optInt("chatHistorySize", 100);
    }
    
    public void setAutoScrollChat(boolean autoScroll) {
        preferences.put("autoScrollChat", autoScroll);
        savePreferences();
    }
    
    public boolean isAutoScrollChat() {
        return preferences.optBoolean("autoScrollChat", true);
    }
    
    public void setChatPanelWidth(int width) {
        preferences.put("chatPanelWidth", width);
        savePreferences();
    }
    
    public int getChatPanelWidth() {
        return preferences.optInt("chatPanelWidth", 350);
    }
    
    // Recent Files
    public void addRecentFile(String filePath) {
        JSONArray recentFiles = preferences.optJSONArray("recentFiles");
        if (recentFiles == null) {
            recentFiles = new JSONArray();
        }
        
        // Remove if already exists
        for (int i = 0; i < recentFiles.length(); i++) {
            if (recentFiles.getString(i).equals(filePath)) {
                recentFiles.remove(i);
                break;
            }
        }
        
        // Add to beginning
        recentFiles.put(0, filePath);
        
        // Keep only last 10 files
        while (recentFiles.length() > 10) {
            recentFiles.remove(recentFiles.length() - 1);
        }
        
        preferences.put("recentFiles", recentFiles);
        savePreferences();
    }
    
    public String[] getRecentFiles() {
        JSONArray recentFiles = preferences.optJSONArray("recentFiles");
        if (recentFiles == null) {
            return new String[0];
        }
        
        String[] files = new String[recentFiles.length()];
        for (int i = 0; i < recentFiles.length(); i++) {
            files[i] = recentFiles.getString(i);
        }
        return files;
    }
    
    // Custom Settings (for extensibility)
    public void setCustomSetting(String key, Object value) {
        preferences.put(key, value);
        savePreferences();
    }
    
    public Object getCustomSetting(String key, Object defaultValue) {
        if (preferences.has(key)) {
            return preferences.get(key);
        }
        return defaultValue;
    }
    
    public String getCustomSettingString(String key, String defaultValue) {
        return preferences.optString(key, defaultValue);
    }
    
    public int getCustomSettingInt(String key, int defaultValue) {
        return preferences.optInt(key, defaultValue);
    }
    
    public boolean getCustomSettingBoolean(String key, boolean defaultValue) {
        return preferences.optBoolean(key, defaultValue);
    }
    
    // Reset to defaults
    public void resetToDefaults() {
        preferences = new JSONObject();
        setDefaultPreferences();
        System.out.println("Preferences reset to defaults");
    }
    
    // Export/Import preferences
    public void exportPreferences(String filePath) {
        try {
            String content = preferences.toString(2);
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Preferences exported to: " + filePath);
        } catch (Exception e) {
            System.out.println("Error exporting preferences: " + e.getMessage());
        }
    }
    
    public void importPreferences(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            preferences = new JSONObject(content);
            savePreferences();
            System.out.println("Preferences imported from: " + filePath);
        } catch (Exception e) {
            System.out.println("Error importing preferences: " + e.getMessage());
        }
    }
    
    public String getPreferencesFilePath() {
        return preferencesFilePath.toString();
    }
} 
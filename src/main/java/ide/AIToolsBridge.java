package ide;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;

/**
 * AIToolsBridge provides an API interface for AI tools to interact with
 * the code editors and perform operations like code replacement and linting.
 * This bridge allows AI systems to programmatically manipulate code in the IDE.
 */
public class AIToolsBridge {
    private static AIToolsBridge instance;
    private final CodeTools codeTools;
    
    private AIToolsBridge(CodeTools codeTools) {
        this.codeTools = codeTools;
    }
    
    public static AIToolsBridge getInstance(CodeTools codeTools) {
        if (instance == null) {
            instance = new AIToolsBridge(codeTools);
        }
        return instance;
    }
    
    /**
     * Replace code in the current editor at specified positions
     * @param startPosition Start position for replacement
     * @param endPosition End position for replacement
     * @param newCode New code to insert
     * @return JSON response with success status and details
     */
    public String replaceCodeInCurrentEditor(int startPosition, int endPosition, String newCode) {
        JSONObject response = new JSONObject();
        
        try {
            boolean success = codeTools.replaceCodeInCurrentEditor(startPosition, endPosition, newCode);
            response.put("success", success);
            response.put("operation", "replace_code_in_current_editor");
            response.put("startPosition", startPosition);
            response.put("endPosition", endPosition);
            response.put("newCode", newCode);
            
            if (success) {
                response.put("message", "Code replaced successfully");
            } else {
                response.put("message", "Failed to replace code");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred during code replacement");
        }
        
        return response.toString();
    }
    
    /**
     * Replace code in all open editors using pattern matching
     * @param pattern Pattern to match (regex)
     * @param replacement Replacement text
     * @return JSON response with success status and replacement count
     */
    public String replaceCodeInAllEditors(String pattern, String replacement) {
        JSONObject response = new JSONObject();
        
        try {
            int replacements = codeTools.replaceCodeInAllEditors(pattern, replacement);
            response.put("success", true);
            response.put("operation", "replace_code_in_all_editors");
            response.put("pattern", pattern);
            response.put("replacement", replacement);
            response.put("replacements", replacements);
            response.put("message", "Replaced " + replacements + " occurrence(s)");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred during pattern replacement");
        }
        
        return response.toString();
    }
    
    /**
     * Insert code at the current caret position
     * @param code Code to insert
     * @return JSON response with success status
     */
    public String insertCodeAtCaret(String code) {
        JSONObject response = new JSONObject();
        
        try {
            boolean success = codeTools.insertCodeAtCaret(code);
            response.put("success", success);
            response.put("operation", "insert_code_at_caret");
            response.put("code", code);
            
            if (success) {
                response.put("message", "Code inserted successfully");
            } else {
                response.put("message", "Failed to insert code");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred during code insertion");
        }
        
        return response.toString();
    }
    
    /**
     * Get information about all open editors
     * @return JSON response with editor information
     */
    public String getOpenEditorsInfo() {
        JSONObject response = new JSONObject();
        
        try {
            List<Editor> editors = codeTools.getOpenEditors();
            JSONArray editorsArray = new JSONArray();
            
            for (Editor editor : editors) {
                JSONObject editorInfo = new JSONObject();
                editorInfo.put("filePath", editor.getFilePath());
                editorInfo.put("textLength", editor.getText().length());
                editorInfo.put("lineCount", editor.getText().split("\n").length);
                editorInfo.put("caretPosition", editor.getCaretPosition());
                editorInfo.put("currentLine", editor.getCurrentLine());
                editorInfo.put("currentColumn", editor.getCurrentColumn());
                editorsArray.put(editorInfo);
            }
            
            response.put("success", true);
            response.put("operation", "get_open_editors_info");
            response.put("editors", editorsArray);
            response.put("totalEditors", editors.size());
            response.put("message", "Retrieved information for " + editors.size() + " editor(s)");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred while getting editor information");
        }
        
        return response.toString();
    }
    
    /**
     * Get information about the current editor
     * @return JSON response with current editor information
     */
    public String getCurrentEditorInfo() {
        JSONObject response = new JSONObject();
        
        try {
            Editor currentEditor = codeTools.getCurrentEditor();
            
            if (currentEditor != null) {
                JSONObject editorInfo = new JSONObject();
                editorInfo.put("filePath", currentEditor.getFilePath());
                editorInfo.put("textLength", currentEditor.getText().length());
                editorInfo.put("lineCount", currentEditor.getText().split("\n").length);
                editorInfo.put("caretPosition", currentEditor.getCaretPosition());
                editorInfo.put("currentLine", currentEditor.getCurrentLine());
                editorInfo.put("currentColumn", currentEditor.getCurrentColumn());
                editorInfo.put("text", currentEditor.getText());
                
                response.put("success", true);
                response.put("operation", "get_current_editor_info");
                response.put("editor", editorInfo);
                response.put("message", "Retrieved current editor information");
            } else {
                response.put("success", false);
                response.put("message", "No current editor available");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred while getting current editor information");
        }
        
        return response.toString();
    }
    
    /**
     * Run linting on the current editor
     * @return JSON response with linting status
     */
    public String lintCurrentEditor() {
        JSONObject response = new JSONObject();
        
        try {
            codeTools.lintCurrentEditor();
            response.put("success", true);
            response.put("operation", "lint_current_editor");
            response.put("message", "Linting completed for current editor");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred during linting");
        }
        
        return response.toString();
    }
    
    /**
     * Run linting on all open editors
     * @return JSON response with linting status
     */
    public String lintAllEditors() {
        JSONObject response = new JSONObject();
        
        try {
            codeTools.lintAllEditors();
            response.put("success", true);
            response.put("operation", "lint_all_editors");
            response.put("message", "Linting completed for all editors");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred during linting");
        }
        
        return response.toString();
    }
    
    /**
     * Get CodeTools settings
     * @return JSON response with current settings
     */
    public String getCodeToolsSettings() {
        JSONObject response = new JSONObject();
        
        try {
            response.put("success", true);
            response.put("operation", "get_code_tools_settings");
            response.put("autoLintEnabled", codeTools.isAutoLintEnabled());
            response.put("lintDelay", codeTools.getLintDelay());
            response.put("message", "Retrieved CodeTools settings");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred while getting settings");
        }
        
        return response.toString();
    }
    
    /**
     * Set CodeTools settings
     * @param autoLintEnabled Whether to enable auto-linting
     * @param lintDelay Delay before auto-linting in milliseconds
     * @return JSON response with settings update status
     */
    public String setCodeToolsSettings(boolean autoLintEnabled, int lintDelay) {
        JSONObject response = new JSONObject();
        
        try {
            codeTools.setAutoLintEnabled(autoLintEnabled);
            codeTools.setLintDelay(lintDelay);
            
            response.put("success", true);
            response.put("operation", "set_code_tools_settings");
            response.put("autoLintEnabled", autoLintEnabled);
            response.put("lintDelay", lintDelay);
            response.put("message", "Settings updated successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Exception occurred while updating settings");
        }
        
        return response.toString();
    }
    
    /**
     * Process a JSON command and return the result
     * @param commandJson JSON string containing the command
     * @return JSON response with the result
     */
    public String processCommand(String commandJson) {
        try {
            JSONObject command = new JSONObject(commandJson);
            String operation = command.getString("operation");
            
            switch (operation) {
                case "replace_code_in_current_editor":
                    return replaceCodeInCurrentEditor(
                        command.getInt("startPosition"),
                        command.getInt("endPosition"),
                        command.getString("newCode")
                    );
                    
                case "replace_code_in_all_editors":
                    return replaceCodeInAllEditors(
                        command.getString("pattern"),
                        command.getString("replacement")
                    );
                    
                case "insert_code_at_caret":
                    return insertCodeAtCaret(command.getString("code"));
                    
                case "get_open_editors_info":
                    return getOpenEditorsInfo();
                    
                case "get_current_editor_info":
                    return getCurrentEditorInfo();
                    
                case "lint_current_editor":
                    return lintCurrentEditor();
                    
                case "lint_all_editors":
                    return lintAllEditors();
                    
                case "get_code_tools_settings":
                    return getCodeToolsSettings();
                    
                case "set_code_tools_settings":
                    return setCodeToolsSettings(
                        command.getBoolean("autoLintEnabled"),
                        command.getInt("lintDelay")
                    );
                    
                default:
                    JSONObject errorResponse = new JSONObject();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "Unknown operation: " + operation);
                    errorResponse.put("message", "Unsupported operation");
                    return errorResponse.toString();
            }
        } catch (Exception e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "Exception occurred while processing command");
            return errorResponse.toString();
        }
    }
} 
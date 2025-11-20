package  ide;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
public class SyntaxHelper {
    public static void setSyntaxStyleByExtension(RSyntaxTextArea textArea, String filePath) {
        String extension = getFileExtension(filePath).toLowerCase();

        switch (extension) {
            case "java" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            case "py" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
            case "js" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
            case "html" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            case "css" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
            case "c" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
            case "cpp", "cxx" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
            case "xml" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
            case "json" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            case "sh" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
            case "rb" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_RUBY);
            case "kt", "kts" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_KOTLIN);
            case "go" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GO);
            case "ts" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
            case "php" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
            default -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }
    }
    public static void setSyntaxStyleByName(RSyntaxTextArea textArea, String name) {
        if (textArea == null || name == null) return;
        switch (name.toLowerCase()) {
            case "java" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
            case "python" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
            case "javascript", "js" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
            case "html" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            case "css" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
            case "c" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
            case "c++", "cpp", "cplus" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
            case "xml" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
            case "json" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            case "bash", "sh", "shell" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
            case "ruby" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_RUBY);
            case "kotlin" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_KOTLIN);
            case "go" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GO);
            case "typescript", "ts" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
            case "php" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PHP);
            case "none", "auto", "plain", "text" -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            default -> textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }
    }
    private static String getFileExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        return (dotIndex != -1 && dotIndex < path.length() - 1) ? path.substring(dotIndex + 1) : "";
    }
}
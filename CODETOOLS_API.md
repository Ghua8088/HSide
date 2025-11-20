# CodeTools API Documentation

## Overview

The CodeTools API provides a comprehensive set of tools for manipulating code in the HSide IDE, including code replacement, insertion, and automatic linting. This API is designed to be used by AI systems and can be accessed through the AIToolsBridge.

## Features

### 1. Code Replacement
- Replace code in the current editor at specific positions
- Replace code in all open editors using pattern matching (regex)
- Insert code at the current caret position

### 2. Automatic Linting
- Automatic linting after code changes (configurable delay)
- Manual linting of current editor or all editors
- Integration with existing Checkstyle linter

### 3. Editor Information
- Get information about all open editors
- Get detailed information about the current editor
- Access editor content, position, and metadata

## API Usage

### Basic Usage

The CodeTools API can be accessed through the AIToolsBridge, which provides JSON-based communication:

```java
// Get the CodeTools instance
CodeTools codeTools = notepadManager.getCodeTools();

// Get the AIToolsBridge for API access
AIToolsBridge toolsBridge = AIToolsBridge.getInstance(codeTools);
```

### Code Replacement Examples

#### Replace code in current editor
```java
// Replace code from position 100 to 150 with new code
boolean success = codeTools.replaceCodeInCurrentEditor(100, 150, "new code here");
```

#### Replace code using pattern matching
```java
// Replace all occurrences of "oldMethod" with "newMethod" in all editors
int replacements = codeTools.replaceCodeInAllEditors("oldMethod", "newMethod");

// Replace using regex pattern
int replacements = codeTools.replaceCodeInAllEditors("\\bint\\b", "final int");
```

#### Insert code at caret position
```java
// Insert code at the current cursor position
boolean success = codeTools.insertCodeAtCaret("System.out.println(\"Hello World\");");
```

### Linting Examples

#### Manual linting
```java
// Lint current editor
codeTools.lintCurrentEditor();

// Lint all open editors
codeTools.lintAllEditors();
```

#### Auto-linting configuration
```java
// Enable/disable auto-linting
codeTools.setAutoLintEnabled(true);

// Set lint delay (in milliseconds)
codeTools.setLintDelay(2000); // 2 seconds
```

### Editor Information

#### Get current editor info
```java
Editor currentEditor = codeTools.getCurrentEditor();
if (currentEditor != null) {
    String filePath = currentEditor.getFilePath();
    String text = currentEditor.getText();
    int caretPosition = currentEditor.getCaretPosition();
    int currentLine = currentEditor.getCurrentLine();
    int currentColumn = currentEditor.getCurrentColumn();
}
```

#### Get all open editors
```java
List<Editor> editors = codeTools.getOpenEditors();
for (Editor editor : editors) {
    System.out.println("Editor: " + editor.getFilePath());
}
```

## JSON API Commands

The AIToolsBridge provides a JSON-based API for external tools:

### Replace code in current editor
```json
{
  "operation": "replace_code_in_current_editor",
  "startPosition": 100,
  "endPosition": 150,
  "newCode": "new code here"
}
```

### Replace code in all editors
```json
{
  "operation": "replace_code_in_all_editors",
  "pattern": "oldMethod",
  "replacement": "newMethod"
}
```

### Insert code at caret
```json
{
  "operation": "insert_code_at_caret",
  "code": "System.out.println(\"Hello World\");"
}
```

### Get editor information
```json
{
  "operation": "get_current_editor_info"
}
```

### Lint current editor
```json
{
  "operation": "lint_current_editor"
}
```

### Get CodeTools settings
```json
{
  "operation": "get_code_tools_settings"
}
```

### Set CodeTools settings
```json
{
  "operation": "set_code_tools_settings",
  "autoLintEnabled": true,
  "lintDelay": 1000
}
```

## UI Integration

The CodeTools functionality is integrated into the HSide IDE through the AI menu:

- **Code Replace**: Opens a dialog for pattern-based code replacement
- **Code Tools Settings**: Configure auto-linting and other settings
- **Lint Current Editor**: Manually lint the current editor
- **Lint All Editors**: Lint all open editors

## Automatic Linting

The automatic linting feature works as follows:

1. **Trigger**: Text changes in any editor (insert/delete)
2. **Delay**: Configurable delay (default: 1000ms) to avoid excessive linting during typing
3. **Execution**: Runs the Checkstyle linter on the modified editor
4. **Display**: Shows linting errors with red underlines and gutter icons

### Configuration

Auto-linting can be configured through:
- The Code Tools Settings dialog
- Programmatically via the API
- JSON commands through AIToolsBridge

## Error Handling

All API methods include proper error handling:

- **Success responses**: Include operation details and success status
- **Error responses**: Include error messages and exception details
- **Graceful degradation**: Failed operations don't crash the IDE

## Integration with AI

The CodeTools API is designed to work seamlessly with AI systems:

1. **Tool calls**: AI responses can include tool calls in JSON format
2. **Automatic processing**: Tool calls are automatically processed and results are included in responses
3. **Context awareness**: Tools have access to current editor state and content

### Example AI Tool Call
```
<tool_call>
{
  "operation": "replace_code_in_current_editor",
  "startPosition": 100,
  "endPosition": 150,
  "newCode": "// Fixed method implementation\npublic void fixedMethod() {\n    // Implementation here\n}"
}
</tool_call>
```

## Performance Considerations

- **Debounced linting**: Auto-linting uses a timer to avoid excessive processing
- **Efficient replacements**: Pattern matching is optimized for large files
- **Memory management**: Large operations are handled efficiently
- **Background processing**: Linting runs in background threads

## Future Enhancements

Potential future features:
- Support for more linting tools (ESLint, Pylint, etc.)
- Code formatting tools
- Refactoring operations
- Code analysis and suggestions
- Integration with version control systems 
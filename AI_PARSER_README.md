# AI Client Parser - Smart Code Insertion

This document describes the enhanced AI Client parser that intelligently determines where to insert AI-generated code suggestions.

## Overview

The new parser in `AIClient.java` analyzes the context and suggestion content to determine the optimal insertion point, ensuring that code is inserted at the correct location rather than always at the cursor position.

## Key Features

### 1. Smart Position Detection
- **Complete Methods/Classes**: Automatically finds the appropriate location within a class to insert complete methods
- **Complete Blocks**: Handles if/for/while/try-catch blocks intelligently
- **Simple Continuations**: Maintains the original behavior for simple text completions
- **Line-based Insertions**: Can insert suggestions at specific line numbers

### 2. Context Analysis
The parser analyzes:
- Current code structure
- Suggestion type (method, class, block, or simple continuation)
- Current line content
- Surrounding code context

### 3. Insertion Strategies
- **At Cursor**: For simple continuations
- **End of Line**: For block structures that should start on a new line
- **Before Closing Brace**: For complete methods/classes within existing structures
- **Specific Line**: For targeted insertions

## API Methods

### AIClient Class

#### `getAISuggestionWithPosition(String context, int currentPosition)`
Returns a `SuggestionResult` with the suggestion and optimal insertion position.

```java
AIClient aiClient = new AIClient();
String context = "public class Test {\n    // existing code\n}";
int position = context.length() - 1;
AIClient.SuggestionResult result = aiClient.getAISuggestionWithPosition(context, position);
```

#### `getAISuggestionForLine(String context, int lineNumber)`
Returns a `SuggestionResult` for insertion at a specific line number.

```java
AIClient.SuggestionResult result = aiClient.getAISuggestionForLine(context, 5);
```

### Editor Class

#### `getAISuggestionWithPosition(int position)`
Gets AI suggestion for the current editor content at a specific position.

```java
Editor editor = getCurrentEditor();
AIClient.SuggestionResult result = editor.getAISuggestionWithPosition(cursorPosition);
```

#### `insertAISuggestion(AIClient.SuggestionResult result)`
Inserts a suggestion at the smart position.

```java
editor.insertAISuggestion(result);
```

#### `getAISuggestionForLine(int lineNumber)`
Gets AI suggestion for a specific line.

```java
AIClient.SuggestionResult result = editor.getAISuggestionForLine(10);
```

#### `insertAISuggestionAtLine(int lineNumber)`
Inserts AI suggestion at a specific line.

```java
editor.insertAISuggestionAtLine(10);
```

## SuggestionResult Class

```java
public static class SuggestionResult {
    public final String suggestion;           // The suggested code
    public final int insertionPosition;       // Where to insert it
    public final boolean shouldInsertNewLine; // Whether to add a newline
}
```

## Usage Examples

### 1. Simple Continuation
```java
// Context: "String str = \"Hello"
// Result: Insertion at current position, no newline
```

### 2. Complete Method
```java
// Context: "public class Test {\n    // existing code\n}"
// Result: Insertion before closing brace, with newline
```

### 3. Block Structure
```java
// Context: "if (condition) {"
// Result: Insertion at end of line, with newline
```

### 4. Line-based Insertion
```java
// Context: "public class Test {\n    // line 1\n    // line 2\n}"
// Line: 2
// Result: Insertion at beginning of line 2
```

## Integration with UI

The parser is integrated into the existing Ctrl+Space and Tab functionality:

1. **Ctrl+Space**: Gets suggestion with smart position detection
2. **Tab**: Inserts suggestion at the determined position
3. **Escape/typing**: Clears suggestion

## Testing

Run the test file to see examples:
```bash
javac -cp "lib/*:src" src/test/java/AIClientParserTest.java
java -cp "lib/*:src" AIClientParserTest
```

## Benefits

1. **Better Code Organization**: Suggestions are placed in logical locations
2. **Improved User Experience**: Less manual positioning required
3. **Context Awareness**: Understands code structure and syntax
4. **Flexible Insertion**: Supports multiple insertion strategies
5. **Backward Compatibility**: Maintains existing functionality

## Future Enhancements

- Support for more programming languages
- Enhanced context analysis for complex code structures
- Integration with code formatting tools
- Support for multi-line selections
- Context menu integration for right-click suggestions 
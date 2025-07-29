package ide;
public class PromptBuilder {
    private static String trimLines(String code, int maxLines) {
        String[] lines = code.split("\\R");
        StringBuilder out = new StringBuilder();
        int start = Math.max(0, lines.length - maxLines);
        for (int i = start; i < lines.length; i++) {
            out.append(lines[i]).append("\n");
        }
        return out.toString();
    }
    public static String autocompletePrompt(String before, String after) {
        String trimmedBefore = trimLines(before, 10);  
        String trimmedAfter = trimLines(after, 5);     
        return """
            You are a coding autocomplete assistant.

            Your task is to predict what the user is likely to type **next** at the caret position.

            ⚠️ DO NOT repeat existing code.
            ⚠️ DO NOT regenerate full classes or methods.
            ✅ Only suggest what comes immediately after the caret — a few lines or a natural next block.
            ✅ Do not return import statements, class headers, or duplicate code.
            ✅ Format output as a plain code fragment (NO explanations, no markdown).
            --- CODE BEFORE CARET ---
            %s
            --- CURSOR HERE ---
            --- CODE AFTER CARET ---
            %s
            ✨ Suggest 1–5 lines of code that should come next, cleanly indented:
            """.formatted(trimmedBefore.trim(), trimmedAfter.trim());
    }
}
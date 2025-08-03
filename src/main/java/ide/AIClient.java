package ide;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AIClient {
    public enum Provider {
        OLLAMA("Ollama"),
        OPENAI("OpenAI"),
        CLAUDE("Claude"),
        GEMINI("Gemini"),
        CUSTOM("Custom");
        
        private final String displayName;
        
        Provider(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    private Provider currentProvider = Provider.OLLAMA;
    private String apiKey = "";
    private String baseUrl = "";
    private String model = "";
    private String port = "11434";
    
    // Provider-specific configurations
    private final Map<Provider, String> defaultUrls = new HashMap<>();
    private final Map<Provider, String[]> defaultModels = new HashMap<>();
    
    public AIClient() {
        initializeProviderDefaults();
        loadPreferences();
        setupOllama();
    }
    
    private void initializeProviderDefaults() {
        // Default URLs for each provider
        defaultUrls.put(Provider.OLLAMA, "http://localhost:11434");
        defaultUrls.put(Provider.OPENAI, "https://api.openai.com/v1");
        defaultUrls.put(Provider.CLAUDE, "https://api.anthropic.com/v1");
        defaultUrls.put(Provider.GEMINI, "https://generativelanguage.googleapis.com/v1beta");
        defaultUrls.put(Provider.CUSTOM, "");
        
        // Default models for each provider
        defaultModels.put(Provider.OLLAMA, new String[]{"llama2", "codellama", "mistral", "neural-chat"});
        defaultModels.put(Provider.OPENAI, new String[]{"gpt-4", "gpt-4-turbo", "gpt-3.5-turbo", "gpt-3.5-turbo-16k"});
        defaultModels.put(Provider.CLAUDE, new String[]{"claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307"});
        defaultModels.put(Provider.GEMINI, new String[]{"gemini-pro", "gemini-pro-vision"});
        defaultModels.put(Provider.CUSTOM, new String[]{});
        
        // Set initial values
        this.baseUrl = defaultUrls.get(Provider.OLLAMA);
        this.model = defaultModels.get(Provider.OLLAMA)[0];
    }
    
    private void loadPreferences() {
        PreferencesManager prefs = PreferencesManager.getInstance();
        
        // Load AI settings from preferences
        String providerName = prefs.getAIProvider();
        try {
            this.currentProvider = Provider.valueOf(providerName.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.currentProvider = Provider.OLLAMA; // Default fallback
        }
        
        this.model = prefs.getAIModel();
        this.baseUrl = prefs.getAIBaseUrl();
        this.apiKey = prefs.getAIApiKey();
        this.port = prefs.getAIPort();
        
        System.out.println("Loaded AI preferences: " + providerName + ", " + model + ", " + baseUrl);
    }
    
    private void setupOllama() {
        if (currentProvider == Provider.OLLAMA && !isOllamaRunning()) {
            try {
                ProcessBuilder pb = new ProcessBuilder("ollama", "serve");
                pb.redirectErrorStream(true);
                pb.start();
                System.out.println("Started Ollama.");
            } catch (IOException e) {
                System.out.println("Error starting Ollama: " + e.getMessage());
            }
        }
    }
    
    private boolean isOllamaRunning() {
        try {
            URI uri = URI.create(this.baseUrl + "/api/tags");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean isModelAvailable(String modelName) {
        try {
            String[] availableModels = getOllamaModels();
            for (String model : availableModels) {
                if (model.equals(modelName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Provider management
    public void setProvider(Provider provider) {
        this.currentProvider = provider;
        this.baseUrl = defaultUrls.get(provider);
        String[] models = defaultModels.get(provider);
        if (models.length > 0) {
            this.model = models[0];
        }
        
        // Save to preferences
        PreferencesManager.getInstance().setAIProvider(provider.toString());
    }
    
    public Provider getCurrentProvider() {
        return currentProvider;
    }
    
    public Provider[] getAvailableProviders() {
        return Provider.values();
    }
    
    // Configuration methods
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        PreferencesManager.getInstance().setAIApiKey(apiKey);
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        PreferencesManager.getInstance().setAIBaseUrl(baseUrl);
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setModel(String model) {
        this.model = model;
        PreferencesManager.getInstance().setAIModel(model);
    }
    
    public String getModel() {
        return model;
    }
    
    public void setPort(String port) {
        this.port = port;
        PreferencesManager.getInstance().setAIPort(port);
    }
    
    public String getPort() {
        return port;
    }
    
    // Main AI suggestion method for code completion
    public String getAISuggestion(String context, int caretPosition) {
        if (context == null || context.isEmpty()) {
            return "";
        }

        try {
            System.out.println("Getting Suggestion from " + currentProvider + "...");
            String beforeCaret = context.substring(0, caretPosition);
            String afterCaret = context.substring(caretPosition);
            String prompt = PromptBuilder.autocompletePrompt(afterCaret, beforeCaret);
            
            switch (currentProvider) {
                case OLLAMA:
                    return getOllamaSuggestion(prompt);
                case OPENAI:
                    return getOpenAISuggestion(prompt);
                case CLAUDE:
                    return getClaudeSuggestion(prompt);
                case GEMINI:
                    return getGeminiSuggestion(prompt);
                case CUSTOM:
                    return getCustomSuggestion(prompt);
                default:
                    return "";
            }
        } catch (Exception ex) {
            System.out.println("AI Suggestion Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return "";
    }
    
    // Chat method for normal conversation
    public String getChatResponse(String message) {
        return getChatResponse(message, null);
    }
    
    // Chat method with context attachment
    public String getChatResponse(String message, String context) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        try {
            System.out.println("Getting Chat Response from " + currentProvider + "...");
            
            // Check if Ollama is running for Ollama provider
            if (currentProvider == Provider.OLLAMA) {
                if (!isOllamaRunning()) {
                    return "Ollama is not running. Please start Ollama first or check your connection.";
                }
                if (!isModelAvailable(this.model)) {
                    return "Model '" + this.model + "' is not available. Please check your AI settings and select an available model.";
                }
            }
            
            switch (currentProvider) {
                case OLLAMA:
                    return getOllamaChatResponse(message, context);
                case OPENAI:
                    return getOpenAIChatResponse(message, context);
                case CLAUDE:
                    return getClaudeChatResponse(message, context);
                case GEMINI:
                    return getGeminiChatResponse(message, context);
                case CUSTOM:
                    return getCustomChatResponse(message, context);
                default:
                    return "";
            }
        } catch (Exception ex) {
            System.out.println("AI Chat Error: " + ex.getMessage());
            ex.printStackTrace();
            return "Error: " + ex.getMessage() + ". Please check your AI settings and connection.";
        }
    }
    
    // Streaming chat method with callback
    public void getStreamingChatResponse(String message, String context, ChatStreamCallback callback) {
        if (message == null || message.isEmpty()) {
            callback.onError("Empty message");
            return;
        }

        new Thread(() -> {
            try {
                System.out.println("Getting Streaming Chat Response from " + currentProvider + "...");
                
                // Check if Ollama is running for Ollama provider
                if (currentProvider == Provider.OLLAMA) {
                    if (!isOllamaRunning()) {
                        callback.onError("Ollama is not running. Please start Ollama first or check your connection.");
                        return;
                    }
                    if (!isModelAvailable(this.model)) {
                        callback.onError("Model '" + this.model + "' is not available. Please check your AI settings and select an available model.");
                        return;
                    }
                }
                
                switch (currentProvider) {
                    case OLLAMA:
                        getOllamaStreamingChatResponse(message, context, callback);
                        break;
                    case OPENAI:
                        getOpenAIStreamingChatResponse(message, context, callback);
                        break;
                    case CLAUDE:
                        getClaudeStreamingChatResponse(message, context, callback);
                        break;
                    case GEMINI:
                        getGeminiStreamingChatResponse(message, context, callback);
                        break;
                    case CUSTOM:
                        getCustomStreamingChatResponse(message, context, callback);
                        break;
                    default:
                        callback.onError("Unsupported provider");
                }
            } catch (Exception ex) {
                System.out.println("AI Chat Error: " + ex.getMessage());
                ex.printStackTrace();
                callback.onError("Error: " + ex.getMessage() + ". Please check your AI settings and connection.");
            }
        }).start();
    }
    
    // Callback interface for streaming
    public interface ChatStreamCallback {
        void onChunk(String chunk);
        void onThink(String thinkBlock); // Legacy method for backward compatibility
        void onThinkChunk(String thinkChunk); // New method for streaming think blocks
        void onThinkComplete(); // Called when think block is complete
        void onComplete();
        void onError(String error);
    }
    
    // Provider-specific implementation methods
    private String getOllamaSuggestion(String prompt) throws Exception {
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("prompt", prompt)
            .put("stream", false);
        
        String endpoint = this.baseUrl + "/api/generate";
        return makeRequest(endpoint, payload, null);
    }
    
    private String getOpenAISuggestion(String prompt) throws Exception {
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)))
            .put("max_tokens", 1000)
            .put("temperature", 0.7);
        
        String endpoint = this.baseUrl + "/chat/completions";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.apiKey);
        
        String response = makeRequest(endpoint, payload, headers);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("choices")
                  .getJSONObject(0)
                  .getJSONObject("message")
                  .getString("content");
    }
    
    private String getClaudeSuggestion(String prompt) throws Exception {
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("max_tokens", 1000)
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)));
        
        String endpoint = this.baseUrl + "/messages";
        Map<String, String> headers = new HashMap<>();
        headers.put("x-api-key", this.apiKey);
        headers.put("anthropic-version", "2023-06-01");
        
        String response = makeRequest(endpoint, payload, headers);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("content")
                  .getJSONObject(0)
                  .getString("text");
    }
    
    private String getGeminiSuggestion(String prompt) throws Exception {
        JSONObject payload = new JSONObject()
            .put("contents", new JSONArray()
                .put(new JSONObject()
                    .put("parts", new JSONArray()
                        .put(new JSONObject()
                            .put("text", prompt)))));
        
        String endpoint = this.baseUrl + "/models/" + this.model + ":generateContent?key=" + this.apiKey;
        
        String response = makeRequest(endpoint, payload, null);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("candidates")
                  .getJSONObject(0)
                  .getJSONObject("content")
                  .getJSONArray("parts")
                  .getJSONObject(0)
                  .getString("text");
    }
    
    private String getCustomSuggestion(String prompt) throws Exception {
        // For custom providers, use a generic approach
        JSONObject payload = new JSONObject()
            .put("prompt", prompt)
            .put("model", this.model);
        
        String endpoint = this.baseUrl;
        Map<String, String> headers = new HashMap<>();
        if (!this.apiKey.isEmpty()) {
            headers.put("Authorization", "Bearer " + this.apiKey);
        }
        
        return makeRequest(endpoint, payload, headers);
    }
    
    // Generic HTTP request method
    private String makeRequest(String endpoint, JSONObject payload, Map<String, String> headers) throws Exception {
        URI uri = URI.create(endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        
        // Add custom headers
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }
        
        // Check response code
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode + ": " + conn.getResponseMessage());
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }
        
        String fullResponse = response.toString();
        System.out.println("Full Response:\n" + fullResponse);
        
        // Extract response based on provider
        if (currentProvider == Provider.OLLAMA) {
            JSONObject json = new JSONObject(fullResponse);
            fullResponse = json.getString("response").trim();
        }
        
        fullResponse = fullResponse.replaceAll("(?s)<think>.*?</think>", "").trim();
        
        // Only extract code blocks for code completion, not for chat
        if (endpoint.contains("/api/generate") && payload.getString("prompt").contains("autocomplete")) {
            String suggestion = extractCodeBlock(fullResponse);
            System.out.println("Suggestion:\n" + suggestion);
            return suggestion;
        } else {
            // For chat responses, return the full response
            System.out.println("Chat Response:\n" + fullResponse);
            return fullResponse;
        }
    }
    
    // Get available models for current provider
    public String[] getAvailableModels() {
        try {
            switch (currentProvider) {
                case OLLAMA:
                    return getOllamaModels();
                case OPENAI:
                    return defaultModels.get(Provider.OPENAI);
                case CLAUDE:
                    return defaultModels.get(Provider.CLAUDE);
                case GEMINI:
                    return defaultModels.get(Provider.GEMINI);
                case CUSTOM:
                    return new String[]{};
                default:
                    return new String[]{};
            }
        } catch (Exception ex) {
            System.out.println("Error getting available models: " + ex.getMessage());
            ex.printStackTrace();
        }
        return new String[]{};
    }
    
    private String[] getOllamaModels() throws Exception {
        URI uri = URI.create(this.baseUrl + "/api/tags");
        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("GET");
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        JSONObject json = new JSONObject(response.toString());
        JSONArray models = json.getJSONArray("models");
        String[] names = new String[models.length()];
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            names[i] = model.getString("name");
        }
        return names;
    }
    
    // Utility method for extracting code blocks
    public static String extractCodeBlock(String response) {
        String codeBlockRegex = "(?s)```(?:[a-zA-Z]*\\n)?(.*?)```";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(codeBlockRegex);
        java.util.regex.Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return response.trim();
    }
    
    // Chat response methods for each provider
    private String getOllamaChatResponse(String message, String context) throws Exception {
        String prompt = "You are a helpful AI assistant. Provide a clear, concise response to: " + message + "\n\nImportant: Do not repeat yourself. Give a single, direct answer dont use emojis.";
        if (context != null && !context.trim().isEmpty()) {
            prompt = "Context:\n" + context + "\n\nUser message: " + message + "\n\nProvide a clear, concise response considering the context. Do not repeat yourself.";
        }
        
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("prompt", prompt)
            .put("stream", false)
            .put("temperature", 0.7)
            .put("top_p", 0.9);
        
        String endpoint = this.baseUrl + "/api/generate";
        return makeRequest(endpoint, payload, null);
    }
    
    private String getOpenAIChatResponse(String message, String context) throws Exception {
        String content = message;
        if (context != null && !context.trim().isEmpty()) {
            content = "Context:\n" + context + "\n\nUser message: " + message;
        }
        
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a helpful AI assistant. Respond naturally and conversationally."))
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", content)))
            .put("max_tokens", 1000)
            .put("temperature", 0.7);
        
        String endpoint = this.baseUrl + "/chat/completions";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.apiKey);
        
        String response = makeRequest(endpoint, payload, headers);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("choices")
                  .getJSONObject(0)
                  .getJSONObject("message")
                  .getString("content");
    }
    
    private String getClaudeChatResponse(String message, String context) throws Exception {
        String content = "You are a helpful AI assistant. Respond naturally to: " + message;
        if (context != null && !context.trim().isEmpty()) {
            content = "Context:\n" + context + "\n\nUser message: " + message + "\n\nPlease respond naturally, considering the provided context.";
        }
        
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("max_tokens", 1000)
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", content)));
        
        String endpoint = this.baseUrl + "/messages";
        Map<String, String> headers = new HashMap<>();
        headers.put("x-api-key", this.apiKey);
        headers.put("anthropic-version", "2023-06-01");
        
        String response = makeRequest(endpoint, payload, headers);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("content")
                  .getJSONObject(0)
                  .getString("text");
    }
    
    private String getGeminiChatResponse(String message, String context) throws Exception {
        String text = "You are a helpful AI assistant. Respond naturally to: " + message;
        if (context != null && !context.trim().isEmpty()) {
            text = "Context:\n" + context + "\n\nUser message: " + message + "\n\nPlease respond naturally, considering the provided context.";
        }
        
        JSONObject payload = new JSONObject()
            .put("contents", new JSONArray()
                .put(new JSONObject()
                    .put("parts", new JSONArray()
                        .put(new JSONObject()
                            .put("text", text)))));
        
        String endpoint = this.baseUrl + "/models/" + this.model + ":generateContent?key=" + this.apiKey;
        String response = makeRequest(endpoint, payload, null);
        JSONObject json = new JSONObject(response);
        return json.getJSONArray("candidates")
                  .getJSONObject(0)
                  .getJSONObject("content")
                  .getJSONArray("parts")
                  .getJSONObject(0)
                  .getString("text");
    }
    
    private String getCustomChatResponse(String message, String context) throws Exception {
        JSONObject payload = new JSONObject()
            .put("message", message)
            .put("max_tokens", 1000);
        
        if (context != null && !context.trim().isEmpty()) {
            payload.put("context", context);
        }
        
        String endpoint = this.baseUrl;
        Map<String, String> headers = new HashMap<>();
        if (this.apiKey != null && !this.apiKey.isEmpty()) {
            headers.put("Authorization", "Bearer " + this.apiKey);
        }
        
        return makeRequest(endpoint, payload, headers);
    }
    
    // Streaming implementations
    private void getOllamaStreamingChatResponse(String message, String context, ChatStreamCallback callback) throws Exception {
        String prompt = "You are a helpful AI assistant. Provide a clear, concise response to: " + message + "\n\nImportant: Do not repeat yourself. Give a single, direct answer.";
        
        if (context != null && !context.trim().isEmpty()) {
            prompt = "Context:\n" + context + "\n\nUser message: " + message + "\n\nProvide a clear, concise response considering the context. Do not repeat yourself.";
        }
        
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("prompt", prompt)
            .put("stream", true)
            .put("temperature", 0.7)
            .put("top_p", 0.9);
        
        String endpoint = this.baseUrl + "/api/generate";
        makeStreamingRequest(endpoint, payload, null, callback);
    }
    
    private void getOpenAIStreamingChatResponse(String message, String context, ChatStreamCallback callback) throws Exception {
        String content = message;
        if (context != null && !context.trim().isEmpty()) {
            content = "Context:\n" + context + "\n\nUser message: " + message;
        }
        
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a helpful AI assistant. Respond naturally and conversationally."))
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", content)))
            .put("max_tokens", 1000)
            .put("temperature", 0.7)
            .put("stream", true);
        
        String endpoint = this.baseUrl + "/chat/completions";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.apiKey);
        
        makeStreamingRequest(endpoint, payload, headers, callback);
    }
    
    private void getClaudeStreamingChatResponse(String message, String context, ChatStreamCallback callback) throws Exception {
        String content = "You are a helpful AI assistant. Respond naturally to: " + message;
        if (context != null && !context.trim().isEmpty()) {
            content = "Context:\n" + context + "\n\nUser message: " + message + "\n\nPlease respond naturally, considering the provided context.";
        }
        
        JSONObject payload = new JSONObject()
            .put("model", this.model)
            .put("max_tokens", 1000)
            .put("messages", new JSONArray()
                .put(new JSONObject()
                    .put("role", "user")
                    .put("content", content)))
            .put("stream", true);
        
        String endpoint = this.baseUrl + "/messages";
        Map<String, String> headers = new HashMap<>();
        headers.put("x-api-key", this.apiKey);
        headers.put("anthropic-version", "2023-06-01");
        
        makeStreamingRequest(endpoint, payload, headers, callback);
    }
    
    private void getGeminiStreamingChatResponse(String message, String context, ChatStreamCallback callback) throws Exception {
        String text = "You are a helpful AI assistant. Respond naturally to: " + message;
        if (context != null && !context.trim().isEmpty()) {
            text = "Context:\n" + context + "\n\nUser message: " + message + "\n\nPlease respond naturally, considering the provided context.";
        }
        
        JSONObject payload = new JSONObject()
            .put("contents", new JSONArray()
                .put(new JSONObject()
                    .put("parts", new JSONArray()
                        .put(new JSONObject()
                            .put("text", text)))));
        
        String endpoint = this.baseUrl + "/models/" + this.model + ":streamGenerateContent?key=" + this.apiKey;
        makeStreamingRequest(endpoint, payload, null, callback);
    }
    
    private void getCustomStreamingChatResponse(String message, String context, ChatStreamCallback callback) throws Exception {
        JSONObject payload = new JSONObject()
            .put("message", message)
            .put("max_tokens", 1000)
            .put("stream", true);
        
        if (context != null && !context.trim().isEmpty()) {
            payload.put("context", context);
        }
        
        String endpoint = this.baseUrl;
        Map<String, String> headers = new HashMap<>();
        if (this.apiKey != null && !this.apiKey.isEmpty()) {
            headers.put("Authorization", "Bearer " + this.apiKey);
        }
        
        makeStreamingRequest(endpoint, payload, headers, callback);
    }
    
    // Generic streaming request method
    private void makeStreamingRequest(String endpoint, JSONObject payload, Map<String, String> headers, ChatStreamCallback callback) throws Exception {
        URI uri = URI.create(endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        
        // Add custom headers
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }
        
        // Check response code
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP " + responseCode + ": " + conn.getResponseMessage());
        }
        
        StringBuilder thinkBlock = new StringBuilder();
        boolean inThinkBlock = false;
        int chunkCount = 0;
        final int MAX_CHUNKS = 100000000; // Prevent infinite loops
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null && chunkCount < MAX_CHUNKS) {
                if (line.trim().isEmpty()) continue;
                
                // Handle Ollama streaming format
                if (currentProvider == Provider.OLLAMA) {
                    try {
                        JSONObject json = new JSONObject(line);
                        String chunk = json.optString("response", "");
                        boolean done = json.optBoolean("done", false);
                        
                        if (done) {
                            System.out.println("Streaming completed after " + chunkCount + " chunks");
                            break;
                        }
                        
                        if (!chunk.isEmpty()) {
                            chunkCount++;
                            
                            if (chunk.contains("<think>")) {
                                inThinkBlock = true;
                                // Extract the content after <think> tag
                                String thinkContent = chunk.substring(chunk.indexOf("<think>") + 7);
                                if (!thinkContent.isEmpty()) {
                                    callback.onThinkChunk(thinkContent);
                                }
                            } else if (chunk.contains("</think>")) {
                                inThinkBlock = false;
                                // Extract the content before </think> tag
                                String thinkContent = chunk.substring(0, chunk.indexOf("</think>"));
                                if (!thinkContent.isEmpty()) {
                                    callback.onThinkChunk(thinkContent);
                                }
                                callback.onThinkComplete();
                            } else if (inThinkBlock) {
                                callback.onThinkChunk(chunk);
                            } else {
                                callback.onChunk(chunk);
                            }
                        }
                    } catch (Exception e) {
                        // Skip malformed JSON
                        System.out.println("Skipping malformed JSON: " + line);
                    }
                } else {
                    // Handle other providers (simplified for now)
                    callback.onChunk(line);
                }
            }
            
            if (chunkCount >= MAX_CHUNKS) {
                System.out.println("Warning: Reached maximum chunk limit, stopping stream");
            }
        }
        
        callback.onComplete();
    }
}

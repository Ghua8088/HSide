package  ide;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;
public class AIClient {
    private String url;
    private String port;
    private String model;
    private boolean isOllamaRunning() {
        try {
            URI uri = URI.create(this.url + this.port + "/api/tags");
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }
    AIClient() {
        this.port = "11434";
        this.url = "http://localhost:";
        this.model = "qwen2.5-coder:0.5b";
        if (!isOllamaRunning()) {
            try {
                ProcessBuilder pb = new ProcessBuilder("ollama", "serve");
                pb.redirectErrorStream(true);
                pb.start();
                System.out.println("Started Ollama.");
            } catch (IOException e) {
                System.out.println("Error starting Ollama: " + e.getMessage());
            }
        } else {
            System.out.println("Ollama already running.");
        }
    }
    public void setport(String port){
        this.port=port;
    }
    public void seturl(String url){
        this.url=url;
    }
    public void setModel(String model){
        this.model=model;
    }
    public String getport(){
        return this.port;
    }
    public String geturl(){
        return this.url;
    }
    public String getModel(){
        return this.model;
    }
    public String getAISuggestion(String context, int caretPosition) {
        if (context == null || context.isEmpty()) {
            return "";
        }

        try {
            System.out.println("Getting Suggestion...");
            String beforeCaret = context.substring(0, caretPosition);
            String afterCaret = context.substring(caretPosition);
            String prompt = PromptBuilder.autocompletePrompt(afterCaret,beforeCaret);
            JSONObject payload = new JSONObject()
                .put("model", "qwen2.5-coder:0.5b")
                .put("prompt", prompt)
                .put("stream", false);
            System.out.println("Payload:\n" + payload.toString(2));
            URI uri = URI.create(this.url + this.port + "/api/generate");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }
            JSONObject json = new JSONObject(response.toString());
            String fullResponse = json.getString("response").trim();
            System.out.println("Full Response:\n" + fullResponse);
            String suggestion = extractCodeBlock(fullResponse);
            System.out.println("Suggestion:\n" + suggestion);
            return suggestion;
        } catch (Exception ex) {
            System.out.println("AI Suggestion Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return "";
    }
    public static  String extractCodeBlock(String response) {
        // Match triple-backtick fenced blocks
        String codeBlockRegex = "(?s)```(?:[a-zA-Z]*\\n)?(.*?)```";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(codeBlockRegex);
        java.util.regex.Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            // Return only the inside of the backticks
            return matcher.group(1).trim();
        }

        // If no code block was found, return raw response
        return response.trim();
    }
    public String[] getAvailableModels() {
        try {
            URI uri = URI.create(this.url + this.port + "/api/tags");
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
            String names [] = new String[models.length()];
            for (int i = 0; i < models.length(); i++) {
                JSONObject model = models.getJSONObject(i);
                names[i] = model.getString("name");
            }
            return names;

        } catch (Exception ex) {
            System.out.println("Error getting available models: " + ex.getMessage());
            ex.printStackTrace();
        }
        return new String[]{};
    }
}

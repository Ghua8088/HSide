package ide;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
public class OllamaInstaller {
    private static final String OLLAMA_URL = "https://ollama.ai/download";
    private static final String DEFAULT_MODEL = "qwen2.5-coder:0.5b";
    public static void checkAndInstall() {
        if (!isOllamaInstalled()) {
            int response = JOptionPane.showConfirmDialog(null,
                "Ollama (AI backend) is not installed. Download and install now?",
                "AI Setup Required",
                JOptionPane.YES_NO_OPTION);
            
            if (response == JOptionPane.YES_OPTION) {
                downloadAndInstallOllama();
                downloadDefaultModel();
            }
        }
    }
    private static boolean isOllamaInstalled() {
        try {
            // Check if ollama command exists
            Process process = Runtime.getRuntime().exec("ollama --version");
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
    
    private static void downloadAndInstallOllama() {
        try {
            Desktop.getDesktop().browse(new URI(OLLAMA_URL));
            JOptionPane.showMessageDialog(null, """
                                                1. Download and run the Ollama installer
                                                2. After installation, restart this application
                                                3. The AI features will be automatically configured""",
                "Installation Instructions",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException | IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(null,
                "Failed to open download page: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static void downloadDefaultModel() {
        new Thread(() -> {
            try {
                // Wait for ollama to be installed
                while (!isOllamaInstalled()) {
                    Thread.sleep(5000);
                }
                
                // Pull the default model
                Process process = Runtime.getRuntime().exec("ollama pull " + DEFAULT_MODEL);
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Successfully downloaded model: " + DEFAULT_MODEL);
                } else {
                    System.err.println("Failed to download model: " + output);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
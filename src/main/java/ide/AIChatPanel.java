package ide;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class AIChatPanel extends JPanel {
    private final JTextArea chatArea;
    private final JTextArea inputField;
    private final JButton sendButton;
    private final AIClient aiClient;
    private final JLabel statusLabel;
    private final JButton attachButton;
    private final JTextArea contextArea;
    private final JDialog contextDialog;
    private String currentContext = "";
    private StringBuilder currentResponse = new StringBuilder();
    private boolean isStreaming = false;
    private boolean isInThinkBlock = false;
    
    public AIChatPanel() {
        this.aiClient = AIBridge.getInstance();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(new Color(40, 40, 40));
        
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chatArea.setBackground(new Color(45, 45, 45));
        chatArea.setForeground(new Color(220, 220, 220));
        chatArea.setCaretColor(new Color(220, 220, 220));
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true));
        chatScrollPane.setPreferredSize(new Dimension(300, 400));
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        inputPanel.setBackground(new Color(40, 40, 40));
        
        // Create a panel for input and status
        JPanel inputAndStatusPanel = new JPanel(new BorderLayout());
        inputAndStatusPanel.setBackground(new Color(40, 40, 40));
        
        inputField = new AutoExpandingTextArea();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputField.setBackground(new Color(50, 50, 50));
        inputField.setForeground(new Color(220, 220, 220));
        inputField.setCaretColor(new Color(220, 220, 220));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        sendButton = new StyledButton("›", new Color(100, 150, 255), new Color(80, 120, 200));
        sendButton.setPreferredSize(new Dimension(50, 35));
        sendButton.setToolTipText("Send message");
        
        // Attach context button
        attachButton = new StyledButton("…", new Color(80, 80, 80), new Color(60, 60, 60));
        attachButton.setPreferredSize(new Dimension(40, 35));
        attachButton.setToolTipText("Attach context");
        
        // Status label for loading indicator
        statusLabel = new JLabel("");
        statusLabel.setForeground(new Color(150, 150, 150));
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        
        // Create context dialog
        contextDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Attach Context", true);
        contextDialog.setLayout(new BorderLayout());
        contextDialog.setSize(600, 500);
        contextDialog.setLocationRelativeTo(this);
        
        // Initialize context area first
        contextArea = new JTextArea();
        contextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contextArea.setBackground(new Color(50, 50, 50));
        contextArea.setForeground(new Color(220, 220, 220));
        contextArea.setCaretColor(new Color(220, 220, 220));
        contextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contextArea.setLineWrap(true);
        contextArea.setWrapStyleWord(true);
        
        // Create main panel with options and text area
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(40, 40, 40));
        
        // Create options panel
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.setBackground(new Color(40, 40, 40));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JButton currentEditorButton = new JButton("Current Editor");
        currentEditorButton.setBackground(new Color(100, 150, 255));
        currentEditorButton.setForeground(Color.WHITE);
        currentEditorButton.setFocusPainted(false);
        currentEditorButton.addActionListener(e -> {
            String currentEditorContent = getCurrentEditorContent();
            if (currentEditorContent != null) {
                contextArea.setText(currentEditorContent);
            }
        });
        
        JButton allEditorsButton = new JButton("All Editors");
        allEditorsButton.setBackground(new Color(100, 150, 255));
        allEditorsButton.setForeground(Color.WHITE);
        allEditorsButton.setFocusPainted(false);
        allEditorsButton.addActionListener(e -> {
            String allEditorsContent = getAllEditorsContent();
            if (allEditorsContent != null) {
                contextArea.setText(allEditorsContent);
            }
        });
        
        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(new Color(200, 100, 100));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> contextArea.setText(""));
        
        optionsPanel.add(currentEditorButton);
        optionsPanel.add(allEditorsButton);
        optionsPanel.add(clearButton);
        
        // Create text area with label
        JPanel textAreaPanel = new JPanel(new BorderLayout());
        textAreaPanel.setBackground(new Color(40, 40, 40));
        textAreaPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JLabel contextLabel = new JLabel("Context Content:");
        contextLabel.setForeground(new Color(220, 220, 220));
        contextLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        contextLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        JScrollPane contextScrollPane = new JScrollPane(contextArea);
        contextScrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true));
        
        textAreaPanel.add(contextLabel, BorderLayout.NORTH);
        textAreaPanel.add(contextScrollPane, BorderLayout.CENTER);
        
        JPanel contextButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        contextButtonPanel.setBackground(new Color(40, 40, 40));
        contextButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JButton saveContextButton = new JButton("Save Context");
        saveContextButton.setBackground(new Color(100, 150, 255));
        saveContextButton.setForeground(Color.WHITE);
        saveContextButton.setFocusPainted(false);
        saveContextButton.addActionListener(e -> {
            currentContext = contextArea.getText();
            contextDialog.setVisible(false);
            updateContextIndicator();
        });
        
        JButton cancelContextButton = new JButton("Cancel");
        cancelContextButton.setBackground(new Color(80, 80, 80));
        cancelContextButton.setForeground(Color.WHITE);
        cancelContextButton.setFocusPainted(false);
        cancelContextButton.addActionListener(e -> contextDialog.setVisible(false));
        
        contextButtonPanel.add(saveContextButton);
        contextButtonPanel.add(cancelContextButton);
        
        mainPanel.add(optionsPanel, BorderLayout.NORTH);
        mainPanel.add(textAreaPanel, BorderLayout.CENTER);
        mainPanel.add(contextButtonPanel, BorderLayout.SOUTH);
        
        contextDialog.add(mainPanel, BorderLayout.CENTER);
        
        // Use a more flexible layout for the input panel
        JPanel inputRowPanel = new JPanel(new BorderLayout(5, 0));
        inputRowPanel.setBackground(new Color(40, 40, 40));
        inputRowPanel.add(attachButton, BorderLayout.WEST);
        inputRowPanel.add(inputField, BorderLayout.CENTER);
        inputRowPanel.add(sendButton, BorderLayout.EAST);
        
        inputAndStatusPanel.add(inputRowPanel, BorderLayout.CENTER);
        inputAndStatusPanel.add(statusLabel, BorderLayout.SOUTH);
        
        inputPanel.add(inputAndStatusPanel, BorderLayout.CENTER);
        
        // Add components
        add(chatScrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // Setup event handlers
        setupEventHandlers();
        
        // Add welcome message
        addMessage("AI", "Hello! How can I help you today?", false);
    }
    
    private void setupEventHandlers() {
        // Send button click
        sendButton.addActionListener(e -> sendMessage());
        
        // Attach context button click
        attachButton.addActionListener(e -> {
            contextArea.setText(currentContext);
            contextDialog.setVisible(true);
        });
        
        // Enter key in input field
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
    }
    
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;
        
        // Add user message to chat
        addMessage("You", message, true);
        inputField.setText("");
        
        // Disable input while processing
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        attachButton.setEnabled(false);
        statusLabel.setText("AI is thinking...");
        
        // Start streaming response
        isStreaming = true;
        currentResponse.setLength(0);
        
        // Add AI message placeholder
        addMessage("AI", "", false);
        isInThinkBlock = false;
        
        // Use streaming chat response
        aiClient.getStreamingChatResponse(message, currentContext, new AIClient.ChatStreamCallback() {
            @Override
            public void onChunk(String chunk) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Received chunk: '" + chunk + "'");
                    // Just append the chunk directly to the chat area
                    appendToLastMessage(chunk);
                });
            }
            
            @Override
            public void onThink(String thinkBlock) {
                // Legacy method - not used with streaming
            }
            
            @Override
            public void onThinkChunk(String thinkChunk) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Received think chunk: '" + thinkChunk + "'");
                    if (!isInThinkBlock) {
                        // Start think block
                        isInThinkBlock = true;
                        addMessage("AI (Thinking)", "", false);
                    }
                    // Stream think chunks to the chat
                    appendToLastThinkMessage(thinkChunk);
                });
            }
            
            @Override
            public void onThinkComplete() {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Think block completed");
                    isInThinkBlock = false;
                    // Start a new AI response below the thinking
                    addMessage("AI", "", false);
                });
            }
            
            @Override
            public void onComplete() {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("");
                    isStreaming = false;
                    // Re-enable input
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    attachButton.setEnabled(true);
                    inputField.requestFocus();
                });
            }
            
            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("");
                    isStreaming = false;
                    addMessage("AI", "Error: " + error, false);
                    // Re-enable input
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    attachButton.setEnabled(true);
                    inputField.requestFocus();
                });
            }
        });
    }
    
    private void addMessage(String sender, String message, boolean isUser) {
        String formattedMessage = String.format("%s: %s\n\n", sender, message);
        
        chatArea.append(formattedMessage);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        
        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private void updateLastMessage(String sender, String message, boolean isUser) {
        // Get the current document
        javax.swing.text.Document doc = chatArea.getDocument();
        
        try {
            // Find the last occurrence of the sender's message
            String currentText = chatArea.getText();
            String searchPattern = sender + ": ";
            int lastIndex = currentText.lastIndexOf(searchPattern);
            
            if (lastIndex != -1) {
                // Find the end of the current message (next double newline or end of text)
                int endIndex = currentText.indexOf("\n\n", lastIndex);
                if (endIndex == -1) {
                    endIndex = currentText.length();
                }
                
                // Replace the old message with the new one
                doc.remove(lastIndex, endIndex - lastIndex);
                doc.insertString(lastIndex, sender + ": " + message + "\n\n", null);
            }
        } catch (Exception e) {
            System.out.println("Error updating last message: " + e.getMessage());
        }
        
        // Auto-scroll to bottom
        chatArea.setCaretPosition(doc.getLength());
    }
    
    private void appendToLastMessage(String chunk) {
        // Get the current document
        javax.swing.text.Document doc = chatArea.getDocument();
        
        try {
            // Find the last occurrence of "AI: " and append the chunk right after it
            String currentText = chatArea.getText();
            String searchPattern = "AI: ";
            int lastIndex = currentText.lastIndexOf(searchPattern);
            
            if (lastIndex != -1) {
                // Find where the AI message starts (after "AI: ")
                int messageStart = lastIndex + searchPattern.length();
                
                // Find the end of the current message (next double newline or end of text)
                int endIndex = currentText.indexOf("\n\n", messageStart);
                if (endIndex == -1) {
                    endIndex = currentText.length();
                }
                
                // Insert the chunk at the end of the current message
                doc.insertString(endIndex, chunk, null);
            }
        } catch (Exception e) {
            System.out.println("Error appending chunk: " + e.getMessage());
        }
        
        // Auto-scroll to bottom
        chatArea.setCaretPosition(doc.getLength());
    }
    
    private void appendToLastThinkMessage(String chunk) {
        // Get the current document
        javax.swing.text.Document doc = chatArea.getDocument();
        
        try {
            // Find the last occurrence of "AI (Thinking): " and append the chunk right after it
            String currentText = chatArea.getText();
            String searchPattern = "AI (Thinking): ";
            int lastIndex = currentText.lastIndexOf(searchPattern);
            
            if (lastIndex != -1) {
                // Find where the think message starts (after "AI (Thinking): ")
                int messageStart = lastIndex + searchPattern.length();
                
                // Find the end of the current message (next double newline or end of text)
                int endIndex = currentText.indexOf("\n\n", messageStart);
                if (endIndex == -1) {
                    endIndex = currentText.length();
                }
                
                // Insert the chunk at the end of the current message
                doc.insertString(endIndex, chunk, null);
            }
        } catch (Exception e) {
            System.out.println("Error appending think chunk: " + e.getMessage());
        }
        
        // Auto-scroll to bottom
        chatArea.setCaretPosition(doc.getLength());
    }
    
    private void updateContextIndicator() {
        if (currentContext != null && !currentContext.trim().isEmpty()) {
            attachButton.setBackground(new Color(100, 150, 255));
            attachButton.setToolTipText("Context attached (" + currentContext.length() + " chars) - Click to edit");
        } else {
            attachButton.setBackground(new Color(80, 80, 80));
            attachButton.setToolTipText("Attach context");
        }
    }
    
    public void clearChat() {
        chatArea.setText("");
        addMessage("AI", "Chat cleared. How can I help you?", false);
    }
    
    public void addSystemMessage(String message) {
        addMessage("System", message, false);
    }
    
    // Helper methods to get editor content
    private String getCurrentEditorContent() {
        try {
            // Get the main frame and find the current editor
            Frame mainFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            if (mainFrame instanceof Notepad) {
                Notepad notepad = (Notepad) mainFrame;
                Editor currentEditor = notepad.getCurrentEditor();
                if (currentEditor != null) {
                    String content = currentEditor.getText();
                    String filePath = currentEditor.getFilePath();
                    String fileName = filePath != null ? new java.io.File(filePath).getName() : "Untitled";
                    return "=== " + fileName + " ===\n" + content + "\n";
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting current editor content: " + e.getMessage());
        }
        return null;
    }
    
    private String getAllEditorsContent() {
        try {
            // Get the main frame and find all editors
            Frame mainFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            if (mainFrame instanceof Notepad) {
                Notepad notepad = (Notepad) mainFrame;
                StringBuilder allContent = new StringBuilder();
                
                // Get all editors from the tabbed pane
                JTabbedPane tabbedPane = notepad.getManager().getTabbedPane();
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    Component component = tabbedPane.getComponentAt(i);
                    if (component instanceof Editor) {
                        Editor editor = (Editor) component;
                        String content = editor.getText();
                        String filePath = editor.getFilePath();
                        String fileName = filePath != null ? new java.io.File(filePath).getName() : "Untitled";
                        String tabTitle = tabbedPane.getTitleAt(i);
                        
                        allContent.append("=== ").append(fileName).append(" (").append(tabTitle).append(") ===\n");
                        allContent.append(content).append("\n\n");
                    }
                }
                
                return allContent.toString();
            }
        } catch (Exception e) {
            System.out.println("Error getting all editors content: " + e.getMessage());
        }
        return null;
    }
    
    // Custom auto-expanding text area class
    private static class AutoExpandingTextArea extends JTextArea {
        private static final int MIN_HEIGHT = 35;
        private static final int MAX_HEIGHT = 200;
        private javax.swing.Timer resizeTimer;
        
        public AutoExpandingTextArea() {
            setLineWrap(true); // Enable line wrap for vertical expansion
            setWrapStyleWord(true);
            setRows(1);
            
            // Create a timer for smooth resizing (prevents excessive resizing during rapid typing)
            resizeTimer = new javax.swing.Timer(100, e -> adjustSize());
            resizeTimer.setRepeats(false);
            
            // Add document listener to detect text changes
            getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    scheduleResize();
                }
                
                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    scheduleResize();
                }
                
                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    scheduleResize();
                }
            });
            
            // Add component listener to handle width changes
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    scheduleResize();
                }
            });
        }
        
        private void scheduleResize() {
            // Restart the timer to delay the resize
            resizeTimer.restart();
        }
        
        private void adjustSize() {
            // Calculate the height needed for the text
            String text = getText();
            if (text.isEmpty()) {
                setPreferredSize(new Dimension(getWidth(), MIN_HEIGHT));
            } else {
                // Calculate how many lines the text will take
                FontMetrics fm = getFontMetrics(getFont());
                int lineHeight = fm.getHeight();
                int availableWidth = Math.max(50, getWidth() - 24); // Account for borders and padding
                
                // Split text into lines and count them
                String[] lines = text.split("\n");
                int totalLines = 0;
                
                for (String line : lines) {
                    if (line.isEmpty()) {
                        totalLines++;
                    } else {
                        // Calculate how many lines this text will wrap to
                        int lineWidth = fm.stringWidth(line);
                        int wrappedLines = (int) Math.ceil((double) lineWidth / availableWidth);
                        totalLines += Math.max(1, wrappedLines);
                    }
                }
                
                // Calculate required height
                int requiredHeight = totalLines * lineHeight + 16; // Add padding
                int newHeight = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, requiredHeight));
                
                // Update preferred size - use current width
                setPreferredSize(new Dimension(getWidth(), newHeight));
            }
            
            // Trigger revalidation and repaint
            revalidate();
            repaint();
        }
        
        @Override
        public Dimension getPreferredSize() {
            // Calculate preferred size based on content
            String text = getText();
            if (text.isEmpty()) {
                return new Dimension(Math.max(100, getWidth()), MIN_HEIGHT);
            } else {
                FontMetrics fm = getFontMetrics(getFont());
                int lineHeight = fm.getHeight();
                int availableWidth = Math.max(50, getWidth() - 24);
                
                String[] lines = text.split("\n");
                int totalLines = 0;
                
                for (String line : lines) {
                    if (line.isEmpty()) {
                        totalLines++;
                    } else {
                        int lineWidth = fm.stringWidth(line);
                        int wrappedLines = (int) Math.ceil((double) lineWidth / availableWidth);
                        totalLines += Math.max(1, wrappedLines);
                    }
                }
                
                int requiredHeight = totalLines * lineHeight + 16;
                int height = Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, requiredHeight));
                
                return new Dimension(Math.max(100, getWidth()), height);
            }
        }
        
        @Override
        public Dimension getMinimumSize() {
            return new Dimension(100, MIN_HEIGHT);
        }
        
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, MAX_HEIGHT);
        }
    }
    
    // Custom styled button with modern appearance
    private static class StyledButton extends JButton {
        private final Color normalColor;
        private final Color hoverColor;
        private boolean isHovered = false;
        
        public StyledButton(String text, Color normalColor, Color hoverColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
            
            // Add mouse listeners for hover effect
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background with rounded corners
            Color bgColor = isHovered ? hoverColor : normalColor;
            g2d.setColor(bgColor);
            
            int arc = 8; // Corner radius
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            
            // Draw border
            g2d.setColor(bgColor.darker());
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            
            // Draw text
            g2d.setColor(getForeground());
            g2d.setFont(getFont());
            
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            
            g2d.drawString(getText(), textX, textY);
            
            g2d.dispose();
        }
        
        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            return new Dimension(textWidth + 20, 35);
        }
    }
} 
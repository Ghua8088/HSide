package ide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class LayoutManager {
    private final Notepad notepad;
    private final JTree fileTree;
    private final Terminal terminal;
    private final JSplitPane mainSplitPane;
    private final JSplitPane editorTerminalSplitPane;
    private final JTabbedPane tabbedPane;
    
    // Panel visibility states - start with all panels visible
    private boolean fileTreeVisible = true;
    private boolean terminalVisible = true;
    private boolean rightPanelVisible = false; // Start with right panel hidden
    
    // Layout toggle buttons
    private JButton leftPanelToggle;
    private JButton bottomPanelToggle;
    private JButton rightPanelToggle;
    
    // Panel containers
    private JPanel fileTreeContainer;
    private JPanel terminalContainer;
    private JPanel rightPanelContainer;
    private AIChatPanel aiChatPanel;
    
    public LayoutManager(Notepad notepad, JTree fileTree, Terminal terminal, 
                        JSplitPane mainSplitPane, JSplitPane editorTerminalSplitPane, 
                        JTabbedPane tabbedPane) {
        this.notepad = notepad;
        this.fileTree = fileTree;
        this.terminal = terminal;
        this.mainSplitPane = mainSplitPane;
        this.editorTerminalSplitPane = editorTerminalSplitPane;
        this.tabbedPane = tabbedPane;
        
        // Load visibility states from preferences
        PreferencesManager prefs = PreferencesManager.getInstance();
        fileTreeVisible = prefs.isFileTreeVisible();
        terminalVisible = prefs.isTerminalVisible();
        rightPanelVisible = prefs.isRightPanelVisible();
        
        setupPanelContainers();
        createLayoutButtons();
    }
    
    private void setupPanelContainers() {
        // Create containers for panels to enable proper hiding/showing
        fileTreeContainer = new JPanel(new BorderLayout());
        fileTreeContainer.add(fileTree, BorderLayout.CENTER);
        
        terminalContainer = new JPanel(new BorderLayout());
        terminalContainer.add(new JScrollPane(terminal), BorderLayout.CENTER);
        
        // Create AI chat panel for right side
        aiChatPanel = new AIChatPanel();
        rightPanelContainer = new JPanel(new BorderLayout());
        rightPanelContainer.add(aiChatPanel, BorderLayout.CENTER);
    }
    
    private void createLayoutButtons() {
        // Create toggle buttons with simple rectangle icons
        leftPanelToggle = createToggleButton("Left Panel", true);  // File tree starts visible
        bottomPanelToggle = createToggleButton("Bottom Panel", true);  // Terminal starts visible
        rightPanelToggle = createToggleButton("Right Panel", rightPanelVisible); // AI Chat panel
        
        // Add action listeners
        leftPanelToggle.addActionListener(e -> toggleFileTree());
        bottomPanelToggle.addActionListener(e -> toggleTerminal());
        rightPanelToggle.addActionListener(e -> toggleRightPanel());
        
        // Set tooltips
        leftPanelToggle.setToolTipText("Toggle File Tree (Left Panel)");
        bottomPanelToggle.setToolTipText("Toggle Terminal (Bottom Panel)");
        rightPanelToggle.setToolTipText("Toggle AI Chat (Right Panel)");
    }
    
    private JButton createToggleButton(String text, boolean isVisible) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(24, 24));
        button.setMaximumSize(new Dimension(24, 24));
        button.setMinimumSize(new Dimension(24, 24));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        // Create a simple icon based on visibility
        updateButtonIcon(button, isVisible);
        
        return button;
    }
    
    private void updateButtonIcon(JButton button, boolean isVisible) {
        // Create a simple colored rectangle icon
        int size = 20;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing for smoother icons
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Set background based on visibility state
        if (isVisible) {
            // Panel is visible - show active state
            g2d.setColor(new Color(80, 120, 200)); // Blue background for active
        } else {
            // Panel is hidden - show inactive state
            g2d.setColor(new Color(60, 60, 60)); // Gray background for inactive
        }
        g2d.fillRoundRect(0, 0, size, size, 3, 3);
        
        // Set panel color based on visibility
        if (isVisible) {
            g2d.setColor(new Color(120, 160, 220)); // Lighter blue for visible panels
        } else {
            g2d.setColor(new Color(40, 40, 40)); // Dark gray for hidden panels
        }
        
        // Draw panel representation
        if (button == leftPanelToggle || button == rightPanelToggle) {
            // Vertical panel (left or right)
            g2d.fillRoundRect(12, 2, 6, 16, 1, 1);
            g2d.setColor(isVisible ? new Color(100, 140, 200) : new Color(30, 30, 30));
            g2d.fillRoundRect(2, 2, 8, 16, 1, 1);
        } else if (button == bottomPanelToggle) {
            // Horizontal panel (bottom)
            g2d.fillRoundRect(2, 12, 16, 6, 1, 1);
            g2d.setColor(isVisible ? new Color(100, 140, 200) : new Color(30, 30, 30));
            g2d.fillRoundRect(2, 2, 16, 8, 1, 1);
        }
        
        g2d.dispose();
        
        button.setIcon(new ImageIcon(image));
    }
    
    public void toggleFileTree() {
        fileTreeVisible = !fileTreeVisible;
        updateButtonIcon(leftPanelToggle, fileTreeVisible);
        
        if (fileTreeVisible) {
            // Show file tree
            mainSplitPane.setLeftComponent(fileTreeContainer);
            mainSplitPane.setDividerLocation(200);
        } else {
            // Hide file tree
            mainSplitPane.setLeftComponent(null);
        }
        
        mainSplitPane.revalidate();
        mainSplitPane.repaint();
        
        // Save preference
        PreferencesManager.getInstance().setFileTreeVisible(fileTreeVisible);
    }
    
    public void toggleTerminal() {
        terminalVisible = !terminalVisible;
        updateButtonIcon(bottomPanelToggle, terminalVisible);
        
        if (terminalVisible) {
            // Show terminal
            editorTerminalSplitPane.setBottomComponent(terminalContainer);
            editorTerminalSplitPane.setDividerLocation(600);
        } else {
            // Hide terminal
            editorTerminalSplitPane.setBottomComponent(null);
        }
        
        editorTerminalSplitPane.revalidate();
        editorTerminalSplitPane.repaint();
        
        // Save preference
        PreferencesManager.getInstance().setTerminalVisible(terminalVisible);
    }
    
    public void toggleRightPanel() {
        rightPanelVisible = !rightPanelVisible;
        updateButtonIcon(rightPanelToggle, rightPanelVisible);
        
        if (rightPanelVisible) {
            // Create a new horizontal split pane to hold editor/terminal and AI chat
            JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorTerminalSplitPane, rightPanelContainer);
            
            // Replace the right component of main split pane
            mainSplitPane.setRightComponent(rightSplitPane);
            
            // Set divider location after the component is added to ensure proper sizing
            // Calculate the position to give chat panel the preferred width
            SwingUtilities.invokeLater(() -> {
                int totalWidth = rightSplitPane.getWidth();
                int chatWidth = PreferencesManager.getInstance().getChatPanelWidth();
                int dividerLocation = Math.max(100, totalWidth - chatWidth); // Ensure minimum editor space
                rightSplitPane.setDividerLocation(dividerLocation);
            });
        } else {
            // Restore original layout - just editor and terminal
            mainSplitPane.setRightComponent(editorTerminalSplitPane);
        }
        
        mainSplitPane.revalidate();
        mainSplitPane.repaint();
        
        // Save preference
        PreferencesManager.getInstance().setRightPanelVisible(rightPanelVisible);
    }
    
    public JButton getLeftPanelToggle() {
        return leftPanelToggle;
    }
    
    public JButton getBottomPanelToggle() {
        return bottomPanelToggle;
    }
    
    public JButton getRightPanelToggle() {
        return rightPanelToggle;
    }
    
    public boolean isFileTreeVisible() {
        return fileTreeVisible;
    }
    
    public boolean isTerminalVisible() {
        return terminalVisible;
    }
    
    public boolean isRightPanelVisible() {
        return rightPanelVisible;
    }
    
    public AIChatPanel getAIChatPanel() {
        return aiChatPanel;
    }
} 
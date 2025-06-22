package ide;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleContext;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.vdurmont.emoji.EmojiParser;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;
public class Notepad extends JFrame implements ActionListener{
    private String aiGhostSuggestion = "";
    private org.fife.ui.rtextarea.Gutter gutter;
    private int suggestionStart = -1;
    JMenuBar menubar; 
    AIClient aiClient=new AIClient();
    //JScrollPane scroll;
    JMenu file,edit,help,AI,fonts,fontStyle,fontsize;
    JMenuItem newFile,openFile,saveFile,exit,bold,italic,underline,fontcollection,github,AISettings;
    JButton viewmode;
    JButton confirmsize,confirmfont;
    RSyntaxTextArea textArea;
    JPanel fontSize;
    JComboBox<String> fontselect;
    JLabel fontpreview;
    String fontS[]=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();//gets all Font Names
    JTextField fontSizeN; // Font Size Text Field
    JFileChooser fileChooser; //open , save built-in file chooser
    JDialog fontSelector; // Font Selector Dialog
    JDialog aiSettingsDialog; // AI Settings Dialog
    JPanel footer;
    JLabel word_count,line_count,character_count,support,font_size;
    ImageIcon icon;
    Font f;
    boolean saved;
    private boolean isHighlighting = false;
    private javax.swing.Timer highlightTimer;
    private final int HIGHLIGHT_DELAY = 150; // ms
    // AI settings state
    private String[] availableModels = aiClient.getAvailableModels();
    private String selectedModel = aiClient.getModel();
    private String ollamaPort = aiClient.getport();
    JMenuItem findItem, replaceItem;
    JDialog findReplaceDialog;
    JTextField findField, replaceField;
    JButton findNextBtn, replaceBtn, replaceAllBtn, closeBtn;
    int lastFindIndex = -1;
    public Notepad(){
        saved=false;
        setFocusTraversalKeysEnabled(true);
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.out.println("Failed to initialize FlatLaf");
        }
        f=new Font("Arial",Font.PLAIN,20);
        icon=new ImageIcon("HSIDE.png");

        setSize(800, 600); // Larger default size
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeCheck();
            }
        });
        setLocationRelativeTo(null);
        setIconImage(icon.getImage());
        menubar=new JMenuBar();
        footer=new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        line_count=new JLabel("Line Count: 0");
        word_count=new JLabel("Word Count: 0");
        character_count=new JLabel("Character Count: 0");
        support=new JLabel("UTF-16");
        footer.add(word_count);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(character_count);
        footer.add(Box.createHorizontalGlue());
        footer.add(support);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(line_count);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        file=new JMenu("File");
        edit=new JMenu("Edit");
        help=new JMenu("Help");
        AI=new JMenu("AI");
        viewmode=new JButton(EmojiParser.parseToUnicode(":sunny:"));
        viewmode.setToolTipText("Toggle Dark Mode");
        newFile=new JMenuItem("New");
        openFile=new JMenuItem("Open");
        saveFile=new JMenuItem("Save");
        exit=new JMenuItem("Exit");
        AISettings=new JMenuItem("AI Settings");
        textArea = new GhostTextPane();
        
        textArea.setCurrentLineHighlightColor(new Color(10,10,10,10)); 
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 20));
        RTextScrollPane scroll = new RTextScrollPane(textArea);
        gutter = scroll.getGutter();
        // Set initial (light mode) gutter style
        gutter.setBackground(new Color(245, 245, 245));
        gutter.setLineNumberColor(new Color(80, 80, 80));
        gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 16));
        gutter.setBorderColor(new Color(200, 200, 200));
        gutter.setCurrentLineNumberColor(new Color(0, 120, 215));
        fileChooser=new JFileChooser();
        fonts=new JMenu("Fonts");
        fontselect=new JComboBox<>(fontS);
        fontcollection=new JMenuItem("fontcollection");
        fontStyle=new JMenu("Style");
        bold = new JCheckBoxMenuItem("Bold");
        italic = new JCheckBoxMenuItem("Italic");
        underline = new JCheckBoxMenuItem("Underline");
        fontSize=new JPanel();
        fontSize.setLayout(new GridLayout(1,3));
        font_size=new JLabel("size:");
        fontSizeN=new JTextField("20");
        confirmsize=new JButton("▶");
        fontsize=new JMenu("size");
        fontsize.add(fontSize);
        confirmsize.setToolTipText("Confirm Font Size");
        fontpreview=new JLabel("AaBbCc 123");
        fontpreview.setFont(f);
        fontpreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180), 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        fontSelector=new JDialog(this, "Font Selector", true);
        fontSelector.setSize(300,300);
        fontSelector.getContentPane().setBackground(Color.WHITE);

        confirmfont=new JButton("▶");
        github=new JMenuItem("git reference");

        fontcollection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,KeyEvent.ALT_DOWN_MASK));
        fontSelector.add(fontselect,BorderLayout.NORTH);
        fontpreview.setFont(f);
        fontSelector.add(fontpreview,BorderLayout.CENTER);
        fontSelector.add(confirmfont,BorderLayout.SOUTH);
        //textArea.addStyle("regular", null);
        textArea.addCaretListener((CaretEvent e) -> {
            updatecounts();
        } 
        );
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.isShiftDown() && e.isControlDown()){
                    textArea.setCaretPosition(textArea.getCaretPosition()-1);
                    System.out.println("shift");
                }
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown()) {
                    String context = textArea.getText().substring(0, textArea.getCaretPosition());
                    String suggestion = aiClient.getAISuggestion(context);
                    if (!suggestion.isEmpty()) {
                        ((GhostTextPane)textArea).setGhostText(suggestion, textArea.getCaretPosition());
                        e.consume();
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_TAB && !((GhostTextPane)textArea).getGhostText().isEmpty()) {
                    try {
                        textArea.getDocument().insertString(
                            ((GhostTextPane)textArea).getGhostTextPosition(), 
                            ((GhostTextPane)textArea).getGhostText(), 
                            null
                        );
                        ((GhostTextPane)textArea).clearGhostText();
                        e.consume();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else if (!e.isControlDown() && 
                        (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
                        Character.isLetterOrDigit(e.getKeyChar()))) {
                    ((GhostTextPane)textArea).clearGhostText();
                }
            }
        });
        fontselect.addItemListener((ItemEvent e) -> { 
            fontpreview.setFont(new Font((String)fontselect.getSelectedItem(),Font.PLAIN,15));
        });
        fontSize.add(font_size);
        fontSize.add(fontSizeN);
        fontSize.add(confirmsize);
        textArea.setFont(f);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,2));
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,2));
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,2));
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,2));
        menubar.add(file);
        help.add(github);
        menubar.add(edit);
        menubar.add(help);
        menubar.add(viewmode, BorderLayout.EAST);
        file.add(newFile);
        file.addSeparator();
        file.add(openFile);
        file.add(saveFile);
        file.addSeparator();
        file.add(exit);
        fontStyle.add(bold);
        fontStyle.add(italic);
        fontStyle.add(underline);
        fonts.add(fontStyle);
        fonts.add(fontcollection);
        fonts.add(fontsize);
        edit.add(fonts);
        //scroll=new JScrollPane(textArea);
        add(menubar,BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer,BorderLayout.SOUTH);
        viewmode.addActionListener(this);
        confirmfont.addActionListener(this);
        fontcollection.addActionListener(this);
        newFile.addActionListener(this);
        openFile.addActionListener(this);
        saveFile.addActionListener(this);
        fonts.addActionListener(this);
        exit.addActionListener(this);
        bold.addActionListener(this);
        italic.addActionListener(this);
        underline.addActionListener(this);
        confirmsize.addActionListener(this);
        github.addActionListener(this);
        fontSelector.setLocationRelativeTo(this);
        AI.add(AISettings);
        menubar.add(AI);
        // AI Settings Dialog setup
        aiSettingsDialog = new JDialog(this, "AI Settings", true);
        aiSettingsDialog.setSize(350, 200);
        aiSettingsDialog.setLayout(new BorderLayout());
        JPanel aiPanel = new JPanel();
        aiPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        aiPanel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> modelCombo = new JComboBox<>(availableModels);
        modelCombo.setSelectedItem(selectedModel);
        aiPanel.add(modelCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        aiPanel.add(new JLabel("Ollama Port:"), gbc);
        gbc.gridx = 1;
        JTextField portField = new JTextField(ollamaPort,10);
        aiPanel.add(portField, gbc);
        aiSettingsDialog.add(aiPanel, BorderLayout.CENTER);
        JPanel aiButtonPanel = new JPanel();
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        aiButtonPanel.add(okBtn);
        aiButtonPanel.add(cancelBtn);
        aiSettingsDialog.add(aiButtonPanel, BorderLayout.SOUTH);
        okBtn.addActionListener(e -> {
            selectedModel = (String) modelCombo.getSelectedItem();
            aiClient.setModel(selectedModel);
            ollamaPort = portField.getText();
            aiClient.setport(ollamaPort);
            aiSettingsDialog.setVisible(false);
        });
        cancelBtn.addActionListener(e -> aiSettingsDialog.setVisible(false));
        AISettings.addActionListener(e -> {
            modelCombo.setSelectedItem(selectedModel);
            portField.setText(ollamaPort);
            aiSettingsDialog.setLocationRelativeTo(this);
            aiSettingsDialog.setVisible(true);
        });
        findItem = new JMenuItem("Find");
        replaceItem = new JMenuItem("Replace");
        findItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        replaceItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK));
        edit.add(findItem);
        edit.add(replaceItem);

        // Find/Replace Dialog
        findReplaceDialog = new JDialog(this, "Find/Replace", false);
        findReplaceDialog.setSize(400, 150);
        findReplaceDialog.setLayout(new GridBagLayout());
        GridBagConstraints frgbc = new GridBagConstraints();
        frgbc.insets = new Insets(5, 5, 5, 5);
        frgbc.gridx = 0; frgbc.gridy = 0; frgbc.anchor = GridBagConstraints.EAST;
        findReplaceDialog.add(new JLabel("Find:"), frgbc);
        frgbc.gridx = 1; frgbc.anchor = GridBagConstraints.WEST;
        findField = new JTextField(20);
        findReplaceDialog.add(findField, frgbc);
        frgbc.gridx = 0; frgbc.gridy = 1; frgbc.anchor = GridBagConstraints.EAST;
        findReplaceDialog.add(new JLabel("Replace:"), frgbc);
        frgbc.gridx = 1; frgbc.anchor = GridBagConstraints.WEST;
        replaceField = new JTextField(20);
        findReplaceDialog.add(replaceField, frgbc);
        frgbc.gridx = 0; frgbc.gridy = 2; frgbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        findNextBtn = new JButton("Find Next");
        replaceBtn = new JButton("Replace");
        replaceAllBtn = new JButton("Replace All");
        closeBtn = new JButton("Close");
        btnPanel.add(findNextBtn);
        btnPanel.add(replaceBtn);
        btnPanel.add(replaceAllBtn);
        btnPanel.add(closeBtn);
        findReplaceDialog.add(btnPanel, frgbc);
        findReplaceDialog.setLocationRelativeTo(this);

        findItem.addActionListener(e -> {
            findReplaceDialog.setTitle("Find");
            replaceField.setEnabled(false);
            replaceBtn.setEnabled(false);
            replaceAllBtn.setEnabled(false);
            findReplaceDialog.setVisible(true);
        });
        replaceItem.addActionListener(e -> {
            findReplaceDialog.setTitle("Find/Replace");
            replaceField.setEnabled(true);
            replaceBtn.setEnabled(true);
            replaceAllBtn.setEnabled(true);
            findReplaceDialog.setVisible(true);
        });
        closeBtn.addActionListener(e -> findReplaceDialog.setVisible(false));

        findNextBtn.addActionListener(e -> findNext());
        replaceBtn.addActionListener(e -> replaceCurrent());
        replaceAllBtn.addActionListener(e -> replaceAll());
        setVisible(true);
    }
    void updatecounts(){
        word_count.setText("Word Count: "+getWordCount(textArea.getText()));
        character_count.setText("Character Count: "+getCharacterCount(textArea.getText()));
        line_count.setText("Line Count: "+getLineCount(textArea.getText()));
    }
    void save(){
            fileChooser.showSaveDialog(this);
            try {
                File readfile = fileChooser.getSelectedFile();
                FileOutputStream fos = new FileOutputStream(readfile);
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
                    String text = textArea.getText();
                    bw.write(text);
                }
                word_count.setText("Word Count: "+getWordCount(textArea.getText()));
                character_count.setText("Character Count: "+getCharacterCount(textArea.getText()));
                saved=true;
            } catch (IOException ex) {
                System.out.println("Exception loading File Loader:"+ex);
            }
    }
    void closeCheck(){
        if(!saved && textArea.getText().length()!=0){
            int result=JOptionPane.showConfirmDialog(this,"      you have not saved, \n do you still want to close ?");
            switch (result) {
                case JOptionPane.YES_OPTION:
                    System.exit(0);
                case JOptionPane.NO_OPTION:
                    save();
                    break;
                case JOptionPane.CANCEL_OPTION:
                    break;
            }
        }else{
            System.exit(0);
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==newFile){
            textArea.setText("");
        }
        else if(e.getSource()==openFile){
            int result=fileChooser.showOpenDialog(this);
            if(result==JFileChooser.APPROVE_OPTION){
                try{
                    File readfile=fileChooser.getSelectedFile();
                    FileInputStream fis=new FileInputStream(readfile);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
                        textArea.setText("");
                        String line;
                        while((line=br.readLine())!=null){
                            textArea.setText(textArea.getText()+line+"\n");
                        }
                    }
                    updatecounts();
                    saved=false;
                }
                catch(IOException ex){
                    System.out.println("Error opening file:"+ex);
                }
                
            }
        }else if(e.getSource()==saveFile){
            save();
        }else if(e.getSource()==bold||e.getSource()==italic||e.getSource()==underline){
            int style = Font.PLAIN;
            if (bold.isSelected()) style |= Font.BOLD;
            if (italic.isSelected()) style |= Font.ITALIC;
            Font baseFont = textArea.getFont().deriveFont(style);

            if (underline.isSelected()) {
                HashMap<TextAttribute, Object> attributes = new HashMap<>(baseFont.getAttributes());
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                textArea.setFont(baseFont.deriveFont(attributes));
            } else {
                textArea.setFont(baseFont);
            }
        }else if(e.getSource()==confirmsize){
            textArea.setFont(new Font("Arial",Font.PLAIN,Integer.parseInt(fontSizeN.getText())));
        }else if(e.getSource()==exit){
            closeCheck();
            System.exit(0);
            
        }else if(e.getSource()==fontcollection){
            fontSelector.setVisible(true);
        }else if(e.getSource()==confirmfont){
            textArea.setFont(new Font((String)fontselect.getSelectedItem(),Font.PLAIN,Integer.parseInt(fontSizeN.getText())));
            fontSelector.setVisible(false);
        }else if(e.getSource()==viewmode){
            if(viewmode.getText().equals(EmojiParser.parseToUnicode(":sunny:"))){
                viewmode.setText(EmojiParser.parseToUnicode(":night_with_stars:"));
                textArea.setBackground(new Color(45,45,45));
                textArea.setForeground(Color.WHITE);
                textArea.setCaretColor(Color.WHITE);
                menubar.setBackground(new Color(30,30,30));
                menubar.setForeground(Color.WHITE);
                footer.setBackground(new Color(30,30,30));
                footer.setForeground(Color.WHITE);
                fontSelector.getContentPane().setBackground(new Color(45,45,45));
                fontSelector.getContentPane().setForeground(Color.WHITE);
                fontpreview.setBackground(new Color(45,45,45));
                fontpreview.setForeground(Color.WHITE);
                ((GhostTextPane)textArea).setDarkMode(true);
                gutter.setBackground(new Color(30, 30, 30));
                gutter.setLineNumberColor(new Color(0, 255, 239));
                gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 10));
                gutter.setBorderColor(new Color(60, 60, 60));
                gutter.setCurrentLineNumberColor(new Color(50, 255, 239));
                try { UIManager.setLookAndFeel(new FlatDarkLaf()); SwingUtilities.updateComponentTreeUI(this); } catch (Exception ex) {}
            }else{
                viewmode.setText(EmojiParser.parseToUnicode(":sunny:"));
                textArea.setBackground(new Color(255,255,240));
                textArea.setForeground(Color.BLACK);
                textArea.setCaretColor(Color.BLACK);
                menubar.setBackground(null);
                menubar.setForeground(null);
                footer.setBackground(null);
                footer.setForeground(null);
                fontSelector.getContentPane().setBackground(Color.WHITE);
                fontSelector.getContentPane().setForeground(Color.BLACK);
                fontpreview.setBackground(Color.WHITE);
                fontpreview.setForeground(Color.BLACK);
                gutter.setBackground(new Color(245, 245, 245));
                gutter.setLineNumberColor(new Color(80, 80, 80));
                gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 10));
                gutter.setBorderColor(new Color(200, 200, 200));
                gutter.setCurrentLineNumberColor(new Color(120, 80, 80));
                ((GhostTextPane)textArea).setDarkMode(false);
                try { UIManager.setLookAndFeel(new FlatLightLaf()); SwingUtilities.updateComponentTreeUI(this); } catch (Exception ex) {}
            }
        }else if(e.getSource()==github){
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start " + "https://github.com/Ghua8088?tab=repositories"});
            } catch (IOException e1) {
                System.out.println("exception loading URL:"+e1);
            }
            System.out.println("Redirecting to GITHUB " + EmojiParser.parseToUnicode(":heart_eyes:"));
        }else if(e.getSource()==findItem || e.getSource()==replaceItem){
            findReplaceDialog.setVisible(true);
        }
    }
    int getWordCount(String text){
        return text.split("\\s+").length;
    }
    int getCharacterCount(String text){
        return text.length();
    }
    int getLineCount(String text){
        return text.split("\\n").length;
    }
    void findNext() {
        String findText = findField.getText();
        if (findText.isEmpty()) return;
        String content = textArea.getText();
        int start = textArea.getSelectionEnd();
        int idx = content.indexOf(findText, start);
        if (idx == -1 && start > 0) {
            // Wrap around
            idx = content.indexOf(findText);
        }
        if (idx != -1) {
            textArea.requestFocus();
            textArea.select(idx, idx + findText.length());
            lastFindIndex = idx;
        } else {
            JOptionPane.showMessageDialog(this, "Text not found.");
        }
    }
    void replaceCurrent() {
        String findText = findField.getText();
        String replaceText = replaceField.getText();
        if (findText.isEmpty()) return;
        int selStart = textArea.getSelectionStart();
        int selEnd = textArea.getSelectionEnd();
        if (selStart != selEnd && textArea.getSelectedText().equals(findText)) {
            try {
                textArea.getDocument().remove(selStart, findText.length());
                textArea.getDocument().insertString(selStart, replaceText, null);
                textArea.select(selStart, selStart + replaceText.length());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        findNext();
    }
    void replaceAll() {
        String findText = findField.getText();
        String replaceText = replaceField.getText();
        if (findText.isEmpty()) return;
        String content = textArea.getText();
        int count = 0;
        int idx = content.indexOf(findText);
        while (idx != -1) {
            count++;
            content = content.substring(0, idx) + replaceText + content.substring(idx + findText.length());
            idx = content.indexOf(findText, idx + replaceText.length());
        }
        textArea.setText(content);
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "No occurrences replaced.");
        } else {
            JOptionPane.showMessageDialog(this, count + " occurrences replaced.");
        }
    }
    public static void main(String[] args) {
        OllamaInstaller.checkAndInstall();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.out.println("Failed to initialize FlatLaf");
        }
        SwingUtilities.invokeLater(Notepad::new);
    }    
}
class GhostTextPane extends RSyntaxTextArea {
    private String ghostText = "";
    private int ghostTextPosition = -1;
    private boolean darkMode = false;

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public void setGhostText(String text, int position) {
        this.ghostText = text;
        this.ghostTextPosition = position;
        repaint();
    }

    public void clearGhostText() {
        this.ghostText = "";
        this.ghostTextPosition = -1;
        repaint();
    }

    public String getGhostText() {
        return ghostText;
    }

    public int getGhostTextPosition() {
        return ghostTextPosition;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!ghostText.isEmpty() && ghostTextPosition >= 0) {
            try {
                Rectangle r = modelToView(ghostTextPosition);
                if (r != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    Color ghostColor = darkMode
                        ? new Color(180, 180, 180, 150)
                        : new Color(100, 100, 100, 150);
                    g2d.setColor(ghostColor);
                    g2d.setFont(getFont().deriveFont(Font.ITALIC));
                    g2d.drawString(ghostText, r.x, r.y + g.getFontMetrics().getAscent());
                    g2d.dispose();
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
}
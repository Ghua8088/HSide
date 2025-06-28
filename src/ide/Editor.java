package ide;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.Gutter;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
public class Editor extends JPanel {
    private final static AIClient aiClient = AIBridge.getInstance();
    private static final long serialVersionUID = 1L;
    private final GhostTextPane textArea;
    private final RTextScrollPane scrollPane;
    private boolean saved;
    private boolean changed;
    private String filePath;
    private final Gutter gutter;
    final int[] hoveredLine = {-1};
    private String word_count,line_count,character_count;
    public Editor(String dir){
        super(new BorderLayout());
        hoveredLine[0] = -1;
        filePath = null;
        saved = false;
        textArea = new GhostTextPane();
        scrollPane = new RTextScrollPane(textArea);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setBackground(new Color(45,45,45));
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setCurrentLineHighlightColor(new Color(10,10,10,10));
        gutter = scrollPane.getGutter();
        gutter.setBackground(new Color(30, 30, 30));
        gutter.setLineNumberColor(new Color(0, 255, 239));
        gutter.setLineNumberFont(new Font("Roboto", Font.PLAIN,16));
        gutter.setBorderColor(new Color(60, 60, 60));
        gutter.setCurrentLineNumberColor(new Color(50, 255, 239));
        add(scrollPane, BorderLayout.CENTER);
        setCodeFoldIcon();
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                    if (getSave() == null) {
                        setSave(true);
                    } else {
                        setSave(false);
                    }
                }else if(e.isShiftDown() && e.isControlDown()){
                    textArea.setCaretPosition(textArea.getCaretPosition()-1);
                    System.out.println("shift");
                }else if (e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown()) {
                    String context = getText().substring(0, getCaretPosition());
                    String suggestion = aiClient.getAISuggestion(context,textArea.getCaretPosition());
                    if (!suggestion.isEmpty()) {
                        textArea.setGhostText(suggestion,  getCaretPosition());
                        e.consume();
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_TAB && !textArea.getGhostText().isEmpty()) {
                    try {
                        textArea.getDocument().insertString(
                            textArea.getGhostTextPosition(), 
                            textArea.getGhostText(), 
                            null
                        );
                        getTextArea().clearGhostText();
                        e.consume();
                    } catch (Exception ex) {
                    }
                }
                else if (!e.isControlDown() && 
                        (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
                        Character.isLetterOrDigit(e.getKeyChar()))) {
                         getTextArea().clearGhostText();
                }
            }
        });
        textArea.addCaretListener((CaretEvent e) -> updatecounts());
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setSave(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setSave(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        gutter.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int y = e.getY();
                int lineHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
                int line = y / lineHeight;
                if (hoveredLine[0] != line) {
                    hoveredLine[0] = line;
                    gutter.repaint();
                }
            }
        });
        gutter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredLine[0] = -1;
                gutter.repaint();
            }
        });
    }
    public HashMap<String,String> updatecounts(){
        word_count="Word Count: "+getWordCount(getText());
        character_count="Character Count: "+getCharacterCount(getText());
        line_count = "Line Count: "+getLineCount(getText());
        return new HashMap<String,String>(){{
            put("word_count",word_count);
            put("character_count",character_count);
            put("line_count",line_count);
        }};
    }
    int getWordCount(String text){
        return text.split("\\s+").length;
    }
    String getWordCountLabel(){
        return word_count;
    }
    String getCharacterCountLabel(){
        return character_count;
    }
    String getLineCountLabel(){
        return line_count;
    }
    int getCharacterCount(String text){
        return text.length();
    }
    int getLineCount(String text){
        return text.split("\\n").length;
    }
    public GhostTextPane getTextArea() {
        return textArea;
    }
    private void setCodeFoldIcon(){
        Icon  expandIcon= new ArrowIcon(SwingConstants.SOUTH, 8, Color.GRAY);
        Icon collapseIcon = new ArrowIcon(SwingConstants.EAST, 8, Color.GRAY);
        gutter.setFoldIndicatorEnabled(true);
        gutter.setFoldIcons(collapseIcon, expandIcon);
    }
    public Boolean getSave(){
        return saved;
    }
    public void setSave(Boolean saved){
        this.saved = saved;
    }
    public RTextScrollPane getScrollPane() {
        return scrollPane;
    }
    public String getLineAtCaret(JTextArea textArea) {
        try {
            int caretPos = textArea.getCaretPosition();
            int line = textArea.getLineOfOffset(caretPos);
            int start = textArea.getLineStartOffset(line);
            int end = textArea.getLineEndOffset(line);
            return textArea.getText(start, end - start);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    @Override
    public void setFont(Font font) {
        if (textArea != null) textArea.setFont(font);
    }
    public void setFilePath(String path) { 
        this.filePath = path; 
    }
    public String getFilePath() { 
        return filePath; 
    }
    public void setSyntaxStyle(String style) {
        textArea.setSyntaxEditingStyle(style);
    }
    public void setDarkMode(boolean dark) {
        textArea.setDarkMode(dark);
    }
    public void setText(String text) {
        textArea.setText(text);
    }
    public String getText() {
        return textArea.getText();
    }
    public void setCaretPosition(int pos) {
        textArea.setCaretPosition(pos);
    }
    public int getCaretPosition() {
        return textArea.getCaretPosition();
    }
    public Gutter getGutter() {
        return scrollPane.getGutter();
    }
    static class ArrowIcon implements Icon {
        private final int direction;
        private final int size;
        private final Color color;

        ArrowIcon(int direction, int size, Color color) {
            this.direction = direction;
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            int mid = size / 2;
            switch (direction) {
                case SwingConstants.EAST -> {
                    // Draw a chevron >
                    g2.drawLine(x, y, x + mid, y + mid);
                    g2.drawLine(x + mid, y + mid, x, y + size);
                }
                case SwingConstants.SOUTH -> {
                    // Draw a chevron v
                    g2.drawLine(x, y, x + mid, y + mid);
                    g2.drawLine(x + mid, y + mid, x + size, y);
                }
            }

            g2.dispose();
        }
        

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
    
    
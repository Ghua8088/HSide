package ide;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.FoldIndicatorIcon;

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
    private String word_count,line_count,character_count,position_count;
    private Runnable onCountsChanged;
    
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
        gutter = scrollPane.getGutter();
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
                    System.out.println(context);
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
        textArea.addCaretListener((CaretEvent e) -> {
            updatecounts();
            if (onCountsChanged != null) {
                onCountsChanged.run();
            }
        });
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setSave(false);
                updatecounts();
                if (onCountsChanged != null) {
                    onCountsChanged.run();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setSave(false);
                updatecounts();
                if (onCountsChanged != null) {
                    onCountsChanged.run();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatecounts();
                if (onCountsChanged != null) {
                    onCountsChanged.run();
                }
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
        position_count = "ln " + getCurrentLine() + " col " + getCurrentColumn();
        return new HashMap<String,String>(){{
            put("word_count",word_count);
            put("character_count",character_count);
            put("line_count",line_count);
            put("position_count",position_count);
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
    
    String getPositionCountLabel(){
        return position_count;
    }
    
    public void setOnCountsChanged(Runnable callback) {
        this.onCountsChanged = callback;
    }
    int getCharacterCount(String text){
        return text.length();
    }
    int getLineCount(String text){
        return text.split("\\n").length;
    }
    
    int getCurrentLine() {
        try {
            return textArea.getLineOfOffset(textArea.getCaretPosition()) + 1;
        } catch (Exception e) {
            return 1;
        }
    }
    
    int getCurrentColumn() {
        try {
            int caretPos = textArea.getCaretPosition();
            int lineStart = textArea.getLineStartOffset(textArea.getLineOfOffset(caretPos));
            return caretPos - lineStart + 1;
        } catch (Exception e) {
            return 1;
        }
    }
    public GhostTextPane getTextArea() {
        return textArea;
    }
    private void setCodeFoldIcon(){
        FoldIndicatorIcon  expandIcon= new ArrowIcon(SwingConstants.SOUTH, 8, Color.GRAY);
        FoldIndicatorIcon collapseIcon = new ArrowIcon(SwingConstants.EAST, 8, Color.GRAY);
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
    static class ArrowIcon extends FoldIndicatorIcon {
        private final int direction;
        private final int size;
        private final Color color;

        ArrowIcon(int direction, int size, Color color) {
            super(true);
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
                    g2.drawLine(x, y, x + mid, y + mid);
                    g2.drawLine(x + mid, y + mid, x, y + size);
                }
                case SwingConstants.SOUTH -> {
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
    
    
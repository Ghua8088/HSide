package ide;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import java.awt.*;
import javax.swing.text.BadLocationException;
public class GhostTextPane extends RSyntaxTextArea {
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
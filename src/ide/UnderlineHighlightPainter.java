package ide;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
class UnderlineHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
    public UnderlineHighlightPainter(Color color) {
        super(color);
    }
    @Override
    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
        try {
            Rectangle p0 = c.modelToView2D(offs0).getBounds();
            Rectangle p1 = c.modelToView2D(offs1).getBounds();
            if (p0 == null || p1 == null) return;
            int y = p0.y + p0.height - 2;
            g.setColor(getColor());
            g.drawLine(p0.x, y, p1.x, y+1);
            
        } catch (BadLocationException e) {
            // ignore
        }
    }
}
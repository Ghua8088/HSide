package ide;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
class TabReorderHandler extends MouseAdapter {
    private final JTabbedPane tabbedPane;
    private int dragTabIndex = -1;
    public TabReorderHandler(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }
    @Override
    public void mousePressed(MouseEvent e) {
        dragTabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        int targetIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
        if (dragTabIndex != -1 && targetIndex != -1 && dragTabIndex != targetIndex) {
            Component comp = tabbedPane.getComponentAt(dragTabIndex);
            String title = tabbedPane.getTitleAt(dragTabIndex);
            Icon icon = tabbedPane.getIconAt(dragTabIndex);
            String tip = tabbedPane.getToolTipTextAt(dragTabIndex);
            Component tabComponent = tabbedPane.getTabComponentAt(dragTabIndex);

            tabbedPane.remove(dragTabIndex);
            tabbedPane.insertTab(title, icon, comp, tip, targetIndex);
            tabbedPane.setTabComponentAt(targetIndex, tabComponent);
            tabbedPane.setSelectedIndex(targetIndex);
            dragTabIndex = targetIndex;
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        dragTabIndex = -1;
    }
}
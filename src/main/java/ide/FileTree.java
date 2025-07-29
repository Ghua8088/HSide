package ide;

import java.awt.Component;
import java.awt.FontMetrics;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
public class FileTree extends JTree{
    public FileTree(DefaultTreeModel model) {
        super(model);
        setShowsRootHandles(true);
        setCellRenderer(new FileTreeCellRenderer());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        putClientProperty("JTree.lineStyle", "None");
        setToggleClickCount(1);
        setLargeModel(true);
    }
}
class FileTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        if (userObject instanceof File file) {
            String fullName = file.getName();
            int availableWidth = tree.getVisibleRect().width - 50;
            FontMetrics fm;
            fm = label.getFontMetrics(label.getFont());
            String truncated = fullName;

            if (fm.stringWidth(fullName) > availableWidth) {
                while (fm.stringWidth(truncated + "...") > availableWidth && truncated.length() > 1) {
                    truncated = truncated.substring(0, truncated.length() - 1);
                }
                truncated += "...";
            }
            label.setText("📄"+truncated);
            label.setToolTipText(fullName);
        }

        return label;
    }
}
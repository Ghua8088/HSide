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
        // If this is our placeholder node, render nothing (it only exists to show expand handle)
        if (userObject instanceof Boolean && Boolean.TRUE.equals(userObject)) {
            label.setText("");
            label.setToolTipText(null);
            return label;
        }

        if (userObject instanceof File file) {
            String fullName = file.getName();
            if (fullName == null || fullName.isEmpty()) {
                // For root-like nodes, show absolute path
                fullName = file.getAbsolutePath();
            }

            // Determine available width; give first-level nodes more space so names remain readable
            int availableWidth = tree.getVisibleRect().width - 50;
            try {
                javax.swing.tree.TreePath path = tree.getPathForRow(row);
                int depth = path == null ? 0 : path.getPathCount() - 1; // root=0, first level=1
                if (depth == 1) {
                    availableWidth = Math.max(availableWidth, tree.getVisibleRect().width - 20);
                }
            } catch (Exception ignored) {}

            FontMetrics fm = label.getFontMetrics(label.getFont());
            String truncated = fullName;

            if (fm.stringWidth(fullName) > availableWidth && availableWidth > fm.stringWidth("...")) {
                while (fm.stringWidth(truncated + "...") > availableWidth && truncated.length() > 1) {
                    truncated = truncated.substring(0, truncated.length() - 1);
                }
                truncated += "...";
            }

            // Use different glyphs for directories vs files
            String glyph = file.isDirectory() ? "📁" : "📄";
            label.setText(glyph + truncated);
            label.setToolTipText(fullName);
        }

        return label;
    }
}
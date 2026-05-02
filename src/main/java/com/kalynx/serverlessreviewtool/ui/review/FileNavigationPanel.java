package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.managers.ReviewContextManager;
import com.kalynx.serverlessreviewtool.models.ReviewContext;
import com.kalynx.serverlessreviewtool.models.*;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedPanel;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedScrollPane;
import com.kalynx.serverlessreviewtool.swingextensions.themedcomponents.ThemedTree;
import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.icons.FileIcon;
import com.kalynx.serverlessreviewtool.theme.icons.FolderIcon;
import com.kalynx.serverlessreviewtool.theme.icons.RepositoryIcon;
import com.kalynx.serverlessreviewtool.theme.icons.FileCommentIcon;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileNavigationPanel - Tree view for navigating files across multiple repositories
 */
public class FileNavigationPanel extends ThemedPanel {

    private final ReviewContextManager reviewContextManager;
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private final List<FileSelectionListener> listeners = new ArrayList<>();

    private ThemedTree fileTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private ReviewContext currentReviewContext;

    public FileNavigationPanel(ReviewContextManager reviewContextManager) {
        this.reviewContextManager = reviewContextManager;
        setLayout(new BorderLayout());

        initializeComponents();
        setupListeners();
    }

    private void initializeComponents() {
        rootNode = new DefaultMutableTreeNode("Review Files");
        treeModel = new DefaultTreeModel(rootNode);

        fileTree = new ThemedTree(treeModel);
        fileTree.setRootVisible(false);
        fileTree.setShowsRootHandles(true);
        fileTree.setCellRenderer(new FileTreeCellRenderer());

        fileTree.addTreeSelectionListener(ignored -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.getUserObject() instanceof ReviewFile file) {
                fireFileSelected(file);
            }
        });

        ThemedScrollPane scrollPane = new ThemedScrollPane(fileTree);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        reviewContextManager.addListener(this::onReviewContextChanged);
    }

    private void onReviewContextChanged(ReviewContext context) {
        this.currentReviewContext = context;
        if (context != null) {
            buildFileTree(context);
        }
    }

    private void buildFileTree(ReviewContext reviewContext) {
        rootNode.removeAllChildren();

        for (Repository repo : reviewContext.getRepositories()) {
            DefaultMutableTreeNode repoNode = new DefaultMutableTreeNode(repo);
            for (ReviewFile file : repo.getFiles()) {
                addFileToTree(repoNode, file);
            }

            rootNode.add(repoNode);
        }

        treeModel.reload();

        // Expand all repository nodes
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
    }

    private void addFileToTree(DefaultMutableTreeNode repoNode, ReviewFile file) {
        String[] pathParts = file.getPath().split("/");
        DefaultMutableTreeNode currentNode = repoNode;

        // Create directory nodes
        for (int i = 0; i < pathParts.length - 1; i++) {
            String dirName = pathParts[i];
            currentNode = findOrCreateChildNode(currentNode, dirName);
        }

        // Add file node
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file);
        currentNode.add(fileNode);
    }

    private DefaultMutableTreeNode findOrCreateChildNode(DefaultMutableTreeNode parent, String name) {
        // Look for existing child with this name
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            Object userObject = child.getUserObject();
            String childName = userObject instanceof String ? (String) userObject : userObject.toString();
            if (childName.equals(name)) {
                return child;
            }
        }

        // Create new child
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
        parent.add(newNode);
        return newNode;
    }

    public ReviewFile getSelectedFile() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null && selectedNode.getUserObject() instanceof ReviewFile) {
            return (ReviewFile) selectedNode.getUserObject();
        }
        return null;
    }

    public void refreshDisplay() {
        treeModel.reload();
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
    }

    // File tree cell renderer
    private class FileTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            Theme theme = themeManager.getCurrentTheme();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            // Apply theme colors
            setBackgroundNonSelectionColor(theme.getBackgroundColor());
            setBackgroundSelectionColor(theme.getAccentColor());
            setTextNonSelectionColor(theme.getForegroundColor());
            setTextSelectionColor(Color.WHITE);
            setBorderSelectionColor(theme.getAccentColor());

            int iconSize = 16; // Standard icon size for tree items

            if (userObject instanceof Repository repo) {
                setText(repo.getName());
                setFont(getFont().deriveFont(Font.BOLD));
                setIcon(new RepositoryIcon(iconSize));
            } else if (userObject instanceof ReviewFile file) {
                setText(file.getFileName());

                // Check for comments on this file
                Icon fileIcon = new FileIcon(iconSize);
                if (currentReviewContext != null) {
                    List<ReviewComment> comments = currentReviewContext.getCommentsForFile(file.getPath());
                    if (!comments.isEmpty()) {
                        // Determine if comments need resolution
                        boolean hasUnresolved = comments.stream()
                            .anyMatch(c -> c.needsResolution() && !c.isResolved());
                        boolean hasResolved = comments.stream()
                            .anyMatch(c -> c.needsResolution() && c.isResolved());

                        Color commentColor = hasUnresolved
                            ? new Color(255, 152, 0)  // Orange for unresolved
                            : (hasResolved ? new Color(76, 175, 80) : theme.getAccentColor());  // Green for resolved

                        int commentCount = comments.size();
                        Icon commentIcon = new FileCommentIcon(10, commentColor, commentCount, false);
                        fileIcon = new CompositeIcon(fileIcon, commentIcon);
                    }
                }
                setIcon(fileIcon);

                // Color by change type (only when not selected)
                if (!sel) {
                    switch (file.getChangeType()) {
                        case ADDED:
                            setForeground(new Color(40, 167, 69)); // Green
                            break;
                        case DELETED:
                            setForeground(new Color(220, 53, 69)); // Red
                            break;
                        case MODIFIED:
                            setForeground(theme.getAccentColor());
                            break;
                        case RENAMED:
                            setForeground(new Color(255, 193, 7)); // Yellow
                            break;
                    }
                }
            } else if (userObject instanceof String) {
                // It's a folder
                setText(value.toString());
                setIcon(new FolderIcon(iconSize));
            } else {
                setText(value.toString());
            }

            return this;
        }
    }

    // Composite icon to overlay comment indicator on file icon
    private static class CompositeIcon implements Icon {
        private final Icon baseIcon;
        private final Icon overlayIcon;

        public CompositeIcon(Icon baseIcon, Icon overlayIcon) {
            this.baseIcon = baseIcon;
            this.overlayIcon = overlayIcon;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            baseIcon.paintIcon(c, g, x, y);
            // Position overlay in bottom-right corner of base icon
            int overlayX = x + baseIcon.getIconWidth() - overlayIcon.getIconWidth();
            int overlayY = y + baseIcon.getIconHeight() - overlayIcon.getIconHeight();
            overlayIcon.paintIcon(c, g, overlayX, overlayY);
        }

        @Override
        public int getIconWidth() {
            return baseIcon.getIconWidth(); // Same width as base icon
        }

        @Override
        public int getIconHeight() {
            return baseIcon.getIconHeight(); // Same height as base icon
        }
    }

    // Listener interface
    public interface FileSelectionListener {
        void onFileSelected(ReviewFile file);
    }

    public void addFileSelectionListener(FileSelectionListener listener) {
        listeners.add(listener);
    }

    private void fireFileSelected(ReviewFile file) {
        for (FileSelectionListener listener : listeners) {
            listener.onFileSelected(file);
        }
    }
}


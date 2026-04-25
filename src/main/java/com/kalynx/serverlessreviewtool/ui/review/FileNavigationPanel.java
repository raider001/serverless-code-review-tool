package com.kalynx.serverlessreviewtool.ui.review;

import com.kalynx.serverlessreviewtool.theme.ThemeManager;
import com.kalynx.serverlessreviewtool.theme.Theme;
import com.kalynx.serverlessreviewtool.theme.components.*;
import com.kalynx.serverlessreviewtool.theme.icons.FileIcon;
import com.kalynx.serverlessreviewtool.theme.icons.FolderIcon;
import com.kalynx.serverlessreviewtool.theme.icons.RepositoryIcon;
import com.kalynx.serverlessreviewtool.ui.review.model.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileNavigationPanel - Tree view for navigating files across multiple repositories
 */
public class FileNavigationPanel extends ThemedPanel {

    private final ThemeManager themeManager;
    private ReviewContext reviewContext;
    private ThemedTree fileTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private ThemedScrollPane scrollPane;
    private final List<FileSelectionListener> listeners;
    private Commit startCommit;
    private Commit endCommit;

    public FileNavigationPanel(ReviewContext reviewContext) {
        this.themeManager = ThemeManager.getInstance();
        this.reviewContext = reviewContext;
        this.listeners = new ArrayList<>();

        setLayout(new BorderLayout());
//        setBorder(ThemedTitledBorder.create("Files"));

        initializeComponents();
        buildFileTree();
    }

    private void initializeComponents() {
        // Create root node
        rootNode = new DefaultMutableTreeNode("Review Files");
        treeModel = new DefaultTreeModel(rootNode);

        // Create tree
        fileTree = new ThemedTree(treeModel);
        fileTree.setRootVisible(false);
        fileTree.setShowsRootHandles(true);
        fileTree.setCellRenderer(new FileTreeCellRenderer());

        // Add selection listener
        fileTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
            if (selectedNode != null && selectedNode.getUserObject() instanceof ReviewFile) {
                ReviewFile file = (ReviewFile) selectedNode.getUserObject();
                fireFileSelected(file);
            }
        });

        // Wrap in scroll pane
        scrollPane = new ThemedScrollPane(fileTree);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void buildFileTree() {
        rootNode.removeAllChildren();

        // Group files by repository
        for (Repository repo : reviewContext.getRepositories()) {
            DefaultMutableTreeNode repoNode = new DefaultMutableTreeNode(repo);

            // Group files by directory
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
            DefaultMutableTreeNode dirNode = findOrCreateChildNode(currentNode, dirName);
            currentNode = dirNode;
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

    public void setReviewContext(ReviewContext context) {
        this.reviewContext = context;
        buildFileTree();
    }

    public void setCommitRange(Commit startCommit, Commit endCommit) {
        this.startCommit = startCommit;
        this.endCommit = endCommit;
        // TODO: Filter files to only show those changed in this commit range
    }

    public ReviewFile getSelectedFile() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode != null && selectedNode.getUserObject() instanceof ReviewFile) {
            return (ReviewFile) selectedNode.getUserObject();
        }
        return null;
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

            if (userObject instanceof Repository) {
                Repository repo = (Repository) userObject;
                setText(repo.getName());
                setFont(getFont().deriveFont(Font.BOLD));
                setIcon(new RepositoryIcon(iconSize));
            } else if (userObject instanceof ReviewFile) {
                ReviewFile file = (ReviewFile) userObject;
                setText(file.getFileName());
                setIcon(new FileIcon(iconSize));

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

    @Override
    protected void paintComponent(Graphics g) {
        // Recreate titled border with current theme colors
        // TitledBorders are immutable and must be recreated to pick up new theme colors
        setBorder(ThemedTitledBorder.create("Files"));
        super.paintComponent(g);
    }
}


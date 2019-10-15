package cz.hudecekpetr.snowride.ui;

import com.sun.javafx.scene.control.skin.TreeViewSkin;

import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;

/**
 * A workaround to find out whether the given index is visible in tree view (#85). <br>
 * <br>
 * https://bugs.openjdk.java.net/browse/JDK-8090386
 *
 */
public class ProjectTreeViewSkin extends TreeViewSkin<HighElement> {

    public ProjectTreeViewSkin(TreeView treeView) {
        super(treeView);
    }

    public boolean isIndexVisible(int index) {
        TreeCell<HighElement> firstVisibleCell = flow.getFirstVisibleCell();
        TreeCell<HighElement> lastVisibleCell = flow.getLastVisibleCell();
        return firstVisibleCell != null && lastVisibleCell != null &&
                firstVisibleCell.getIndex() <= index && lastVisibleCell.getIndex() >= index;
    }
}

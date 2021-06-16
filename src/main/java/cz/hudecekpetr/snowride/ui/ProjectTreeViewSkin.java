package cz.hudecekpetr.snowride.ui;


import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;

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
        TreeCell<HighElement> firstVisibleCell = getVirtualFlow().getFirstVisibleCell();
        TreeCell<HighElement> lastVisibleCell = getVirtualFlow().getLastVisibleCell();
        return firstVisibleCell != null && lastVisibleCell != null &&
                firstVisibleCell.getIndex() <= index && lastVisibleCell.getIndex() >= index;
    }
}

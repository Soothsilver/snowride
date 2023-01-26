package cz.hudecekpetr.snowride.ui.popup;

import cz.hudecekpetr.snowride.fx.autocompletion.AutoCompletePopup;
import javafx.scene.Node;
import javafx.scene.control.Skin;

public class SnowPopupSkin implements Skin<SnowPopup> {

    private SnowPopup popup;
    private Node contentPane;

    public SnowPopupSkin(SnowPopup popup, Node contentPane) {
        this.popup = popup;
        this.contentPane = contentPane;
    }

    @Override
    public void dispose() {
        this.popup = null;
    }

    @Override
    public Node getNode() {
        return contentPane;
    }

    @Override
    public SnowPopup getSkinnable() {
        return popup;
    }

}

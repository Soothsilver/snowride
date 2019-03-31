package cz.hudecekpetr.snowride.ui;

import javafx.scene.control.Label;
import org.controlsfx.control.PopOver;

public class DocumentationPopOver extends PopOver {
    public DocumentationPopOver() {
        //this.setAnchorLocation(AnchorLocation.CONTENT_TOP_RIGHT);
        this.setArrowLocation(ArrowLocation.TOP_RIGHT);
        this.setCloseButtonEnabled(true);
        this.setHeaderAlwaysVisible(true);
        this.setContentNode(new Label("Documentation would be here."));
        this.setMinWidth(500);
        this.setMinHeight(500);
        this.setTitle("Documentation");
    }
}

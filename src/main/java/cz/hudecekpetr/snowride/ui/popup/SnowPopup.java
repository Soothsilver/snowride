package cz.hudecekpetr.snowride.ui.popup;

import cz.hudecekpetr.snowride.fx.ScreenEdgeAvoidance;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.stage.Window;


abstract public class SnowPopup extends PopupControl {


    public void showRightIfPossible(Window parent, double anchorLeft, double anchorWidth, double y) {
        Point2D finalPoint = ScreenEdgeAvoidance.determineStartingPositionForDocumentationPopup(anchorLeft, y, anchorWidth, new Dimension2D(450, 500));
        show(parent, finalPoint.getX(), finalPoint.getY());
    }

    public void showRightIfPossible(Region region) {
        Window parent = region.getScene().getWindow();
        showRightIfPossible(
                parent,
                parent.getX() + region.localToScene(0.0D, 0.0D).getX() + region.getScene().getX(),
                region.getWidth(),
                parent.getY() + region.localToScene(0.0D, 0.0D).getY() + region.getScene().getY() + 0
        );
    }

}

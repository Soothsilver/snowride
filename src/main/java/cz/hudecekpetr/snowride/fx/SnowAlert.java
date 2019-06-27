package cz.hudecekpetr.snowride.fx;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class SnowAlert extends Alert {
    public SnowAlert(AlertType alertType, String contentText, ButtonType... buttons) {
        super(alertType, contentText, buttons);
        CenterToParentUtility.prepareToShowAtCenterOfMainForm(this);
    }
}

package cz.hudecekpetr.snowride.ui.about;

import cz.hudecekpetr.snowride.fx.CenterToParentUtility;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public abstract class AboutDialogBase extends Stage {
    public AboutDialogBase() {
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                AboutDialogBase.this.close();
            }
        });
        CenterToParentUtility.prepareToShowAtCenterOfMainForm(this);

    }


}

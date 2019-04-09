package cz.hudecekpetr.snowride.ui.about;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public abstract class AboutDialogBase extends Stage {
    public AboutDialogBase() {
        this.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                    AboutDialogBase.this.close();
                }
            }
        });
    }
}

package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.fx.TooltipHack;
import cz.hudecekpetr.snowride.generalpurpose.GarbageCollectorCaller;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;

public class SnowrideApplication extends Application {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> {
            e.printStackTrace();
            // Internet articles say that error messages shouldn't be closed with "OK". I'm not sure "Well, that happened." is better, though.
            new Alert(Alert.AlertType.WARNING, ExceptionUtils.getMessage(e), new ButtonType("Well, that happened.")).showAndWait();
        }));
        Settings.load();
        GarbageCollectorCaller.maybeStart();
        launch(args);
    }

    public void start(Stage primaryStage) {
        MainForm mainForm = new MainForm(primaryStage);
        mainForm.show();
        try {
            mainForm.loadProjectFromFolder(new File(Settings.getInstance().lastOpenedProject));
            TooltipHack.hackDelayTimers(new Tooltip());
        } catch (Exception exception) {
            mainForm.loadProjectFromFolder(new File("."));
        }
    }
}

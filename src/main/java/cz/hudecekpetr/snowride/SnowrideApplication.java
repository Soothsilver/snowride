package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.fx.SnowAlert;
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
            new SnowAlert(Alert.AlertType.WARNING, ExceptionUtils.getMessage(e), new ButtonType("Well, that happened.")).showAndWait();
        }));
        Settings.loadPrimaryFile();
        if (args.length > 0) {
            // Load the snow file with preference
            Settings.getInstance().lastOpenedProject = args[0];
        }
        GarbageCollectorCaller.maybeStart();
        launch(args);
    }

    public void start(Stage primaryStage) {
        MainForm mainForm = new MainForm(primaryStage);
        mainForm.show();
        try {
            mainForm.loadProjectFromFolderOrSnowFile(new File(Settings.getInstance().lastOpenedProject).getAbsoluteFile());
            TooltipHack.hackDelayTimers(new Tooltip());
        } catch (Exception exception) {
            mainForm.loadProjectFromFolderOrSnowFile(new File("."));
        }
    }
}

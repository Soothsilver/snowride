package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

public class SnowrideApplication extends Application {
    public static void main(String[] args) {
        Settings.load();
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        MainForm mainForm = new MainForm(primaryStage);
        mainForm.show();
        mainForm.loadProjectFromFolder(new File(Settings.getInstance().lastOpenedProject));
    }
}

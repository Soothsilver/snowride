package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.application.Application;
import javafx.stage.Stage;

public class SnowrideApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        MainForm mainForm = new MainForm(primaryStage);
        mainForm.show();
        mainForm.loadProjectFromCurrentDir();
    }
}

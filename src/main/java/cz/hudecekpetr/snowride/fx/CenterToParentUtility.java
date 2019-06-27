package cz.hudecekpetr.snowride.fx;

import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

/**
 * The code duplication here kind of sucks but it's not my fault that Stage and Dialog have the same methods but don't
 * implement the same interface nor share a base class.
 */
public class CenterToParentUtility {
    /**
     * Prepares a not-yet-visible window to be shown on top of the center of the main form rather than in the middle of the
     * system's primary screen. Used from https://stackoverflow.com/a/40105390/1580088.
     * @param newWindow A new popup.
     */
    public static void prepareToShowAtCenterOfMainForm(Stage newWindow) {
        if (MainForm.INSTANCE != null) {
            // Calculate the center position of the parent Stage
            Stage primaryStage = MainForm.INSTANCE.getStage();
            double centerXPosition = primaryStage.getX() + primaryStage.getWidth() / 2d;
            double centerYPosition = primaryStage.getY() + primaryStage.getHeight() / 2d;

            // Hide the pop-up stage before it is shown and becomes relocated
            newWindow.setOnShowing(ev -> newWindow.hide());

            // Relocate the pop-up Stage
            newWindow.setOnShown(ev -> {
                newWindow.setX(centerXPosition - newWindow.getWidth() / 2d);
                newWindow.setY(centerYPosition - newWindow.getHeight() / 2d);
                newWindow.show();
            });
        }
    }

    /**
     * Prepares a not-yet-visible dialog to be shown on top of the center of the main form rather than in the middle of the
     * system's primary screen. Used from https://stackoverflow.com/a/40105390/1580088.
     * @param newWindow A new popup.
     */
    public static void prepareToShowAtCenterOfMainForm(Dialog<?> newWindow) {
        if (MainForm.INSTANCE != null) {
            // Calculate the center position of the parent Stage
            Stage primaryStage = MainForm.INSTANCE.getStage();
            double centerXPosition = primaryStage.getX() + primaryStage.getWidth() / 2d;
            double centerYPosition = primaryStage.getY() + primaryStage.getHeight() / 2d;
            // Relocate the pop-up Stage
            newWindow.setOnShown(ev -> {
                newWindow.setX(centerXPosition);
                newWindow.setY(centerYPosition);
            });
            newWindow.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    newWindow.setX(centerXPosition - newWindow.getWidth() / 2d);
                }
            });
            newWindow.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    newWindow.setY(centerYPosition - newWindow.getHeight() / 2d);
                }
            });
        }
    }
}

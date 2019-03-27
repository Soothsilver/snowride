package cz.hudecekpetr.snowride.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;

public class LongRunningOperation {
    public SimpleDoubleProperty progress = new SimpleDoubleProperty(-1);

    public void success(double byWhat) {
        Platform.runLater(()->{
            progress.set(progress.get() + byWhat);
        });
    }
}

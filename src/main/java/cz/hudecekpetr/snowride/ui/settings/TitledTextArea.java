package cz.hudecekpetr.snowride.ui.settings;

import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class TitledTextArea extends VBox {

    private final TextArea area;

    public TitledTextArea(String title, String defaultValue) {
        super(5);
        this.getChildren().add(new Label(title));
        area = new TextArea(defaultValue);
        area.setPrefWidth(Double.MAX_VALUE);
        area.setPrefHeight(70);
        area.setFont(MainForm.TEXT_EDIT_FONT);
        this.getChildren().add(area);
    }

    public String getText() {
        return area.getText();
    }
}

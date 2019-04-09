package cz.hudecekpetr.snowride.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class AboutKeyboardShortcuts extends Stage {
    public AboutKeyboardShortcuts() {
        Label lblShortcuts = null;
        try {
            lblShortcuts = new Label(IOUtils.resourceToString("/KeyboardShortcuts.txt", StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        VBox vbMain = new VBox(5, lblShortcuts);
        javafx.scene.control.Button bClose = new Button("Close");
        bClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });
        HBox hButtons = new HBox(5, bClose);
        hButtons.setAlignment(Pos.CENTER_RIGHT);
        VBox vbAll = new VBox(vbMain, hButtons);
        VBox.setVgrow(vbMain, Priority.ALWAYS);
        vbAll.setPadding(new Insets(5));
        this.setScene(new Scene(vbAll, 500, 400));
        this.setTitle("Keyboard shortcuts of Snowride");
        this.getIcons().add(Images.snowflake);
    }
}

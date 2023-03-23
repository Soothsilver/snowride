package cz.hudecekpetr.snowride.ui.about;

import cz.hudecekpetr.snowride.ui.Images;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Scanner;

public class AboutKeyboardShortcuts extends AboutDialogBase {
    public AboutKeyboardShortcuts() {
        Label lblShortcuts = new Label(new Scanner(AboutChangelog.class.getResourceAsStream("/KeyboardShortcuts.txt"), "UTF-8").useDelimiter("\\A").next());
        VBox vbMain = new VBox(5, lblShortcuts);
        javafx.scene.control.Button bClose = new Button("Close");
        bClose.setOnAction(event -> close());
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

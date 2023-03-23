package cz.hudecekpetr.snowride.ui.about;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.util.Scanner;

public class AboutChangelog extends AboutDialogBase {
    public AboutChangelog() {
        String content = new Scanner(AboutChangelog.class.getResourceAsStream("/Changelog.html"), "UTF-8").useDelimiter("\\A").next();
        WebView webView = new WebView();
        webView.getEngine().loadContent(content);
        Button bClose = new Button("Close");
        bClose.setOnAction(event -> close());
        HBox hButtons = new HBox(5, bClose);
        hButtons.setAlignment(Pos.CENTER_RIGHT);
        VBox vbAll = new VBox(webView, hButtons);
        VBox.setVgrow(webView, Priority.ALWAYS);
        vbAll.setPadding(new Insets(5));
        this.setScene(new Scene(vbAll, 500, 400));
        this.setTitle("Snowride changelog");
        this.getIcons().add(Images.snowflake);
    }
}

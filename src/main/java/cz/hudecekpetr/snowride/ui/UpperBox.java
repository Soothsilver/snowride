package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.fx.DocumentationHtmlConversion;
import cz.hudecekpetr.snowride.semantics.findusages.FindUsages;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.util.List;

public class UpperBox extends VBox {
    private final Label lblName;
    private final Button bFindUsages;
    private ContextMenu findUsagesContextMenu;
    private HighElement forElement;
    private WebView webView;

    public UpperBox() {
        lblName = new Label("Test or keyword name here");
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14pt;");

        bFindUsages = new Button("Find usages");
        bFindUsages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (forElement instanceof Scenario) {
                    MenuItem placeholder = new MenuItem("(this keyword is not used anywhere)");
                    placeholder.setDisable(true);
                    List<MenuItem> items = FindUsages.findUsages(null, ((Scenario) forElement), MainForm.INSTANCE.getRootElement());
                    if (findUsagesContextMenu != null) {
                        findUsagesContextMenu.hide();
                    }
                    if (items.size() == 0) {
                        findUsagesContextMenu = new ContextMenu(placeholder);
                    } else {
                        findUsagesContextMenu = new ContextMenu(items.toArray(new MenuItem[0]));
                    }
                    findUsagesContextMenu.show(bFindUsages, Side.BOTTOM, 0, 0);
                }
            }
        });
        webView = new WebView();
        webView.setBlendMode(BlendMode.DARKEN);
        webView.setMaxHeight(200);
        HBox hboxNameAndFindUsages = new HBox(10d, lblName, bFindUsages);
        hboxNameAndFindUsages.setPadding(new Insets(5,0,0,5));
        getChildren().addAll(hboxNameAndFindUsages, webView);
    }

    public void update(HighElement value) {
        lblName.setText(value.getShortName());
        this.forElement = value;
        bFindUsages.setVisible(value instanceof Scenario && !((Scenario) value).isTestCase());
        webView.getEngine().loadContent("<a style='color: blue; text-decoration: underline; font-size: 8pt;'>(edit documentation...)</a> " + DocumentationHtmlConversion.robotToHtml(value.getFullDocumentation(), 8));
    }
}

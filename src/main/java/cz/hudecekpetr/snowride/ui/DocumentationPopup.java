package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;

public class DocumentationPopup extends PopupWindow {

    private final Label keyword_name;
    private final Label keyword_source;
    private final Label keyword_documentation;

    public void setData(IAutocompleteOption option) {
        keyword_name.setText(option.getAutocompleteText());
        keyword_source.setText(option.getItalicsSubheading());
        keyword_documentation.setText(option.getFullDocumentation());
    }


    public DocumentationPopup() {
        this.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
        Label documentation = new Label("Documentation");
        keyword_name = new Label("No Operation");
        keyword_name.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");
        keyword_source = new Label("Built-in keyword (library BuiltIn)");
        keyword_source.setStyle("-fx-font-style: italic;");
        keyword_documentation = new Label("Does absolutely nothing.");
        keyword_documentation.setWrapText(true);
        VBox documentationPane = new VBox(documentation, keyword_name,
                keyword_source, keyword_documentation);
        documentationPane.setStyle("-fx-background-color: whitesmoke;");
        documentationPane.setMinWidth(300);
        documentationPane.setMinHeight(200);
        documentationPane.setMaxWidth(300);
        documentationPane.setMaxHeight(400);
        documentationPane.setPadding(new Insets(3));
        this.setConsumeAutoHidingEvents(false);
        this.getContent().add(documentationPane);
    }
}

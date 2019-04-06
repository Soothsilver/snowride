package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.fx.DocumentationTextArea;
import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import org.fxmisc.flowless.VirtualizedScrollPane;


public class DocumentationPopup extends Popup {

    private final Label keyword_name;
    private final Label keyword_source;
    private final ImageView icon;
    private final DocumentationTextArea keyword_documentation;

    public void setData(IAutocompleteOption option) {
        keyword_name.setText(option.getAutocompleteText());
        keyword_source.setText(option.getItalicsSubheading());
        icon.setImage(option.getAutocompleteIcon());
        keyword_documentation.setDocumentation(option.getFullDocumentation());
    }


    public DocumentationPopup() {
        this.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
        Label documentation = new Label("Documentation");
        keyword_name = new Label("No Operation");
        keyword_name.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");
        keyword_source = new Label("Built-in keyword (library BuiltIn)");
        keyword_source.setStyle("-fx-font-style: italic; -fx-font-size: 8pt;");
        keyword_documentation = new DocumentationTextArea();
        keyword_documentation.setWrapText(true);
        icon = new ImageView(Images.stop);
        VirtualizedScrollPane<DocumentationTextArea> vPane = new VirtualizedScrollPane<>(keyword_documentation);
        VBox documentationPane = new VBox(documentation, keyword_name,
                new HBox(5, icon, keyword_source), vPane);
        VBox.setVgrow(vPane, Priority.ALWAYS);
        documentationPane.setStyle("-fx-background-color: whitesmoke;");
        documentationPane.setMinWidth(400);
        documentationPane.setMinHeight(350);
        documentationPane.setMaxWidth(400);
        documentationPane.setMaxHeight(350);
        documentationPane.setPadding(new Insets(6));
        this.setConsumeAutoHidingEvents(false);
        this.getContent().add(documentationPane);
    }
}

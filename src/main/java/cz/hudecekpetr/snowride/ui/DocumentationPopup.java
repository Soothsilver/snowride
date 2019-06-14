package cz.hudecekpetr.snowride.ui;

import com.sun.javafx.util.Utils;
import cz.hudecekpetr.snowride.fx.DocumentationTextArea;
import cz.hudecekpetr.snowride.fx.IHasQuickDocumentation;
import cz.hudecekpetr.snowride.fx.ScreenEdgeAvoidance;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;


public class DocumentationPopup extends Popup {

    private final Label keyword_name;
    private final Label keyword_source;
    private final ImageView icon;
    private final DocumentationTextArea keyword_documentation;

    public void setData(IHasQuickDocumentation option) {
        keyword_name.setText(option.getQuickDocumentationCaption());
        keyword_source.setText(option.getItalicsSubheading());
        icon.setImage(option.getAutocompleteIcon());
        keyword_documentation.setDocumentation(option.getFullDocumentation());
    }


    public DocumentationPopup() {
        this.setAutoFix(false);
        this.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
        keyword_name = new Label("No Operation");
        keyword_name.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");
        keyword_source = new Label("Built-in keyword (library BuiltIn)");
        keyword_source.setStyle("-fx-font-style: italic; -fx-font-size: 10pt;");
        keyword_documentation = new DocumentationTextArea();
        keyword_documentation.setWrapText(true);
        icon = new ImageView(Images.stop);
        VirtualizedScrollPane<DocumentationTextArea> vPane = new VirtualizedScrollPane<>(keyword_documentation);
        VBox documentationPane = new VBox(keyword_name,
                new HBox(5, icon, keyword_source), vPane);
        VBox.setVgrow(vPane, Priority.ALWAYS);
        documentationPane.setStyle("-fx-background-color: whitesmoke;");
        documentationPane.setMinWidth(450);
        documentationPane.setMinHeight(500);
        documentationPane.setMaxWidth(450);
        documentationPane.setMaxHeight(500);
        documentationPane.setPadding(new Insets(6));
        this.setConsumeAutoHidingEvents(false);
        this.getContent().add(documentationPane);
    }

    public void showRightIfPossible(Window parent, double anchorLeft, double anchorWidth, double y) {
        Point2D finalPoint = ScreenEdgeAvoidance.determineStartingPositionForDocumentationPopup(anchorLeft, y, anchorWidth, new Dimension2D(450, 500));
        show(parent, finalPoint.getX(), finalPoint.getY());
    }
}

package cz.hudecekpetr.snowride.ui.upperbox;

import cz.hudecekpetr.snowride.fx.DocumentationTextArea;
import cz.hudecekpetr.snowride.ui.grid.SnowTableView;
import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.semantics.findusages.FindUsages;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.util.List;

public class UpperBox extends VBox {
    private final Label lblName;
    private final Button bFindUsages;
    private ContextMenu findUsagesContextMenu;
    private HighElement forElement;
    private DocumentationTextArea documentationTextArea;

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
        documentationTextArea = new DocumentationTextArea();
        UpperBoxBinding theBinding = new UpperBoxBinding(this, documentationTextArea.totalHeightEstimateProperty());
        documentationTextArea.minHeightProperty().bind(theBinding);
        documentationTextArea.prefHeightProperty().bind(theBinding);
        documentationTextArea.maxHeightProperty().bind(theBinding);
        VirtualizedScrollPane<DocumentationTextArea> vPane = new VirtualizedScrollPane<>(documentationTextArea);
        vPane.minHeightProperty().bind(theBinding);
        vPane.prefHeightProperty().bind(theBinding);
        vPane.maxHeightProperty().bind(theBinding);
        //vPane.prefHeightProperty().bind(theBinding);
        HBox hboxNameAndFindUsages = new HBox(10d, lblName, bFindUsages);
        hboxNameAndFindUsages.setPadding(new Insets(5, 0, 0, 5));
        Hyperlink bEditDocumentation = new Hyperlink("(edit documentation...)");
        bEditDocumentation.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DocumentationEditWindow editWindow = new DocumentationEditWindow(forElement);
                editWindow.showAndWait();
            }
        });
        Hyperlink bEditTags = new Hyperlink("(edit tags)");
        bEditTags.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (forElement == null) {
                    return;
                }
                if (forElement instanceof Scenario) {
                    LogicalLine line = ((Scenario) forElement).findLineWithTags();
                    SnowTableView table = MainForm.INSTANCE.gridTab.getSpreadsheetViewTable();
                    if (line == null) {
                        line = table.createNewLine();
                        table.getItems().add(0, line);
                        line.getCellAsStringProperty(1, MainForm.INSTANCE).set(new Cell("[Tags]", "    ", line));
                        line.getCellAsStringProperty(2, MainForm.INSTANCE).set(new Cell("", "    ", line));
                    }
                    table.getSelectionModel().clearAndSelect(line.lineNumber.intValue(), table.getVisibleLeafColumn(2));
                    table.requestFocus();
                } else {
                    LogicalLine line = ((Suite) forElement).findLineWithTags();
                    SnowTableView table = MainForm.INSTANCE.gridTab.getTableSettings();
                    if (line == null) {
                        line = table.createNewLine();
                        table.getItems().add(0, line);
                        line.getCellAsStringProperty(0, MainForm.INSTANCE).set(new Cell("Force Tags", "    ", line));
                        line.getCellAsStringProperty(1, MainForm.INSTANCE).set(new Cell("", "    ", line));
                    }
                    table.getSelectionModel().clearAndSelect(line.lineNumber.intValue(), table.getVisibleLeafColumn(2));
                    table.requestFocus();

                }
            }
        });
        HBox hEdit = new HBox(5d, bEditDocumentation, bEditTags);
        getChildren().addAll(hboxNameAndFindUsages, vPane, hEdit);
    }

    public void update(HighElement value) {
        lblName.setText(value.getShortName());
        this.forElement = value;
        bFindUsages.setVisible(value instanceof Scenario && !((Scenario) value).isTestCase());
        documentationTextArea.setDocumentation(value.getFullDocumentation().trim());
    }

    public void updateSelf() {
        if (this.forElement != null) {
            this.update(forElement);
        }
    }

    private class UpperBoxBinding extends DoubleBinding {

        private UpperBox box;
        private final ObservableValue<Double> estimatedHeight;
        private double lastKnownHeight = 0;

        UpperBoxBinding(UpperBox box, ObservableValue<Double> estimatedHeight) {
            this.box = box;
            this.estimatedHeight = estimatedHeight;
            super.bind(estimatedHeight);
        }

        @Override
        protected double computeValue() {
            System.out.println(box.toString() + "'s height wants to become " + estimatedHeight.getValue());
            Double originValue = estimatedHeight.getValue();
            if (originValue != null && originValue != 0) {
                lastKnownHeight = originValue;
            }
            return Math.min(200.0, lastKnownHeight);
        }


        @Override
        public void dispose() {
            super.unbind(estimatedHeight);
        }


        @Override
        public ObservableList<?> getDependencies() {
            return
                    FXCollections.singletonObservableList(estimatedHeight);
        }
    }
}
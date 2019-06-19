package cz.hudecekpetr.snowride.ui.upperbox;

import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.fx.grid.SnowTableKind;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.ui.about.AboutDialogBase;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

public class DocumentationEditWindow extends AboutDialogBase {
    public DocumentationEditWindow(HighElement element) {
        LogicalLine line = element.getDocumentationLine();
        Button bClose = new Button("OK");
        TextArea documentationArea = new TextArea();
        bClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clickOK(line, element, documentationArea);
            }
        });
        this.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                    clickOK(line, element, documentationArea);
                }
            }
        });
        HBox hButtons = new HBox(5, bClose);
        hButtons.setAlignment(Pos.CENTER_RIGHT);
        documentationArea.setWrapText(true);
        documentationArea.setStyle("-fx-background-color: -fx-control-inner-background");
        if (line == null) {
            documentationArea.setText("");
        } else {
            documentationArea.setText(line.cells.stream().skip(element instanceof Scenario ? 2 : 1).map(cell -> cell.contents).collect(Collectors.joining("\n")).trim());
        }
        documentationArea.selectEnd();
        VBox vbAll = new VBox(5, documentationArea, hButtons);
        VBox.setVgrow(documentationArea, Priority.ALWAYS);
        vbAll.setPadding(new Insets(5));
        this.setScene(new Scene(vbAll, 500, 400));
        this.setTitle("Edit documentation");
        this.getIcons().add(Images.snowflake);
    }

    private void clickOK(LogicalLine line, HighElement element, TextArea documentationArea) {
        LogicalLine editedLine = line;
        int docStartsAt = element instanceof Scenario ? 2 : 1;
        if (editedLine == null) {
            ObservableList<LogicalLine> allLogicalLines = null;
            if (element instanceof Scenario) {
                allLogicalLines = ((Scenario) element).getLines();
            } else {
                allLogicalLines = ((Suite) element).fileParsed.findOrCreateSettingsSection().pairs;
            }
            LogicalLine newLine = new LogicalLine();
            newLine.setBelongsToHighElement(element);
            newLine.lineNumber = new PositionInListProperty<>(newLine, allLogicalLines);
            newLine.belongsWhere = (element instanceof Scenario ? SnowTableKind.SCENARIO : SnowTableKind.SETTINGS);
            newLine.recalcStyles();
            allLogicalLines.add(0, newLine);
            newLine.getCellAsStringProperty(docStartsAt - 1, MainForm.INSTANCE).set(new Cell(element instanceof Scenario ? "[Documentation]" : "Documentation", "    ", newLine));
            editedLine = newLine;
        }
        String[] lines = StringUtils.splitByWholeSeparatorPreserveAllTokens(documentationArea.getText().trim(), "\n");
        String ellipsisSeparator = element instanceof Scenario ? "\n    ...    " : "\n...    ";
        // Erase existing documentation:
        for (int i = docStartsAt; i < editedLine.cells.size(); i++) {
            editedLine.getCellAsStringProperty(i, MainForm.INSTANCE).set(new Cell("", "    ", editedLine));
        }
        // Add new one
        for (int i = 0; i < lines.length; i++) {
            String elSeparatorFinally = ellipsisSeparator;
            if (i == lines.length -1) {
                elSeparatorFinally = "";
            }
            editedLine.getCellAsStringProperty(docStartsAt + i, MainForm.INSTANCE).set(new Cell(lines[i].trim(), elSeparatorFinally, editedLine));
        }
        element.markAsStructurallyChanged(MainForm.INSTANCE);
        MainForm.INSTANCE.gridTab.upperBox.updateSelf();
        MainForm.INSTANCE.gridTab.upperBox2.updateSelf();
        close();
    }
}

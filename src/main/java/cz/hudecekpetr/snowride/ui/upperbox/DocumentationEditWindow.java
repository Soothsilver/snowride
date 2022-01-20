package cz.hudecekpetr.snowride.ui.upperbox;

import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Scenario;
import cz.hudecekpetr.snowride.tree.highelements.Suite;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import cz.hudecekpetr.snowride.ui.about.AboutDialogBase;
import cz.hudecekpetr.snowride.ui.grid.SnowTableKind;
import cz.hudecekpetr.snowride.undo.AddRowOperation;
import cz.hudecekpetr.snowride.undo.ChangeTextOperation;
import cz.hudecekpetr.snowride.undo.MassOperation;
import cz.hudecekpetr.snowride.undo.UndoableOperation;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentationEditWindow extends AboutDialogBase {
    public DocumentationEditWindow(HighElement element) {
        LogicalLine line = element.getDocumentationLine();
        Button bClose = new Button("OK");
        TextArea documentationArea = new TextArea();
        bClose.setOnAction(event -> clickOK(line, element, documentationArea));
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && event.isShortcutDown()) {
                clickOK(line, element, documentationArea);
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
        int docStartsAt = element instanceof Scenario ? 2 : 1;

        List<UndoableOperation> operations = new LinkedList<>();
        ObservableList<LogicalLine> lines = element instanceof Scenario ? ((Scenario) element).getLines()
                : ((Suite) element).fileParsed.findOrCreateSettingsSection().getPairs();

        if (line == null || !lines.contains(line)) {
            line = LogicalLine.createEmptyLine((element instanceof Scenario ? SnowTableKind.SCENARIO : SnowTableKind.SETTINGS), element, lines);
            lines.add(0, line);
            String contents = element instanceof Scenario ? "[Documentation]" : "Documentation";
            line.getCellAsStringProperty(docStartsAt - 1, MainForm.INSTANCE).set(new Cell(contents, "    ", line));
            operations.add(new AddRowOperation(lines, 0, element));
            operations.add(new ChangeTextOperation(lines, "", contents, "    ", 0, docStartsAt - 1));
        }

        String[] documentationAreaLines = StringUtils.splitByWholeSeparatorPreserveAllTokens(documentationArea.getText().trim(), "\n");
        String ellipsisSeparator = element instanceof Scenario ? "\n    ...    " : "\n...    ";
        int lineIndex = lines.indexOf(line);

        // Erase existing documentation
        for (int i = docStartsAt; i < line.cells.size(); i++) {
            SimpleObjectProperty<Cell> cell = line.getCellAsStringProperty(i, MainForm.INSTANCE);
            operations.add(new ChangeTextOperation(lines, cell.getValue().contents, "", "    ", lineIndex, i));
            cell.set(new Cell("", "    ", line));
        }

        // Add new documentation
        for (int i = 0; i < documentationAreaLines.length; i++) {
            String postTrivia = (i == documentationAreaLines.length - 1) ? "" : ellipsisSeparator;
            SimpleObjectProperty<Cell> cell = line.getCellAsStringProperty(docStartsAt + i, MainForm.INSTANCE);
            String contents = documentationAreaLines[i].trim();
            operations.add(new ChangeTextOperation(lines, "", contents, postTrivia, lineIndex, docStartsAt + i));
            cell.set(new Cell(contents, postTrivia, line));
        }

        // undoStack
        if (!operations.isEmpty()) {
            element.getUndoStack().iJustDid(new MassOperation(operations));
        }

        element.markAsStructurallyChanged(MainForm.INSTANCE);
        MainForm.INSTANCE.gridTab.upperBox.updateSelf();
        MainForm.INSTANCE.gridTab.upperBox2.updateSelf();
        close();
        MainForm.INSTANCE.gridTab.requestFocus();
    }
}

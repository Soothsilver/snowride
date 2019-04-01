package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.grid.SnowTableView;
import cz.hudecekpetr.snowride.tree.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.GridView;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import java.util.List;
import java.util.stream.Collectors;

public class GridTab {

    Label lblParseError;
    SnowTableView spreadsheetView;
    SplitPane suiteView;
    private MainForm mainForm;

    public Tab getTabGrid() {
        return tabGrid;
    }

    private Tab tabGrid;


    public GridTab(MainForm mainForm) {
        this.mainForm = mainForm;
        lblParseError = new Label("No file loaded yet.");
        spreadsheetView = new SnowTableView(mainForm);
        SnowTableView tableSettings = new SnowTableView(mainForm);
        SnowTableView tableVariables = new SnowTableView(mainForm);
        Label settings = new Label("Settings");
        settings.setPadding(new Insets(5, 0, 0, 5));
        VBox vboxSettings = new VBox(5, settings, tableSettings);
        VBox.setVgrow(tableSettings, Priority.ALWAYS);
        VBox vboxVariables = new VBox(5, new Label("Variables"), tableVariables);
        VBox.setVgrow(tableVariables, Priority.ALWAYS);
        suiteView = new SplitPane(vboxSettings, vboxVariables);
        suiteView.setOrientation(Orientation.VERTICAL);
    }

    public Tab createTab() {
        tabGrid = new Tab("Assisted grid editing");
        tabGrid.setClosable(false);
        tabGrid.setContent(lblParseError);
        return tabGrid;
    }

    public void loadElement(HighElement value) {
        tabGrid.setContent(lblParseError);
        if (value instanceof FolderSuite) {
            FolderSuite fsuite = (FolderSuite) value;
            if (fsuite.getInitFileParsed() != null && fsuite.getInitFileParsed().errors.size() > 0) {
                setParseErrors(fsuite.getInitFileParsed().errors);
            } else {
                loadSuiteTables(fsuite);
            }
        } else if (value instanceof FileSuite) {
            FileSuite fsuite = (FileSuite)value;
            if (fsuite.fileParsed.errors.size() > 0) {
                setParseErrors(fsuite.fileParsed.errors);
            } else {
                loadSuiteTables(fsuite);
            }
        } else if (value instanceof Scenario) {
            tabGrid.setContent(spreadsheetView);
            spreadsheetView.setScenario(value);
            spreadsheetView.loadLines(((Scenario)value).getLines());
        } else {
            lblParseError.setText("Unknown high element.");
        }
    }

    private void loadSuiteTables(ISuite fsuite) {
        tabGrid.setContent(suiteView);
    }

    private void setParseErrors(List<Exception> errors) {
        if (errors.size() > 0) {
            this.lblParseError.setText("Parser error count: " + errors.size() + "\n" + errors.stream().map(Extensions::toStringWithTrace).collect(Collectors.joining("\n")));
        } else {
            this.lblParseError.setText("Clear of parse errors.");
        }
        tabGrid.setContent(lblParseError);
    }


}

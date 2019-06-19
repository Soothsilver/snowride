package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.grid.SnowTableKind;
import cz.hudecekpetr.snowride.fx.grid.SnowTableView;
import cz.hudecekpetr.snowride.semantics.findusages.FindUsages;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.RobotFile;
import cz.hudecekpetr.snowride.tree.Scenario;
import cz.hudecekpetr.snowride.tree.Suite;
import cz.hudecekpetr.snowride.ui.upperbox.UpperBox;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.stream.Collectors;

public class GridTab {

    public SnowTableView getTableSettings() {
        return tableSettings;
    }

    private final SnowTableView tableSettings;
    private final SnowTableView tableVariables;
    Label lblParseError;

    public SnowTableView getSpreadsheetViewTable() {
        return spreadsheetViewTable;
    }

    SnowTableView spreadsheetViewTable;
    VBox suiteView;
    VBox spreadsheetView;
    private Tab tabGrid;
    public final UpperBox upperBox;
    public final UpperBox upperBox2;

    public GridTab(MainForm mainForm) {
        lblParseError = new Label("No file loaded yet.");
        spreadsheetViewTable = new SnowTableView(mainForm, SnowTableKind.SCENARIO);
        upperBox = new UpperBox();
        upperBox2 = new UpperBox();
        spreadsheetView = new VBox(5, upperBox, spreadsheetViewTable);

        VBox.setVgrow(spreadsheetViewTable, Priority.ALWAYS);
        tableSettings = new SnowTableView(mainForm, SnowTableKind.SETTINGS);
        tableVariables = new SnowTableView(mainForm, SnowTableKind.VARIABLES);
        Label settings = new Label("Settings");
        settings.setPadding(new Insets(5, 0, 0, 5));
        VBox vboxSettings = new VBox(5, settings, tableSettings);
        VBox.setVgrow(tableSettings, Priority.ALWAYS);
        Label labelVariables = new Label("Variables");
        labelVariables.setPadding(new Insets(5, 0, 0, 5));
        VBox vboxVariables = new VBox(5, labelVariables, tableVariables);
        VBox.setVgrow(tableVariables, Priority.ALWAYS);
        SplitPane suiteViewSplitPane = new SplitPane(vboxSettings, vboxVariables);
        suiteViewSplitPane.setOrientation(Orientation.VERTICAL);
        suiteView = new VBox(5, upperBox2, suiteViewSplitPane);
        VBox.setVgrow(suiteViewSplitPane, Priority.ALWAYS);
    }

    public Tab getTabGrid() {
        return tabGrid;
    }

    public Tab createTab() {
        tabGrid = new Tab("Assisted grid editing");
        tabGrid.setClosable(false);
        tabGrid.setContent(lblParseError);
        return tabGrid;
    }

    public void loadElement(HighElement value) {
        if (value != null) {
            value.asSuite().reparseAndRecalculateResources();
            upperBox2.update(value);
            upperBox.update(value);
        }
        if (value instanceof FolderSuite) {
            FolderSuite fsuite = (FolderSuite) value;
            if (fsuite.getInitFileParsed() != null && fsuite.getInitFileParsed().errors.size() > 0) {
                setParseErrors(fsuite.getInitFileParsed().errors);
            } else {
                loadSuiteTables(fsuite);
            }
        } else if (value instanceof FileSuite) {
            FileSuite fsuite = (FileSuite) value;
            if (fsuite.fileParsed.errors.size() > 0) {
                setParseErrors(fsuite.fileParsed.errors);
            } else {
                loadSuiteTables(fsuite);
            }
        } else if (value instanceof Scenario) {
            spreadsheetViewTable.loadLines(value, ((Scenario) value).getLines());
            tabGrid.setContent(spreadsheetView);
        } else {
            tabGrid.setContent(lblParseError);
            lblParseError.setText("No element is loaded.");
            tabGrid.setContent(lblParseError);
        }
    }

    private void loadSuiteTables(Suite fsuite) {
        if (fsuite.fileParsed == null) {
            fsuite.fileParsed = new RobotFile();
        }
        tableSettings.loadLines(fsuite, fsuite.fileParsed.findOrCreateSettingsSection().pairs);
        tableVariables.loadLines(fsuite, fsuite.fileParsed.findOrCreateVariablesSection().pairs);

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

package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.grid.SnowTableView;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.Scenario;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.controlsfx.control.GridView;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import java.util.List;
import java.util.stream.Collectors;

public class GridTab {

    Label lblParseError;
    SnowTableView spreadsheetView;
    private MainForm mainForm;

    public Tab getTabGrid() {
        return tabGrid;
    }

    private Tab tabGrid;


    public GridTab(MainForm mainForm) {
        this.mainForm = mainForm;
        lblParseError = new Label("No file loaded yet.");
        spreadsheetView = new SnowTableView();
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
            if (fsuite.getInitFileParsed() != null) {
                setParseErrors(fsuite.getInitFileParsed().errors);
            } else {
                lblParseError.setText("No init file at this node.");
            }
        } else if (value instanceof FileSuite) {
            setParseErrors(((FileSuite)value).getFileParsed().errors);
        } else if (value instanceof Scenario) {
            tabGrid.setContent(spreadsheetView);
            spreadsheetView.loadLines(((Scenario)value).getLines());
        } else {
            lblParseError.setText("Unknown high element.");
        }
    }

    private void setParseErrors(List<Exception> errors) {
        if (errors.size() > 0) {
            this.lblParseError.setText("Parser error count: " + errors.size() + "\n" + errors.stream().map(Extensions::toStringWithTrace).collect(Collectors.joining("\n")));
        } else {
            this.lblParseError.setText("Clear of parse errors.");
        }

    }


}

package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import cz.hudecekpetr.snowride.tree.TestCase;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class GridTab {

    Label lblParseError;
    private MainForm mainForm;


    public GridTab(MainForm mainForm) {
        this.mainForm = mainForm;
        lblParseError = new Label("No file loaded yet.");
    }

    public Tab createTab() {
        Tab tabGrid = new Tab("Assisted grid editing");
        tabGrid.setClosable(false);
        tabGrid.setContent(lblParseError);
        return tabGrid;
    }

    public void loadElement(HighElement value) {
        if (value instanceof FolderSuite) {
            FolderSuite fsuite = (FolderSuite) value;
            if (fsuite.getInitFileParsed() != null) {
                setParseErrors(fsuite.getInitFileParsed().errors);
            } else {
                lblParseError.setText("No init file at this node.");
            }
        } else if (value instanceof FileSuite) {
            setParseErrors(((FileSuite)value).getFileParsed().errors);
        } else if (value instanceof TestCase) {
            lblParseError.setText("Test cases are not supported.");
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

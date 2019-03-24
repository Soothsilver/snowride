package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class SerializingTab {
    private TextArea tbWouldSerializeTo;
    private Tab t;
    private MainForm mainForm;

    public SerializingTab(MainForm mainForm) {
        this.mainForm = mainForm;
        tbWouldSerializeTo = new TextArea();
        tbWouldSerializeTo.setFont(MainForm.TEXT_EDIT_FONT);
    }

    public Tab createTab() {
        t = new Tab("Would serialize to", tbWouldSerializeTo);
        t.setClosable(false);
        return t;
    }


    public void selTabChanged(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
        if (newValue == t) {
            HighElement value = mainForm.getProjectTree().getFocusModel().getFocusedItem().getValue();
            loadElement(value);
        }
    }

    public void loadElement(HighElement value) {
        if (value instanceof FileSuite) {
            FileSuite fs = (FileSuite)value;
            try {
                this.tbWouldSerializeTo.setText(fs.serialize());
            } catch (Exception exc) {
                this.tbWouldSerializeTo.setText(Extensions.toStringWithTrace(exc));
            }
        }
    }
}

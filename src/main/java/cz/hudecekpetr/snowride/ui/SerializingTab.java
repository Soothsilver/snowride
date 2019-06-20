package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.tree.highelements.FileSuite;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SerializingTab {
    private Label lblPerfect;
    private TextArea tbWouldSerializeTo;
    private Tab t;
    private MainForm mainForm;

    public SerializingTab(MainForm mainForm) {
        this.mainForm = mainForm;
        lblPerfect = new Label("...");
        tbWouldSerializeTo = new TextArea();
        tbWouldSerializeTo.setFont(MainForm.TEXT_EDIT_FONT);
    }

    public Tab createTab() {
        VBox content = new VBox(tbWouldSerializeTo, lblPerfect);
        VBox.setVgrow(tbWouldSerializeTo, Priority.ALWAYS);
        t = new Tab("Would serialize to", content);
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
                String afterSerialization = fs.serialize().replace("\r", "");
                this.tbWouldSerializeTo.setText(afterSerialization);
                if (afterSerialization.equals(Extensions.removeFinalNewlineIfAny(fs.contents.replace("\r", "")))) {
                    lblPerfect.setText("Serialization is perfect.");
                } else {
                    lblPerfect.setText("There are problems with serializations. You should probably not save your changes.");
                }
            } catch (Exception exc) {
                this.tbWouldSerializeTo.setText(Extensions.toStringWithTrace(exc));
            }
        }
    }
}

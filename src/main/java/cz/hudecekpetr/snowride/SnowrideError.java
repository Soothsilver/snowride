package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.controlsfx.validation.Severity;

public class SnowrideError {
    public SimpleObjectProperty<HighElement> where;
    public SimpleStringProperty description;
    public SimpleObjectProperty<Severity> severity;
}

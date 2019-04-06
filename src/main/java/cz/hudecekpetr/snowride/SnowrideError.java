package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.controlsfx.validation.Severity;

public class SnowrideError {
    public SimpleObjectProperty<HighElement> where = new SimpleObjectProperty<>();
    public SimpleStringProperty description = new SimpleStringProperty();
    public SimpleObjectProperty<Severity> severity = new SimpleObjectProperty<>();
    public SimpleObjectProperty<ErrorKind> type = new SimpleObjectProperty<>();

    public SnowrideError(HighElement where, ErrorKind type, Severity severity, String description) {
        this.where.setValue(where);
        this.description.setValue(description);
        this.severity.set(severity);
        this.type.set(type);
    }
}

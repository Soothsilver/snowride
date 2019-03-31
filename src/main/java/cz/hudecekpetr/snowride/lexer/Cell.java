package cz.hudecekpetr.snowride.lexer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;

public class Cell {

    public final String contents;
    public String postTrivia;
    public boolean virtual;

    public Cell(String contents, String postTrivia) {

        this.contents = contents;
        this.postTrivia = postTrivia;
    }

    @Override
    public String toString() {
        return contents;
    }
}

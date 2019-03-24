package cz.hudecekpetr.snowride.lexer;

import javafx.beans.property.SimpleStringProperty;

public class Cell {

    public final String contents;
    public final String postTrivia;
    public final SimpleStringProperty contentsProperty = new SimpleStringProperty("");

    public Cell(String contents, String postTrivia) {

        this.contents = contents;
        this.postTrivia = postTrivia;
        this.contentsProperty.set(contents);
    }

    @Override
    public String toString() {
        return contents;
    }
}

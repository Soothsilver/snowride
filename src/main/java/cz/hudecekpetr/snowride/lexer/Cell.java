package cz.hudecekpetr.snowride.lexer;

public class Cell {

    public final String contents;
    public final String postTrivia;

    public Cell(String contents, String postTrivia) {

        this.contents = contents;
        this.postTrivia = postTrivia;
    }
}

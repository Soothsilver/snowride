package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scenario extends HighElement {

    private final Cell nameCell;
    private boolean isTestCase;

    public List<LogicalLine> getLines() {
        return lines;
    }

    private final List<LogicalLine> lines;
    private final String postTrivia;

    public Scenario(Cell nameCell, boolean isTestCase, List<LogicalLine> lines, String postTrivia) {
        super(nameCell.contents, null, new ArrayList<>());

        this.nameCell = nameCell;
        this.setTestCase(isTestCase);
        this.lines = lines;
        for (int i = 0; i < lines.size(); i++) {
            this.lines.get(i).lineNumber.set(i+1);
        }
        this.postTrivia = postTrivia;
    }

    @Override
    public void saveAll() throws IOException {
        // Saved as part of the file suite.
    }

    public void serializeInto(StringBuilder sb) {
        sb.append(nameCell.contents);
        sb.append(nameCell.postTrivia);
        lines.forEach(ll -> {
            ll.serializeInto(sb);
        });
        sb.append(postTrivia);
    }

    public boolean isTestCase() {
        return isTestCase;
    }

    public void setTestCase(boolean testCase) {
        isTestCase = testCase;
        if (isTestCase) {
            this.imageView.setImage(Images.testIcon);
            this.checkbox.setVisible(true);
        } else {
            this.imageView.setImage(Images.keywordIcon);
            this.checkbox.setVisible(false);
        }
    }

    @Override
    public String toString() {
        return (isTestCase ? "[test]" : "[keyword]") + " " + shortName;
    }

    @Override
    public Image getAutocompleteIcon() {
        return this.isTestCase ? Images.testIcon : Images.keywordIcon;
    }
}

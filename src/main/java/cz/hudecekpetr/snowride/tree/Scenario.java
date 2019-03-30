package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scenario extends HighElement {

    private final Cell nameCell;
    private boolean isTestCase;

    public ObservableList<LogicalLine> getLines() {
        return lines;
    }

    private final ObservableList<LogicalLine> lines;

    public Scenario(Cell nameCell, boolean isTestCase, List<LogicalLine> lines) {
        super(nameCell.contents, null, new ArrayList<>());

        this.nameCell = nameCell;
        this.setTestCase(isTestCase);
        this.lines = FXCollections.observableList(lines);
        for (int i = 0; i < lines.size(); i++) {
            this.lines.get(i).lineNumber.set(i+1);
        }
    }

    @Override
    public void saveAll() throws IOException {
        // Saved as part of the file suite.
    }

    @Override
    public void deleteSelf() {
        // TODO element deletion
        throw new RuntimeException("Deleting tests and keywords is not yet implemented.");
    }

    @Override
    public void renameSelfTo(String newName) {
        // TODO element renam
        throw new NotImplementedException("Deleting tests not yet implemented.");
    }

    @Override
    public void applyAndValidateText() {
        this.parent.applyAndValidateText();
    }

    public void serializeInto(StringBuilder sb) {
        sb.append(nameCell.contents);
        sb.append(nameCell.postTrivia);
        lines.forEach(ll -> {
            ll.serializeInto(sb);
        });
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

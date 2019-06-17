package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.lexer.Cell;

import java.util.List;
import java.util.Map;

public class CellSemantics {
    public int cellIndex;
    public boolean isComment;
    public boolean isKeyword;
    public List<IKnownKeyword> permissibleKeywords;
    public Map<String, IKnownKeyword> permissibleKeywordsByInvariantName;
    public IKnownKeyword thisHereKeyword;
    public Cell.ArgumentStatus argumentStatus = Cell.ArgumentStatus.UNKNOWN;

    public CellSemantics(int cellIndex) {
        this.cellIndex = cellIndex;
    }

}

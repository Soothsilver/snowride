package cz.hudecekpetr.snowride.semantics;

import com.google.common.collect.Multimap;
import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.tree.Cell;

import java.util.List;

public class CellSemantics {
    public int cellIndex;
    public boolean isComment;
    public boolean isKeyword;
    public boolean isVariable;
    public List<IKnownKeyword> permissibleKeywords;
    public List<? extends IAutocompleteOption> variablesList;
    public Multimap<String, IKnownKeyword> permissibleKeywordsByInvariantName;
    public IKnownKeyword thisHereKeyword;
    public Cell.ArgumentStatus argumentStatus = Cell.ArgumentStatus.UNKNOWN;

    public CellSemantics(int cellIndex) {
        this.cellIndex = cellIndex;
    }

}

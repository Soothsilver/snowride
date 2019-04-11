package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.AutoCompletionBinding;
import cz.hudecekpetr.snowride.fx.AutoCompletionTextFieldBinding;
import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.grid.IceCell;
import cz.hudecekpetr.snowride.fx.grid.SnowTableKind;
import cz.hudecekpetr.snowride.lexer.Cell;
import javafx.scene.control.TextField;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeCompletionBinding {
    private final IceCell iceCell;
    private final AutoCompletionTextFieldBinding<? extends IAutocompleteOption> binding;
    private SnowTableKind snowTableKind;

    public CodeCompletionBinding(TextField textField, IceCell iceCell, SnowTableKind snowTableKind) {
        this.snowTableKind = snowTableKind;
        binding = new AutoCompletionTextFieldBinding<IAutocompleteOption>(textField, this::getSuggestions) {
            @Override
            protected void completeUserInput(IAutocompleteOption completion) {
                super.completeUserInput(completion);
                iceCell.commit();
            }
        };
        this.iceCell = iceCell;
        binding.setMinWidth(500);
        binding.setDelay(0);
        binding.setVisibleRowCount(15);
    }

    private Collection<? extends IAutocompleteOption> getSuggestions(AutoCompletionBinding.ISuggestionRequest request) {
        String text = Extensions.toInvariant(request.getUserText());
        Cell cell = iceCell.getItem();
        Stream<? extends IAutocompleteOption> allOptions = cell.getCompletionOptions(snowTableKind).filter(option ->
                Extensions.toInvariant(option.getAutocompleteText()).contains(text)
        );
        List<IAutocompleteOption> collectedOptions = allOptions.collect(Collectors.toList());
        if ("".equals(text) && collectedOptions.size() > 0) {
            collectedOptions.add(0, new DummyAutocompleteOption());
        }
        return collectedOptions;
    }

    public void trigger() {
        this.binding.setUserInput("");
    }
}

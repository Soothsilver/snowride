package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.AutoCompletionBinding;
import cz.hudecekpetr.snowride.fx.AutoCompletionTextFieldBinding;
import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.grid.IceCell;
import cz.hudecekpetr.snowride.lexer.Cell;
import javafx.scene.control.TextField;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeCompletionBinding {
    private final TextField textField;
    private final IceCell iceCell;
    private final AutoCompletionTextFieldBinding<? extends IAutocompleteOption> binding;

    public CodeCompletionBinding(TextField textField, IceCell iceCell) {
        binding = new AutoCompletionTextFieldBinding<IAutocompleteOption>(textField, this::getSuggestions) {
            @Override
            protected void completeUserInput(IAutocompleteOption completion) {
                super.completeUserInput(completion);
                iceCell.commit();
            }
        };
        this.textField = textField;
        this.iceCell = iceCell;
        binding.setMinWidth(500);
        binding.setDelay(0);
        binding.setVisibleRowCount(15);
    }

    private Collection<? extends IAutocompleteOption> getSuggestions(AutoCompletionBinding.ISuggestionRequest request) {
        String text = request.getUserText().toLowerCase();
        Cell cell = iceCell.getItem();
        Stream<IAutocompleteOption> allOptions = cell.getCompletionOptions().filter(option -> {
            return option.getAutocompleteText().toLowerCase().contains(text);
        });
        return allOptions.collect(Collectors.toList());
    }

    public void trigger() {
        this.binding.setUserInput("");
    }
}

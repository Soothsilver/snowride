package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.autocompletion.AutoCompletionBinding;
import cz.hudecekpetr.snowride.fx.autocompletion.AutoCompletionTextFieldBinding;
import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.autocompletion.SimpleAutocompleteOption;
import cz.hudecekpetr.snowride.ui.grid.IceCell;
import cz.hudecekpetr.snowride.ui.grid.SnowTableKind;
import cz.hudecekpetr.snowride.tree.Cell;
import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.ui.Images;
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
        boolean exactMatchFound = false;
        for (IAutocompleteOption collectedOption : collectedOptions) {
            if (collectedOption.getAutocompleteText().equalsIgnoreCase(request.getUserText())) {
                exactMatchFound = true;
                break;
            }
        }
        if (!exactMatchFound && collectedOptions.size() != 0) {

            if (Settings.getInstance().cbShowNonexistentOptionFirst) {
                collectedOptions.add(0, new SimpleAutocompleteOption(request.getUserText(), Images.help));
            } else {
                collectedOptions.add(new SimpleAutocompleteOption(request.getUserText(), Images.help));
            }
        }
        if ("".equals(text) && collectedOptions.size() > 0) {
            collectedOptions.add(0, new DummyAutocompleteOption());
        }

        return collectedOptions;
    }

    public void trigger() {
        this.binding.setUserInput("");
    }
}

package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.AutoCompletionBinding;
import cz.hudecekpetr.snowride.fx.AutoCompletionTextFieldBinding;
import cz.hudecekpetr.snowride.fx.IAutocompleteOption;
import cz.hudecekpetr.snowride.fx.grid.IceCell;
import cz.hudecekpetr.snowride.semantics.codecompletion.ExternalLibrary;
import cz.hudecekpetr.snowride.semantics.codecompletion.TestCaseSettingOption;
import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.Scenario;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeCompletionBinding {
    private final TextField textField;
    private final IceCell iceCell;

    public CodeCompletionBinding(TextField textField, IceCell iceCell) {
        AutoCompletionTextFieldBinding<? extends IAutocompleteOption> binding = new AutoCompletionTextFieldBinding<IAutocompleteOption>(textField, this::getSuggestions) {
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
        Stream<IAutocompleteOption> allOptions = Stream.concat(TestCaseSettingOption.allOptions.stream(),
                ((FileSuite)iceCell.getItem().partOfLine.belongsToScenario.parent).getKeywordsPermissibleInSuite()).filter(option -> {
            return option.getAutocompleteText().toLowerCase().contains(text);
        });
        return allOptions.collect(Collectors.toList());
    }
}

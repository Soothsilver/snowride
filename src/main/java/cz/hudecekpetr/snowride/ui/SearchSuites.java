package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.AutoCompletionBinding;
import cz.hudecekpetr.snowride.fx.AutoCompletionTextFieldBinding;
import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SearchSuites {
    private MainForm mainForm;

    public SearchSuites(MainForm mainForm) {
        this.mainForm = mainForm;
    }

    private Collection<HighElement> callback(AutoCompletionBinding.ISuggestionRequest request) {
        String requestText = Extensions.toInvariant(request.getUserText());
        HighElement root = mainForm.getProjectTree().getRoot().getValue();
        List<HighElement> validElements = new ArrayList<>();
        root.selfAndDescendantHighElements().forEachOrdered((highElement -> {
            if (Extensions.toInvariant(highElement.getAutocompleteText()).contains(requestText)) {
                validElements.add(highElement);
            }
        }));
        return validElements;
    }

    public void bind(TextField tbSearchTests) {
        AutoCompletionTextFieldBinding<HighElement> binding = new AutoCompletionTextFieldBinding<HighElement>(tbSearchTests, this::callback) {
            @Override
            protected void completeUserInput(HighElement completion) {
                super.completeUserInput(completion);
                if (completion != null) {
                    mainForm.selectProgrammaticallyAndRememberInHistory(completion);
                    mainForm.getProjectTree().requestFocus();
                }
            }
        };
        binding.setMinWidth(500);
        binding.setDelay(0);
        binding.setVisibleRowCount(25);
    }
}

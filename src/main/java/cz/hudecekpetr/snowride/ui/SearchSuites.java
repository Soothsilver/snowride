package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.fx.autocompletion.AutoCompletionBinding;
import cz.hudecekpetr.snowride.fx.autocompletion.AutoCompletionTextFieldBinding;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SearchSuites {
    private MainForm mainForm;
    private AutoCompletionTextFieldBinding<HighElement> binding;

    public SearchSuites(MainForm mainForm) {
        this.mainForm = mainForm;
    }

    private Collection<HighElement> callback(AutoCompletionBinding.ISuggestionRequest request) {
        String requestText = Extensions.toInvariant(request.getUserText());
        HighElement root = mainForm.getProjectTree().getRoot().getValue();
        List<HighElement> validElements = new ArrayList<>();
        root.childrenRecursively.forEach((highElement -> {
            if (highElement.getInvariantName().contains(requestText) && !highElement.getInvariantName().isEmpty()) {
                validElements.add(highElement);
            }
        }));
        if (!root.getInvariantName().isEmpty() && root.getInvariantName().contains(requestText)) {
            validElements.add(root);
        }
        validElements.sort(new Comparator<HighElement>() {
            @Override
            public int compare(HighElement o1, HighElement o2) {
                return o1.getInvariantName().compareTo(o2.getInvariantName());
            }
        });
        return validElements;
    }

    public void bind(TextField tbSearchTests) {
        binding = new AutoCompletionTextFieldBinding<HighElement>(tbSearchTests, this::callback) {
            @Override
            protected void completeUserInput(HighElement completion) {
                getCompletionTarget().setText(""); // clear the search box so the user can search again
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

    public void trigger() {
        binding.setUserInput("");
    }
}

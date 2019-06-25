package cz.hudecekpetr.snowride.fx.autocompletion;

import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;
import org.controlsfx.control.textfield.AutoCompletionBinding;

public class AutoCompletePopupSkin<T extends IAutocompleteOption> implements Skin<AutoCompletePopup<T>> {
    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;
    private int suppressUpTo = 0;

    public AutoCompletePopupSkin(AutoCompletePopup<T> control) {
        this.control = control;
        this.suggestionList = new ListView<>(control.getSuggestions());
        this.suggestionList.getStyleClass().add("auto-complete-popup");
        this.suggestionList.getStylesheets().add(AutoCompletionBinding.class.getResource("autocompletion.css").toExternalForm());
        this.suggestionList.prefHeightProperty().bind(Bindings.min(control.visibleRowCountProperty(), Bindings.size(this.suggestionList.getItems())).multiply(24).add(18));
        this.suggestionList.setCellFactory(param -> new AutoCompleteCell<T>());
        this.suggestionList.prefWidthProperty().bind(control.prefWidthProperty());
        this.suggestionList.maxWidthProperty().bind(control.maxWidthProperty());
        this.suggestionList.minWidthProperty().bind(control.minWidthProperty());
        this.suggestionList.prefHeightProperty().addListener(this::ln);
        this.control.setOnShown(event -> {
            System.out.println("shown: " + suggestionList.prefHeightProperty().getValue());
            control.heightBecame(suggestionList.prefHeightProperty().getValue());
            if (suggestionList.getFocusModel().getFocusedItem() != null) {
                showOrHideDocumentation(suggestionList.getFocusModel().getFocusedItem());
            }
        });
        this.suggestionList.getFocusModel().focusedItemProperty().addListener((observable, oldValue, newValue) -> showOrHideDocumentation(newValue));
        this.registerEventListener();
    }

    private void ln(ObservableValue<? extends Number> observableValue, Number number, Number number1) {
        control.heightBecame(number1);
    }

    private void showOrHideDocumentation(T newValue) {

        if (newValue == null) {
            suppressUpTo++;
            int[] suppressing = new int[] { suppressUpTo };
            Platform.runLater(() -> {
                //noinspection ConstantConditions - this is a bug in the inspection: this is not always true
                if (suppressUpTo == suppressing[0]) {
                    MainForm.documentationPopup.hide();
                }
            });
        } else {
            suppressUpTo++;
            if (newValue.hasQuickDocumentation()) {
                Window parent = suggestionList.getScene().getWindow();
                MainForm.documentationPopup.setData(newValue);
                if (MainForm.documentationPopup.getOwnerWindow() != parent) {
                    MainForm.documentationPopup.hide();
                }
                MainForm.documentationPopup.showRightIfPossible(parent,
                        parent.getX() + suggestionList.localToScene(0.0D, 0.0D).getX() +
                                suggestionList.getScene().getX() , suggestionList.getWidth() + 3,
                        parent.getY() + suggestionList.localToScene(0.0D, 0.0D).getY() +
                                suggestionList.getScene().getY() + suggestionList.getSelectionModel().getSelectedIndex() * 24);
            }
        }
    }

    private void registerEventListener() {
        this.suggestionList.setOnMouseClicked((me) -> {
            if (me.getButton() == MouseButton.PRIMARY) {
                this.onSuggestionChosen(this.suggestionList.getSelectionModel().getSelectedItem());
            }

        });
        this.suggestionList.setOnKeyPressed((ke) -> {
            switch(ke.getCode()) {
                case TAB:
                case ENTER:
                    this.onSuggestionChosen(this.suggestionList.getSelectionModel().getSelectedItem());
                    break;
                case ESCAPE:
                    if (this.control.isHideOnEscape()) {
                        this.control.hide();
                    }
            }

        });
    }

    private void onSuggestionChosen(T suggestion) {
        if (suggestion != null) {
            Event.fireEvent(this.control, new AutoCompletePopup.SuggestionEvent<>(suggestion));
        }

    }

    public Node getNode() {
        return this.suggestionList;
    }

    public AutoCompletePopup<T> getSkinnable() {
        return this.control;
    }

    public void dispose() {
    }
}

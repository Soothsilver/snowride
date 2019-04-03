package cz.hudecekpetr.snowride.fx;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import com.sun.javafx.PlatformUtil;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;

public class AutoCompletePopupSkin<T extends IAutocompleteOption> implements Skin<AutoCompletePopup<T>> {
    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;
    final int LIST_CELL_HEIGHT = 24;
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
        this.control.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (suggestionList.getFocusModel().getFocusedItem() != null) {
                    showOrHideDocumentation(suggestionList.getFocusModel().getFocusedItem());
                }
            }
        });
        this.suggestionList.getFocusModel().focusedItemProperty().addListener(new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                showOrHideDocumentation(newValue);
            }
        });
        this.registerEventListener();
    }

    private void showOrHideDocumentation(T newValue) {
        if (newValue == null) {
            suppressUpTo++;
            int[] suppressing = new int[] { suppressUpTo };
            Platform.runLater(() -> {
                if (suppressUpTo == suppressing[0]) {
                    MainForm.documentationPopup.hide();
                }
            });
        } else {
            suppressUpTo++;
            Window parent = suggestionList.getScene().getWindow();
            MainForm.documentationPopup.setData(newValue);
            MainForm.documentationPopup.show(parent,
                    parent.getX() + suggestionList.localToScene(0.0D, 0.0D).getX() +
                            suggestionList.getScene().getX() + suggestionList.getWidth(),
                    parent.getY() + suggestionList.localToScene(0.0D, 0.0D).getY() +
                            suggestionList.getScene().getY() + suggestionList.getSelectionModel().getSelectedIndex() * 24);

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

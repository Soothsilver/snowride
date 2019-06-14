package cz.hudecekpetr.snowride.fx.autocompletion;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

public class AutoCompleteCell<T extends IAutocompleteOption> extends ListCell<T> {
    AutoCompleteCell() {
    }

    @Override
    protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(new ImageView(item.getAutocompleteIcon()));
                setText(item.getAutocompleteText());
            }
    }
}

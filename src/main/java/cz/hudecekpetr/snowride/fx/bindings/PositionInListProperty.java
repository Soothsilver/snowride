package cz.hudecekpetr.snowride.fx.bindings;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * An integer property that's bound to the index of an item in an observable list. For example, if your list
 * is ["car", "train", "ship"], then new PositionInListProperty("train", list) will be 1. The binding is unidirectional.
 * Changing this property doesn't affect the list.
 *
 * @param <T> Type of elements in the list.
 */
public class PositionInListProperty<T> extends SimpleIntegerProperty implements ListChangeListener<T> {
    private final T item;
    private final ObservableList<T> list;

    /**
     * Creates the property and binds it to an observable list.
     * @param item The position of this item in the list is tracked by this property.
     * @param list Whenever this list changes, the value of this property is updated.
     */
    public PositionInListProperty(T item, ObservableList<T> list) {
        this.item = item;
        this.list = list;
        this.setValue(list.indexOf(item));
        list.addListener(this);
    }

    @Override
    public void onChanged(Change<? extends T> c) {
        this.setValue(list.indexOf(item));
    }
}

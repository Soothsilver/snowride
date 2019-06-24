package cz.hudecekpetr.snowride.fx;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An observable list that contains all elements of all lists that it watches. Not thread-safe. Makes no guarantees
 * about the order of elements, despite being a list. The most recently added elements, to any list, will be at the
 * end of the multiset.
 */
public class ObservableMultiset<T> extends ObservableListWrapper<T> {
    private Map<ReferenceComparisonObservableList, Listener> listeners = new HashMap<>();

    public ObservableMultiset() {
        super(new ArrayList<>());
    }

    public void appendList(ObservableList<T> child) {
        this.addAll(child);
        Listener listener = new Listener();
        child.addListener(listener);
        listeners.put(new ReferenceComparisonObservableList(child), listener);
    }

    public void removeList(ObservableList<T> child) {
        this.removeAll(child);
        Listener oldListener = listeners.remove(new ReferenceComparisonObservableList(child));
        if (oldListener != null) {
            child.removeListener(oldListener);
        }
    }

    /**
     * Whenever something changes in a watched list, this class will apply those changes
     * to this group list as well.
     */
    private class Listener implements ListChangeListener<T> {
        @Override
        public void onChanged(Change<? extends T> c) {
            while (c.next()) {
                ObservableMultiset.this.addAll(c.getAddedSubList());
                for (T t : c.getRemoved()) {
                    ObservableMultiset.this.remove(t);
                }
            }
        }
    }

    /**
     * When we remove a list from this multiset, we need to remove our listener from that list. To do that, we need
     * to recognize the list and we want to use reference comparison as opposed to a list's equals() method.
     */
    private class ReferenceComparisonObservableList {
        private ObservableList<T> list;

        public ReferenceComparisonObservableList(ObservableList<T> list) {
            this.list = list;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(list);
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass() == ReferenceComparisonObservableList.class && ((ObservableMultiset.ReferenceComparisonObservableList) obj).list == this.list;
        }
    }
}

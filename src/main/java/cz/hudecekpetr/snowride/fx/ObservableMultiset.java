package cz.hudecekpetr.snowride.fx;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservableMultiset<T> extends ObservableListWrapper<T> {
    private List<ReferenceComparisonObservableList> children = new ArrayList<>();
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
        children.removeIf(rcol -> rcol.list == child);
        listeners.remove(new ReferenceComparisonObservableList(child));
    }

    private class Listener implements ListChangeListener<T> {
        @Override
        public void onChanged(Change<? extends T> c) {
            while (c.next()) {
                ObservableMultiset.this.addAll(c.getAddedSubList());
                for (T t : c.getRemoved()) {
                    remove(t);
                }
            }
        }
    }
    private class ReferenceComparisonObservableList {
        private ObservableList<T> list;

        public ReferenceComparisonObservableList(ObservableList<T> list) {
            this.list = list;
        }

        @Override
        public int hashCode() {
            return list.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return ((ObservableMultiset<T>.ReferenceComparisonObservableList)obj).list == this.list;
        }
    }
}

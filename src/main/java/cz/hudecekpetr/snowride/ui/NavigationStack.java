package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;

public class NavigationStack {

    private List<HighElement> navigationStack = new ArrayList<>();
    private int displayingWhat = -1;

    public void standardEnter(HighElement enterWhat) {
        // Remove elements until you remove everything over our position
        while (displayingWhat + 1 < navigationStack.size()) {
            navigationStack.remove(navigationStack.size() - 1);
        }
        navigationStack.add(enterWhat);
        displayingWhat++;
        updatePossibilities();
    }

    private void updatePossibilities() {
        canNavigateBack.set(displayingWhat > 0);
        canNavigateForwards.set(displayingWhat + 1 < navigationStack.size());
    }

    public HighElement navigateForwards() {
        displayingWhat++;
        updatePossibilities();
        return navigationStack.get(displayingWhat);
    }
    public HighElement navigateBackwards() {
        displayingWhat--;
        updatePossibilities();
        return navigationStack.get(displayingWhat);
    }
    public BooleanProperty canNavigateBack = new SimpleBooleanProperty();
    public BooleanProperty canNavigateForwards = new SimpleBooleanProperty();
}

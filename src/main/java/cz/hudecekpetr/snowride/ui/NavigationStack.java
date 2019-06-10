package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.tree.HighElement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NavigationStack {

    private List<HighElement> navigationStack = new ArrayList<>();
    private int displayingWhat = -1;

    public void standardEnter(HighElement enterWhat) {
        // Remove elements until you remove everything over our position
        while (displayingWhat + 1 < navigationStack.size()) {
            // debugging: System.out.println("Destroying " + navigationStack.get(navigationStack.size() -1).getShortName() + " (stack: " + getTheStack() + ")");
            navigationStack.remove(navigationStack.size() - 1);
        }
        navigationStack.add(enterWhat);
        // debugging: System.out.println("Entering " + enterWhat.getShortName() + " (stack: " + getTheStack() + ")");
        displayingWhat++;
        updatePossibilities();
    }

    private String getTheStack() {
        return String.join(" > ", navigationStack.stream().map(HighElement::getShortName).collect(Collectors.toList()));
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

    public void clear() {
        navigationStack.clear();
        displayingWhat = -1;
        updatePossibilities();
    }

    public void remove(HighElement he) {
        if (navigationStack.removeIf(hh -> hh.equals(he))) {
            // Snowride would be confused if we removed elements from its navigation stack.
            clear();
        }

    }
}

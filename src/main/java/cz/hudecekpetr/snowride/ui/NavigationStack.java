package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.tree.highelements.Suite;
import org.robotframework.jaxb.OutputElement;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NavigationStack {

    private List<HighElement> navigationStack = new ArrayList<>();
    private int displayingWhat = -1;

    // output.xml related fields
    public OutputElement currentOutputElement;
    private LinkedList<Pair<HighElement, OutputElement>> outputElementsStack = new LinkedList<>();
    private int currentOutputElementStackIndex = -1;

    public void standardEnter(HighElement enterWhat) {
        addOutputElementToStack(enterWhat);

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
        currentOutputElementStackIndex++;
        displayingWhat++;
        updatePossibilities();
        return navigationStack.get(displayingWhat);
    }

    public HighElement navigateBackwards() {
        currentOutputElementStackIndex--;
        displayingWhat--;
        updatePossibilities();
        return navigationStack.get(displayingWhat);
    }

    public BooleanProperty canNavigateBack = new SimpleBooleanProperty();
    public BooleanProperty canNavigateForwards = new SimpleBooleanProperty();

    public void clear() {
        clearOutputElementStack();
        navigationStack.clear();
        displayingWhat = -1;
        updatePossibilities();
    }

    public OutputElement currentOutputElement() {
        if (currentOutputElementStackIndex < 0) {
            return null;
        }
        return outputElementsStack.get(currentOutputElementStackIndex).getValue();
    }

    private void addOutputElementToStack(HighElement enterWhat) {
        // clear outputElements stack
        if (enterWhat.outputElement != null) {
            clearOutputElementStack();
        }
        // remove outputElements until everything over current position is removed
        while (!outputElementsStack.isEmpty() && currentOutputElementStackIndex + 1 < outputElementsStack.size()) {
            outputElementsStack.removeLast();
        }
        // add outputElement to stack
        if (currentOutputElement != null) {
            outputElementsStack.add(new Pair(enterWhat, currentOutputElement));
            currentOutputElementStackIndex++;
        }
    }

    public void clearOutputElementStack() {
        outputElementsStack.clear();
        currentOutputElementStackIndex = -1;
    }

    public void remove(HighElement he) {
        if (navigationStack.removeIf(hh -> hh.equals(he))) {
            // Snowride would be confused if we removed elements from its navigation stack.
            clear();
        }

    }

    public void updateElement(HighElement currentElement, HighElement newElement) {
        for (int i = 0; i < navigationStack.size(); i++) {
            if (navigationStack.get(i).equals(currentElement)) {
                navigationStack.remove(currentElement);
                navigationStack.add(i, newElement);
            }
        }
    }

    public void removeElements(Suite suite) {
        navigationStack.removeIf(currentElement -> {
            boolean noneMatch = suite.children.stream().noneMatch(newElement -> currentElement.getInvariantName().equals(newElement.getInvariantName()));
            if (noneMatch) {
                currentOutputElementStackIndex--;
                displayingWhat--;
            }
            return noneMatch;
        });
    }
}

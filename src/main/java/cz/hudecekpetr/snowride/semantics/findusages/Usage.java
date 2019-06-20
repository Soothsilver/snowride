package cz.hudecekpetr.snowride.semantics.findusages;

import cz.hudecekpetr.snowride.tree.highelements.HighElement;

public class Usage {
    private final String text;
    private final HighElement element;

    public String getText() {
        return text;
    }

    public HighElement getElement() {
        return element;
    }

    public Usage(String text, HighElement element) {

        this.text = text;
        this.element = element;
    }
}

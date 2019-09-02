package cz.hudecekpetr.snowride.semantics.codecompletion;

import cz.hudecekpetr.snowride.fx.autocompletion.IAutocompleteOption;
import cz.hudecekpetr.snowride.ui.Images;
import javafx.scene.image.Image;

public class GherkinKeywordOption implements IAutocompleteOption {

    public String getKeyword() {
        return keyword;
    }

    private String keyword;

    public GherkinKeywordOption(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String getAutocompleteText() {
        return keyword + " ";
    }

    @Override
    public boolean thenRetriggerCompletion() {
        return true;
    }

    @Override
    public Image getAutocompleteIcon() {
        return Images.gherkin;
    }

    @Override
    public String getFullDocumentation() {
        return "It is also possible to write test cases as requirements that also non-technical project stakeholders must understand. These executable requirements are a corner stone of a process commonly called Acceptance Test Driven Development (ATDD) or Specification by Example." +
                "\n\n" +
                "One way to write these requirements/tests is Given-When-Then style popularized by Behavior Driven Development (BDD). " +
                "When writing test cases in this style, the initial state is usually expressed with a keyword starting with word Given, " +
                "the actions are described with keyword starting with When and the expectations with a keyword starting with Then. " +
                "Keyword starting with And or But may be used if a step has more than one action.\n" +
                "\n" +
                "Prefixes Given, When, Then, And and But are dropped when matching keywords are searched, " +
                "if no match with the full name is found. This works for both user keywords and library keywords. " +
                "For example, Given login page is open in the above example can be implemented as user keyword either with " +
                "or without the word Given. Ignoring prefixes also allows using the same keyword with different prefixes. " +
                "For example _Welcome page should be open_ could also used as _And welcome page should be open._\n" +
                "\n";
    }

    @Override
    public String getItalicsSubheading() {
        return "BDD-style keyword";
    }
}

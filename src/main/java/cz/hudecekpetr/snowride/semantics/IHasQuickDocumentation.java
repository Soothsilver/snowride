package cz.hudecekpetr.snowride.semantics;

import javafx.scene.image.Image;

/**
 * This object can, in some scenarios, causes a documentation popup to appear. High elements, cells and external
 * keywords have quick documentation.
 */
public interface IHasQuickDocumentation {
    /**
     * Gets the icon to appear in both the autocompletion popup and in the documentation popup.
     */
    Image getAutocompleteIcon();

    /**
     * Gets the caption of the documentation popup only.
     */
    String getQuickDocumentationCaption();

    /**
     * Gets the text of documentation, in Robot Framework's Markdown-like format. See
     * http://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html#documentation-formatting
     */
    String getFullDocumentation();

    /**
     * Gets the subcaption of the documentation popup.
     */
    String getItalicsSubheading();

}

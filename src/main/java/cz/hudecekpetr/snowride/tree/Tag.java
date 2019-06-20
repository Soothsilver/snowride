package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.fx.DocumentationTextArea;
import cz.hudecekpetr.snowride.tree.highelements.HighElement;
import cz.hudecekpetr.snowride.tree.highelements.Suite;

import java.util.Objects;

public class Tag {
    private String originalName;
    private String tag;
    private TagKind kind;
    private HighElement origin;


    public Tag(String name, TagKind kind, HighElement origin) {
        this.originalName = name;
        this.tag = name.toLowerCase();
        this.kind = kind;
        this.origin = origin;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag1 = (Tag) o;
        return tag.equals(tag1.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    public String prettyPrint() {
        switch (kind) {
            case STANDARD:
                return originalName;
            case FORCED:
                return DocumentationTextArea.ITALICS_CHARACTER + originalName + DocumentationTextArea.ITALICS_CHARACTER;
            case DEFAULT:
                return DocumentationTextArea.ITALICS_CHARACTER + originalName + " (default)" + DocumentationTextArea.ITALICS_CHARACTER;
            default:
                return tag;
        }
    }

    public String prettyPrintFromSuite(Suite suite) {
        if (origin == suite) {
            return tag;
        } else {
            return DocumentationTextArea.ITALICS_CHARACTER + originalName + DocumentationTextArea.ITALICS_CHARACTER;
        }
    }
}

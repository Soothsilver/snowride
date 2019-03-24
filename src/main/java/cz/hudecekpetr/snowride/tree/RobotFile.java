package cz.hudecekpetr.snowride.tree;

import java.util.ArrayList;
import java.util.List;

public class RobotFile {
    public List<RobotSection> sections = new ArrayList<>();
    public List<Exception> errors = new ArrayList<>();

    public List<HighElement> getHighElements() {
        return new ArrayList<>();
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for(RobotSection robotSection : sections) {
            robotSection.serializeInto(sb);
        }
        if (errors.size() > 0) {
            throw new RuntimeException("There were parse errors. Editing or saving is not possible.");
        }
        return sb.toString();
    }
}

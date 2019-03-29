package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;

import java.util.ArrayList;
import java.util.List;

public class RobotFile {
    public List<RobotSection> sections = new ArrayList<>();
    public List<Exception> errors = new ArrayList<>();

    public List<HighElement> getHighElements() {
        List<HighElement> he = new ArrayList<>();
        for(RobotSection section : sections) {
            he.addAll(section.getHighElements());
        }
        return he;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for(RobotSection robotSection : sections) {
            robotSection.serializeInto(sb);
        }
        if (errors.size() > 0) {
            throw new RuntimeException("There were parse errors. Editing or saving is not possible.");
        }
        String str = Extensions.removeFinalNewlineIfAny(sb.toString());
        str = Extensions.normalizeLineEndings(str);
        return str;
    }
}

package cz.hudecekpetr.snowride.semantics;

import cz.hudecekpetr.snowride.semantics.codecompletion.GherkinKeywordOption;

import java.util.ArrayList;
import java.util.List;

public class GherkinKeywords {
    public static List<GherkinKeywordOption> all = new ArrayList<>();

    static {
        all.add(new GherkinKeywordOption("Given"));
        all.add(new GherkinKeywordOption("When"));
        all.add(new GherkinKeywordOption("Then"));
        all.add(new GherkinKeywordOption("And"));
        all.add(new GherkinKeywordOption("But"));
    }

    public static String getPrefixWithSpaceIfAny(String keyword) {
        for (GherkinKeywordOption gko : all) {
            if (keyword.toLowerCase().startsWith(gko.getKeyword().toLowerCase())) {
                return gko.getKeyword() + " ";
            }
        }
        return null;
    }
}

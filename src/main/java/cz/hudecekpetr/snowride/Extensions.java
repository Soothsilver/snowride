package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.settings.Settings;
import cz.hudecekpetr.snowride.tree.LogicalLine;
import cz.hudecekpetr.snowride.undo.RemoveRowsOperation;
import cz.hudecekpetr.snowride.undo.UndoStack;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Extensions {
    private static ConcurrentHashMap<String, String> invariantNames = new ConcurrentHashMap<>();

    public static String toStringWithTrace(Exception exc) {
        return ExceptionUtils.getMessage(exc) + "\n" + ExceptionUtils.getStackTrace(exc);
    }

    public static <T, NUMBER extends Comparable<NUMBER>> NUMBER max(List<T> lines, Function<T, NUMBER> toComparable) {
        NUMBER currentMax = null;
        if (lines.size() == 0) {
            throw new IllegalArgumentException("There must be at least one item.");
        }
        for (T t : lines) {
            NUMBER tValue = toComparable.apply(t);
            if (currentMax == null || currentMax.compareTo(tValue) < 0) {
                currentMax = tValue;
            }
        }
        return currentMax;
    }

    public static String millisecondsToHumanTime(long period) {
        return String.format("%02d:%02d.%01d",
                TimeUnit.MILLISECONDS.toMinutes(period),
                TimeUnit.MILLISECONDS.toSeconds(period) % 60,
                (TimeUnit.MILLISECONDS.toMillis(period) % 1000) / 100);
    }

    public static String removeFinalNewlineIfAny(String text) {
        if (text.endsWith("\r\n")) {
            text = text.substring(0, text.length() - 2);
        } else if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public static File changeAncestorTo(File changeAncestorOfThisFile, File oldFile, File newFile) {
        String pathInQuestion = changeAncestorOfThisFile.getPath();
        String oldName = oldFile.getPath();
        String newName = newFile.getPath();
        if (pathInQuestion.startsWith(oldName)) {
            return new File(newName + pathInQuestion.substring(oldName.length()));
        } else {
            throw new RuntimeException("The file '" + newFile + "' doesn't contain '" + oldFile + "' as a prefix.");
        }
    }

    public static String toInvariant(String suiteOrKeywordName) {
        return invariantNames.computeIfAbsent(suiteOrKeywordName, key -> key.replace('_', ' ').replace(" ", "").toLowerCase());
    }

    public static String toPrettyName(String newName) {
        // The file or directory name can contain a prefix to control the execution order of the suites.
        // The prefix is separated from the base name by two underscores and, when constructing the actual test suite name,
        // both the prefix and underscores are removed. For example files 01__some_tests.robot and 02__more_tests.robot
        // create test suites Some Tests and More Tests, respectively, and the former is executed before the latter.
        int underscores = newName.indexOf("__");
        if (underscores != -1) {
            return newName.substring(underscores + 2).replace('_', ' ');
        }
        return newName.replace('_', ' ');
    }

    public static String englishCount(int number, String singular, String plural) {
        if (number == 1) {
            return "1 " + singular;
        } else {
            return number + " " + plural;
        }
    }

    public static <T> boolean containsAny(Set<T> haystack, List<T> needles) {
        for (T needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public static void optimizeLines(ObservableList<LogicalLine> lines, UndoStack undoStack) {
        Map<Integer, LogicalLine> indexes = new TreeMap<>();
        int lineIndex = lines.size() - 1;
        boolean permitOne = true;
        while (lineIndex >= 0) {
            LogicalLine line = lines.get(lineIndex);
            if (line.isFullyVirtual()) {
                if (!permitOne) {
                    indexes.put(lineIndex, lines.get(lineIndex));
                }
                permitOne = false;
            } else {
                permitOne = false;
            }
            lineIndex--;
        }

        if (undoStack != null && !indexes.isEmpty()) {
            lines.removeAll(indexes.values());
            undoStack.iJustDid(new RemoveRowsOperation(lines, indexes));
        }
    }

    public static boolean hasLegalExtension(File filename) {
        if (filename.getName().contains(".robot") || (filename.getName().contains(".txt") && Settings.getInstance().cbAlsoImportTxtFiles)) {
            return true;
        } else {
            return false;
        }
    }
}

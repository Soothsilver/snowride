package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Extensions {
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

    public static String normalizeLineEndings(String str) {
        // Commit Unix-style:
        str = str.replace("\r\n", "\n");
        //str = str.replace("\n", "\r\n");
        return str;
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
}

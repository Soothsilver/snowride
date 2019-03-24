package cz.hudecekpetr.snowride;

import cz.hudecekpetr.snowride.lexer.LogicalLine;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
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
}

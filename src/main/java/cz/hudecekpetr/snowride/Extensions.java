package cz.hudecekpetr.snowride;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class Extensions {
    public static String toStringWithTrace(Exception exc) {
        return ExceptionUtils.getMessage(exc) + "\n" + ExceptionUtils.getStackTrace(exc);
    }
}

package cz.hudecekpetr.snowride.fx;

import cz.hudecekpetr.snowride.runner.RunTab;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Keeps action to be run on the JavaFX thread in a queue and then processes them at 10 FPS to avoid excessive
 * calls to Platform.runLater() which would slow down Snowride a lot.
 */
public class DeferredActions {
    private static ConcurrentLinkedQueue<Runnable> actions = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<String> toLog = new ConcurrentLinkedQueue<>();

    /**
     * Thread-safe. Schedules an action to be run on the JavaFX thread in the next 10-FPS-interval.
     */
    public static void runLater(Runnable what) {
        actions.add(what);
    }

    /**
     * Thread-safe. Schedules a line to be added to the lower-part-of-screen log.
     */
    public static void logLater(String what) {
        toLog.add(what);
    }

    /**
     * Runs on the JavaFX thread in response to the timer (at 10 FPS).
     */
    public static void timer(RunTab runTab) {
        Runnable action = actions.poll();
        while (action != null) {
            action.run();
            action = actions.poll();
        }

        StringBuilder sb = new StringBuilder();
        String what = toLog.poll();
        while (what != null) {
            sb.append(what);
            what = toLog.poll();
        }

        if (sb.length() > 0) {
            runTab.tbLog.appendText(sb.toString());
            // Scroll to the absolute bottom:
            runTab.tbLog.showParagraphAtBottom(Integer.MAX_VALUE);
        }
    }
}

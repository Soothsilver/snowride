package cz.hudecekpetr.snowride.ui;

import cz.hudecekpetr.snowride.runner.RunTab;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DeferredActions {
    static ConcurrentLinkedQueue<Runnable> actions = new ConcurrentLinkedQueue<>();
    static ConcurrentLinkedQueue<String> toLog = new ConcurrentLinkedQueue<>();

    public static void runLater(Runnable what) {
        actions.add(what);
    }

    public static void logLater(String what) {
        toLog.add(what);
    }

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
            runTab.tbLog.showParagraphAtBottom(Integer.MAX_VALUE);
        }
    }
}

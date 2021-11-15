package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.Extensions;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayDeque;
import java.util.Deque;

public class Run {
    public SimpleIntegerProperty stoppableProcessId = new SimpleIntegerProperty(-1);
    public SimpleBooleanProperty running = new SimpleBooleanProperty(false);
    public long lastRunBeganWhen = System.currentTimeMillis();
    public long lastKeywordBeganWhen = System.currentTimeMillis();
    public int countPassedTests = 0;
    public int countFailedTests = 0;
    public int countSkippedTests = 0;
    public SimpleStringProperty logFile = new SimpleStringProperty(null);
    public SimpleStringProperty reportFile = new SimpleStringProperty(null);
    public Deque<String> keywordStack = new ArrayDeque<>();
    public boolean forciblyKilled;

    public void clear() {
        stoppableProcessId.set(-1);
        countPassedTests = 0;
        countFailedTests = 0;
        countSkippedTests = 0;
        forciblyKilled = false;
        keywordStack.clear();
        lastKeywordBeganWhen = System.currentTimeMillis();
        lastRunBeganWhen = System.currentTimeMillis();
    }

    public String keywordStackAsString() {
        StringBuilder s = new StringBuilder();
        for(String kw : keywordStack)
        {
            if (!s.toString().equals(""))
            {
                s.insert(0, " > ");
            }
            s.insert(0, kw);
        }
        if (!s.toString().equals(""))
        {
            long period = System.currentTimeMillis() - lastKeywordBeganWhen;
            s.insert(0, "(" + Extensions.millisecondsToHumanTime(period) + ") ");
        }
        return s.toString();
    }

    public boolean isInProgress() {
        return running.getValue();
    }
}

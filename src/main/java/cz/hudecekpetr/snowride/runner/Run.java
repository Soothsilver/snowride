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
    public SimpleStringProperty logFile = new SimpleStringProperty(null);
    public SimpleStringProperty reportFile = new SimpleStringProperty(null);
    public Deque<String> keywordStack = new ArrayDeque<>();
    public boolean forciblyKilled;

    public void clear() {
        stoppableProcessId.set(-1);
        countFailedTests = 0;
        countPassedTests = 0;
        forciblyKilled = false;
        keywordStack.clear();;
        lastKeywordBeganWhen = System.currentTimeMillis();
        lastRunBeganWhen = System.currentTimeMillis();
    }

    public String keywordStackAsString() {
        String s = "";
        for(String kw : keywordStack)
        {
            if (!s.equals(""))
            {
                s = " > " + s;
            }
            s = kw + s;
        }
        if (!s.equals(""))
        {
            long period = System.currentTimeMillis() - lastKeywordBeganWhen;
            s = "(" + Extensions.millisecondsToHumanTime(period) +  ") " + s;
        }
        return s;
    }

    public boolean isInProgress() {
        return running.getValue();
    }
}

package cz.hudecekpetr.snowride.runner;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

public class Run {
    public SimpleIntegerProperty stoppableProcessId = new SimpleIntegerProperty(-1);
    public long lastRunBeganWhen = System.currentTimeMillis();
    public long lastKeywordBeganWhen = System.currentTimeMillis();
    public boolean runInProgress = false;
    public int countPassedTests = 0;
    public int countFailedTests = 0;
    public SimpleStringProperty logFile = new SimpleStringProperty(null);
    public SimpleStringProperty reportFile = new SimpleStringProperty(null);
    public Deque<String> keywordStack = new ArrayDeque<>();
    public boolean forciblyKilled;

    public void clear() {
        countFailedTests = 0;
        countPassedTests = 0;
        forciblyKilled = false;
        runInProgress = true;
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
            s = "(" + String.format("%02d:%02d:%03d",
                    TimeUnit.MILLISECONDS.toMinutes(period),
                    TimeUnit.MILLISECONDS.toSeconds(period) % 60,
                    TimeUnit.MILLISECONDS.toMillis(period) % 1000) +  ") " + s;
        }
        return s;
    }
}

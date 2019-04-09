package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.Extensions;

public class Multirunner {
    private RunTab runTab;
    private boolean multirunnerStartInProgress = false;
    private boolean multirunnerInProgress = false;
    private int numberOfSuccesses = 0;

    public Multirunner(RunTab runTab) {
        this.runTab = runTab;
    }

    public void runUntilFailure() {
        numberOfSuccesses = 0;
        continueRunningUntilFailure();
    }

    private void continueRunningUntilFailure() {
        multirunnerStartInProgress = true;
        try {
            runTab.clickRun(null);
        } catch (Exception exception) {
            multirunnerStartInProgress = false;
            multirunnerInProgress = false;
            throw exception;
        }
    }

    public void manuallyStopped() {
        refreshLabel();
        multirunnerStartInProgress = false;
        multirunnerInProgress = false;
        numberOfSuccesses = 0;
    }

    private void refreshLabel() {
        if (multirunnerInProgress || multirunnerStartInProgress) {
            runTab.lblMultirun.setText("Until failure mode (" + Extensions.englishCount(numberOfSuccesses, "success", "successes") + " so far)");
            runTab.lblMultirun.setVisible(true);
        } else {
            runTab.lblMultirun.setVisible(false);
        }
    }

    public void endedNormally() {
        numberOfSuccesses++;
        if (multirunnerInProgress) {
            refreshLabel();
            multirunnerStartInProgress = false;
            if (runTab.run.countFailedTests == 0) {
                continueRunningUntilFailure();
            }
        }
    }

    public void actuallyStarted() {
        refreshLabel();
        if (multirunnerStartInProgress) {
            multirunnerStartInProgress = false;
            multirunnerInProgress = true;
        }
    }
}

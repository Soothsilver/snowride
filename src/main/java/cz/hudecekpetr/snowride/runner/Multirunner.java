package cz.hudecekpetr.snowride.runner;

public class Multirunner {
    private RunTab runTab;
    private boolean multirunnerInProgress = false;

    public Multirunner(RunTab runTab) {
        this.runTab = runTab;
    }

    public void runUntilFailure() {
        multirunnerInProgress = true;
        try {
            runTab.clickRun(null);
        } catch (Exception exception) {
            multirunnerInProgress = false;
            throw exception;
        }
    }

    public void manuallyStopped() {
        multirunnerInProgress = false;
    }

    public void endedNormally() {
        if (multirunnerInProgress) {
            if (runTab.run.countFailedTests == 0) {
                runUntilFailure();
            } else {
                multirunnerInProgress = false;
            }
        }
    }
}

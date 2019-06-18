package cz.hudecekpetr.snowride.runner;

import cz.hudecekpetr.snowride.Extensions;

public class Multirunner {
    // https://vignette.wikia.nocookie.net/mylittleponyccg/images/8/88/MLP_CCG_Comprehensive_Rules_%28v3.7%29.pdf/revision/latest?cb=20171130021504
    // Rule 802.3.
    public static final Integer BLADES_OF_GRASS_ON_SWEET_APPLE_ACRES = 24567837;
    private RunTab runTab;
    private boolean multirunnerStartInProgress = false;
    private boolean multirunnerInProgress = false;
    private int numberOfSuccesses = 0;
    private int maximumSuccesses = 0;

    public Multirunner(RunTab runTab) {
        this.runTab = runTab;
    }

    public void runUntilFailure(Integer maximumSuccesses) {
        numberOfSuccesses = 0;
        this.maximumSuccesses = maximumSuccesses;
        continueRunningUntilFailure();
    }

    private void continueRunningUntilFailure() {
        multirunnerStartInProgress = true;
        try {
            runTab.startANewRun(false);
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
            if (maximumSuccesses == BLADES_OF_GRASS_ON_SWEET_APPLE_ACRES) {
                runTab.lblMultirun.setText("Until failure mode (" + Extensions.englishCount(numberOfSuccesses, "success", "successes") + " so far)");
            } else {
                runTab.lblMultirun.setText("Until failure mode (" + numberOfSuccesses + " / " + maximumSuccesses  + " successes so far)");
            }
            runTab.lblMultirun.setVisible(true);
        } else {
            runTab.lblMultirun.setVisible(false);
        }
    }

    public void endedNormally() {
        if (runTab.run.countFailedTests == 0) {
            numberOfSuccesses++;
        }
        if (multirunnerInProgress) {
            refreshLabel();
            multirunnerStartInProgress = false;
            if (runTab.run.countFailedTests == 0 && numberOfSuccesses < maximumSuccesses) {
                continueRunningUntilFailure();
            } else {
                multirunnerInProgress = false;
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

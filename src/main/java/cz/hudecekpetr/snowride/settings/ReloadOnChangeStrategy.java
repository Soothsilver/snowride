package cz.hudecekpetr.snowride.settings;

public enum ReloadOnChangeStrategy {
    /**
     * The default. When a change happens to a tracked file, ask the user what to do about it.
     */
    POPUP_DIALOG("Ask what to do about it (default)"),
    /**
     * When a change happens to a tracked file, ignore it.
     */
    DO_NOTHING("Ignore the change"),
    /**
     * When a change happens to a tracked file, the directory should be immediately reloaded.
     */
    RELOAD_AUTOMATICALLY("Load the change immediately");

    private String humanReadable;

    ReloadOnChangeStrategy(String humanReadable) {
        this.humanReadable = humanReadable;
    }


    @Override
    public String toString() {
        return humanReadable;
    }
}

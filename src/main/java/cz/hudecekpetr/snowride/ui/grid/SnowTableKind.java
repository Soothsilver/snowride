package cz.hudecekpetr.snowride.ui.grid;

public enum SnowTableKind {
    SCENARIO(true),
    VARIABLES(false),
    SETTINGS(false);

    private boolean isScenario;

    SnowTableKind(boolean isScenario) {
        this.isScenario = isScenario;
    }

    public boolean isScenario() {
        return isScenario;
    }
}

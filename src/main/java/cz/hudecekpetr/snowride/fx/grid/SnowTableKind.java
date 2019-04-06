package cz.hudecekpetr.snowride.fx.grid;

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

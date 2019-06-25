package cz.hudecekpetr.snowride.runner;

public enum AnsiColor {
    BLACK,
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    WHITE;

    public String toStyle() {
        switch (this) {
            case RED:
                return color("red");
            case GREEN:
                // Blue is prettier than green:
                return color("blue");
            case BLACK:
                return color("black");
            case YELLOW:
                return color("orangered");
            default:
                return color("magenta");
        }
    }

    private String color(String color) {
        return "-fx-fill: " + color + ";";
    }
}

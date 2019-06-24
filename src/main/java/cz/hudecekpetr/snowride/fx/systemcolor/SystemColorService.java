package cz.hudecekpetr.snowride.fx.systemcolor;

import cz.hudecekpetr.snowride.settings.Settings;

import java.awt.*;

public class SystemColorService {
    public static String css = "";

    public static void initialize() {
        if (Settings.getInstance().cbUseSystemColorWindow) {
            int r = SystemColor.window.getRed();
            int g = SystemColor.window.getGreen();
            int b = SystemColor.window.getBlue();
            css = ".root { -fx-control-inner-background: rgba(" + r + ", " + g + ", " + b + ", 1.0); }";
        }
    }
}

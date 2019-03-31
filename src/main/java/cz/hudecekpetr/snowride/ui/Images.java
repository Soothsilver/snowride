package cz.hudecekpetr.snowride.ui;

import javafx.scene.image.Image;

public class Images {
    public static Image fileIcon = staticImage("file16.png");
    public static Image folderIcon = staticImage("folder2_16.png");
    public static Image keywordIcon = staticImage("cog16.png");
    public static Image testIcon = staticImage("robot16.png");
    public static Image yes = staticImage("yes16.png");
    public static Image no = staticImage("no16.png");
    public static Image running = staticImage("running16.png");
    public static Image b = staticImage("b16.png");
    public static Image brackets = staticImage("brackets16.png");
    public static Image python = staticImage("python16.png");
    public static Image java = staticImage("java16.png");

    private static Image staticImage(final String filename) {
        return new Image(Images.class.getResourceAsStream("/icons/" + filename), 16, 16, false, false);
    }
}

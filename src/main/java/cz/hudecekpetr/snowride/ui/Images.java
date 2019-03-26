package cz.hudecekpetr.snowride.ui;

import javafx.scene.image.Image;

public class Images {
    public static Image fileIcon = staticImage("file16.png");
    public static Image folderIcon = staticImage("folder16.png");
    public static Image keywordIcon = staticImage("cog16.png");
    public static Image testIcon = staticImage("robot16.png");

    private static Image staticImage(final String filename) {
        return new Image(Images.class.getResourceAsStream("/icons/" + filename), 16, 16, false, false);
    }
}

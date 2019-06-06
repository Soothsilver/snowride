package cz.hudecekpetr.snowride.ui;

import javafx.scene.image.Image;

public class Images {
    public static Image fileIcon = staticImage("file16.png");
    public static Image folderIcon = staticImage("folder2_16.png");
    public static Image keywordIcon = staticImage("cog16.png");
    public static Image testIcon = staticImage("robot16.png");
    public static Image yes = staticImage("yes16.png");
    public static Image xml = staticImage("xml16.png");
    public static Image no = staticImage("no16.png");
    public static Image running = staticImage("running16.png");
    public static Image b = staticImage("b16.png");
    public static Image brackets = staticImage("brackets16.png");
    public static Image python = staticImage("python16.png");
    public static Image java = staticImage("java16.png");
    public static Image exit = staticImage("exit16.png");
    public static Image log = staticImage("log16.png");
    public static Image report = staticImage("report16.png");
    public static Image open = staticImage("open16.png");
    public static Image stop = staticImage("stop16.png");
    public static Image play = staticImage("play16.png");
    public static Image save = staticImage("save16.png");
    public static Image goLeft = staticImage("GoLeft.png");
    public static Image goRight = staticImage("GoRight.png");
    public static Image warning = staticImage("warning16.png");
    public static Image error = staticImage("error16.png");
    public static Image snowflake = new Image(Images.class.getResourceAsStream("/icons/Snowflake3.png"));

    private static Image staticImage(final String filename) {
        return new Image(Images.class.getResourceAsStream("/icons/" + filename), 16, 16, false, false);
    }
}

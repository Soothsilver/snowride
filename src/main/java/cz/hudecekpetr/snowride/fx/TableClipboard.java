package cz.hudecekpetr.snowride.fx;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class TableClipboard {
    private static Clipboard clipboard = Clipboard.getSystemClipboard();

    public static void store(String cell) {
        ClipboardContent content = new ClipboardContent();
        content.putString(cell);
        clipboard.setContent(content);
    }
}

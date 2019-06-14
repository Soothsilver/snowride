package cz.hudecekpetr.snowride.fx;

import com.sun.javafx.util.Utils;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.stage.Screen;

public class ScreenEdgeAvoidance {
    public static Point2D determineStartingPositionForAutocompletion(Rectangle2D avoidThisRectangle,Dimension2D popupSize) {
        double requestedX, requestedY;
        requestedX = avoidThisRectangle.getMinX();
        requestedY = avoidThisRectangle.getMaxY();

        final Screen currentScreen = Utils.getScreenForPoint(requestedX + 13, requestedY);
        final Rectangle2D screenBounds = currentScreen.getVisualBounds();
        if (requestedX + 13 + popupSize.getWidth() >= screenBounds.getMaxX()) {
            // Would overflow screen on the right -- move slightly to the left
            requestedX = screenBounds.getMaxX() - popupSize.getWidth();
        }
        if (requestedY + popupSize.getHeight() >= screenBounds.getMaxY()) {
            // Would overflow bottom - move above the text field
            requestedY = avoidThisRectangle.getMinY() - popupSize.getHeight();
        }
        return new Point2D(requestedX, requestedY);
    }


    public static Point2D determineStartingPositionForDocumentationPopup(double anchorLeft, double anchorY, double anchorWidth, Dimension2D popupSize) {

        final Screen currentScreen = Utils.getScreenForPoint(anchorLeft + anchorWidth, anchorY);
        final Rectangle2D screenBounds = currentScreen.getVisualBounds();
        double finalX;
        if (anchorLeft + anchorWidth + popupSize.getWidth() >= screenBounds.getMaxX()) {
            // Overflow width
            finalX = anchorLeft - popupSize.getWidth();
        } else {
            finalX = anchorLeft + anchorWidth;
        }
        if (anchorY + popupSize.getHeight() >= screenBounds.getMaxY()) {
            // Overflow height
            anchorY = screenBounds.getMaxY() - popupSize.getHeight();
        }
        return new Point2D(finalX, anchorY);
    }
}

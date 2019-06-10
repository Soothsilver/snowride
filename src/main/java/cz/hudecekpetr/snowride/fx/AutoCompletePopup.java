package cz.hudecekpetr.snowride.fx;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.util.Utils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class AutoCompletePopup<T extends IAutocompleteOption> extends PopupControl {
    private static final int TITLE_HEIGHT = 28;
    private static final double ADDED_BONUS = 28;
    private final ObservableList<T> suggestions = FXCollections.observableArrayList();
    private StringConverter<T> converter;
    private IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 10);
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    private ObjectProperty<EventHandler<AutoCompletePopup.SuggestionEvent<T>>> onSuggestion = new ObjectPropertyBase<EventHandler<AutoCompletePopup.SuggestionEvent<T>>>() {
        protected void invalidated() {
            AutoCompletePopup.this.eventHandlerManager.setEventHandler(AutoCompletePopup.SuggestionEvent.SUGGESTION, (EventHandler)this.get());
        }

        public Object getBean() {
            return AutoCompletePopup.this;
        }

        public String getName() {
            return "onSuggestion";
        }
    };
    public static final String DEFAULT_STYLE_CLASS = "auto-complete-popup";

    public AutoCompletePopup() {
        this.setAutoFix(false);
        this.setAutoHide(true);
        this.setHideOnEscape(true);
        this.getStyleClass().add("auto-complete-popup");
    }

    public ObservableList<T> getSuggestions() {
        return this.suggestions;
    }

    private Node shownByNode;
    public void show(Node node) {
        shownByNode = node;
        if (node.getScene() != null && node.getScene().getWindow() != null) {
            if (!this.isShowing()) {
                updatePopupPosition(700);
            }
        } else {
            throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");
        }
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
    }

    public StringConverter<T> getConverter() {
        return this.converter;
    }

    public final void setVisibleRowCount(int value) {
        this.visibleRowCount.set(value);
    }

    public final int getVisibleRowCount() {
        return this.visibleRowCount.get();
    }

    public final IntegerProperty visibleRowCountProperty() {
        return this.visibleRowCount;
    }

    public final ObjectProperty<EventHandler<AutoCompletePopup.SuggestionEvent<T>>> onSuggestionProperty() {
        return this.onSuggestion;
    }

    public final void setOnSuggestion(EventHandler<AutoCompletePopup.SuggestionEvent<T>> value) {
        this.onSuggestionProperty().set(value);
    }

    public final EventHandler<AutoCompletePopup.SuggestionEvent<T>> getOnSuggestion() {
        return (EventHandler)this.onSuggestionProperty().get();
    }

    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return super.buildEventDispatchChain(tail).append(this.eventHandlerManager);
    }

    protected Skin<?> createDefaultSkin() {
        AutoCompletePopupSkin skin = new AutoCompletePopupSkin(this);
        skin.getSuggestionList().prefHeightProperty().addListener(this::skinPrefHeightChanged);
        return skin;
    }

    private void skinPrefHeightChanged(ObservableValue<? extends Number> observableValue, Number number, Number newNumber) {
        updatePopupPosition((double) newNumber);
    }

    private void updatePopupPosition(double popupsRequestedheight) {
        Window parent = shownByNode.getScene().getWindow();
        double anchorX = parent.getX() + shownByNode.localToScene(0.0D, 0.0D).getX() + shownByNode.getScene().getX();
        double anchorY = parent.getY() + shownByNode.localToScene(0.0D, 0.0D).getY() + shownByNode.getScene().getY() + 28.0D;
        final Screen currentScreen = Utils.getScreenForPoint(anchorX, anchorY);
        final Rectangle2D screenBounds = currentScreen.getVisualBounds();
        /*if (anchorX + this.getPrefWidth() >= screenBounds.getMaxX()) {
            anchorX = anchorX - this.getPrefWidth();
        }*/

        this.show(parent,
                anchorX,
                anchorY);
        if (anchorY + popupsRequestedheight >= screenBounds.getMaxY() - ADDED_BONUS) {
            // Would overflow screen
            super.show(parent, anchorX, anchorY - popupsRequestedheight - ADDED_BONUS);
        } else {
            super.show(parent, anchorX, anchorY);
        }
    }

    public static class SuggestionEvent<TE> extends Event {
        public static final EventType<AutoCompletePopup.SuggestionEvent> SUGGESTION = new EventType("SNOWSUGGESTION");
        private final TE suggestion;

        public SuggestionEvent(TE suggestion) {
            super(SUGGESTION);
            this.suggestion = suggestion;
        }

        public TE getSuggestion() {
            return this.suggestion;
        }
    }
}

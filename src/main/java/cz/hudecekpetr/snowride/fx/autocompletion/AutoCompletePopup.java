package cz.hudecekpetr.snowride.fx.autocompletion;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import com.sun.javafx.event.EventHandlerManager;
import cz.hudecekpetr.snowride.fx.ScreenEdgeAvoidance;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class AutoCompletePopup<T extends IAutocompleteOption> extends PopupControl {
    public static final String DEFAULT_STYLE_CLASS = "auto-complete-popup";
    private static final int TITLE_HEIGHT = 28;
    private final ObservableList<T> suggestions = FXCollections.observableArrayList();
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    private StringConverter<T> converter;
    private IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 10);
    private ObjectProperty<EventHandler<AutoCompletePopup.SuggestionEvent<T>>> onSuggestion = new ObjectPropertyBase<EventHandler<AutoCompletePopup.SuggestionEvent<T>>>() {
        @Override
        @SuppressWarnings("RedundantCast") // not redundant, there would be a compile error
        protected void invalidated() {
            AutoCompletePopup.this.eventHandlerManager.setEventHandler(AutoCompletePopup.SuggestionEvent.SUGGESTION, (EventHandler) this.get());
        }

        @Override
        public Object getBean() {
            return AutoCompletePopup.this;
        }

        @Override
        public String getName() {
            return "onSuggestion";
        }
    };
    private TextField attachedToNode;

    public AutoCompletePopup() {
        this.setAutoFix(false);
        this.setAutoHide(true);
        this.setHideOnEscape(true);
        this.getStyleClass().add("auto-complete-popup");
    }

    public ObservableList<T> getSuggestions() {
        return this.suggestions;
    }

    public void show(TextField node) {
        attachedToNode = node;
        if (node.getScene() != null && node.getScene().getWindow() != null) {
            if (!this.isShowing()) {
                Window parent = node.getScene().getWindow();
                double anchorX = parent.getX() + node.localToScene(0.0D, 0.0D).getX() + node.getScene().getX();
                double anchorY = parent.getY() + node.localToScene(0.0D, 0.0D).getY() + node.getScene().getY() + 28.0D;
                this.show(parent,
                        anchorX,
                        anchorY);
            }
        } else {
            throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");
        }
    }


    public StringConverter<T> getConverter() {
        return this.converter;
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
    }

    public final int getVisibleRowCount() {
        return this.visibleRowCount.get();
    }

    public final void setVisibleRowCount(int value) {
        this.visibleRowCount.set(value);
    }

    public final IntegerProperty visibleRowCountProperty() {
        return this.visibleRowCount;
    }

    public final ObjectProperty<EventHandler<AutoCompletePopup.SuggestionEvent<T>>> onSuggestionProperty() {
        return this.onSuggestion;
    }

    public final EventHandler<AutoCompletePopup.SuggestionEvent<T>> getOnSuggestion() {
        return this.onSuggestionProperty().get();
    }

    public final void setOnSuggestion(EventHandler<AutoCompletePopup.SuggestionEvent<T>> value) {
        this.onSuggestionProperty().set(value);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return super.buildEventDispatchChain(tail).append(this.eventHandlerManager);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AutoCompletePopupSkin(this);
    }

    public void heightBecame(Number whatItBecame) {
        Window parent = attachedToNode.getScene().getWindow();
        Platform.runLater(() -> {
            double x = parent.getX() + attachedToNode.localToScene(0.0D, 0.0D).getX() + attachedToNode.getScene().getX() - 13;
            double y = parent.getY() + attachedToNode.localToScene(0.0D, 0.0D).getY() + attachedToNode.getScene().getY() - 2;
            Point2D adjustedPoint = ScreenEdgeAvoidance.determineStartingPositionForAutocompletion(new Rectangle2D(x, y, attachedToNode.getWidth(), attachedToNode.getHeight()),
                    new Dimension2D(getWidth(), (double) whatItBecame + 4));
            this.setX(adjustedPoint.getX());
            this.setY(adjustedPoint.getY());
        });
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

package cz.hudecekpetr.snowride.fx;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import com.sun.javafx.event.EventHandlerManager;
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
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class AutoCompletePopup<T extends IAutocompleteOption> extends PopupControl {
    private static final int TITLE_HEIGHT = 28;
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
        this.setAutoFix(true);
        this.setAutoHide(true);
        this.setHideOnEscape(true);
        this.getStyleClass().add("auto-complete-popup");
    }

    public ObservableList<T> getSuggestions() {
        return this.suggestions;
    }

    public void show(Node node) {
        if (node.getScene() != null && node.getScene().getWindow() != null) {
            if (!this.isShowing()) {
                Window parent = node.getScene().getWindow();
                this.show(parent, parent.getX() + node.localToScene(0.0D, 0.0D).getX() + node.getScene().getX(), parent.getY() + node.localToScene(0.0D, 0.0D).getY() + node.getScene().getY() + 28.0D);
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
        return new AutoCompletePopupSkin(this);
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

package cz.hudecekpetr.snowride.fx.autocompletion;

/*
 * Copyright (c) 2014, 2016 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.sun.javafx.event.EventHandlerManager;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;

/**
 * The AutoCompletionBinding is the abstract base class of all auto-completion bindings.
 * This class is the core logic for the auto-completion feature but highly customizable.
 **
 * The popup size can be modified through its {@link #setVisibleRowCount(int) }
 * for the height and all the usual methods for the width.
 *
 * @param <T> Model-Type of the suggestions
 */
@SuppressWarnings("DanglingJavadoc")
public abstract class AutoCompletionBinding<T extends IAutocompleteOption> implements EventTarget {


    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/
    private final Node completionTarget;
    private final AutoCompletePopup<T> autoCompletionPopup;
    private final Object suggestionsTaskLock = new Object();

    private AutoCompletionBinding.FetchSuggestionsTask suggestionsTask = null;
    private Callback<AutoCompletionBinding.ISuggestionRequest, Collection<? extends T>> suggestionProvider;
    private boolean ignoreInputChanges = false;
    private long delay = 250;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/


    /**
     * Creates a new AutoCompletionBinding
     *
     * @param completionTarget The target node to which auto-completion shall be added
     * @param suggestionProvider The strategy to retrieve suggestions 
     * @param converter The converter to be used to convert suggestions to strings 
     */
    protected AutoCompletionBinding(Node completionTarget,
                                    Callback<AutoCompletionBinding.ISuggestionRequest, Collection<? extends T>> suggestionProvider,
                                    StringConverter<T> converter){

        this.completionTarget = completionTarget;
        this.suggestionProvider = suggestionProvider;
        this.autoCompletionPopup = new AutoCompletePopup<>();
        this.autoCompletionPopup.setConverter(converter);

        autoCompletionPopup.setOnSuggestion(sce -> {
            try{
                setIgnoreInputChanges(true);
                completeUserInput(sce.getSuggestion());
                fireAutoCompletion(sce.getSuggestion());
                hidePopup();
            }finally{
                // Ensure that ignore is always set back to false
                setIgnoreInputChanges(false);
            }
        });
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Specifies whether the PopupWindow should be hidden when an unhandled
     * escape key is pressed while the popup has focus.
     *
     */
    public void setHideOnEscape(boolean value) {
        autoCompletionPopup.setHideOnEscape(value);
    }

    /**
     * Set the current text the user has entered
     */
    public final void setUserInput(String userText){
        if(!isIgnoreInputChanges()){
            onUserInputChanged(userText);
        }
    }

    /**
     * Sets the delay in ms between a key press and the suggestion popup being displayed.
     *
     */
    public final void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * Gets the target node for auto completion
     * @return the target node for auto completion
     */
    public Node getCompletionTarget(){
        return completionTarget;
    }

    /**
     * Disposes the binding.
     */
    public abstract void dispose();


    /**
     * Set the maximum number of rows to be visible in the popup when it is
     * showing.
     *
     */
    public final void setVisibleRowCount(int value) {
        autoCompletionPopup.setVisibleRowCount(value);
    }

    /**
     * Return the maximum number of rows to be visible in the popup when it is
     * showing.
     *
     * @return the maximum number of rows to be visible in the popup when it is
     * showing.
     */
    public final int getVisibleRowCount() {
        return autoCompletionPopup.getVisibleRowCount();
    }

    /**
     * Return an property representing the maximum number of rows to be visible
     * in the popup when it is showing.
     *
     * @return an property representing the maximum number of rows to be visible
     * in the popup when it is showing.
     */
    public final IntegerProperty visibleRowCountProperty() {
        return autoCompletionPopup.visibleRowCountProperty();
    }

    /**
     * Sets the prefWidth of the popup.
     *
     */
    public final void setPrefWidth(double value) {
        autoCompletionPopup.setPrefWidth(value);
    }

    /**
     * Return the pref width of the popup.
     *
     * @return the pref width of the popup.
     */
    public final double getPrefWidth() {
        return autoCompletionPopup.getPrefWidth();
    }

    /**
     * Return the property associated with the pref width.
     */
    public final DoubleProperty prefWidthProperty() {
        return autoCompletionPopup.prefWidthProperty();
    }

    /**
     * Sets the minWidth of the popup.
     *
     */
    public final void setMinWidth(double value) {
        autoCompletionPopup.setMinWidth(value);
    }

    /**
     * Return the min width of the popup.
     *
     * @return the min width of the popup.
     */
    public final double getMinWidth() {
        return autoCompletionPopup.getMinWidth();
    }

    /**
     * Return the property associated with the min width.
     */
    public final DoubleProperty minWidthProperty() {
        return autoCompletionPopup.minWidthProperty();
    }

    /**
     * Sets the maxWidth of the popup.
     *
     */
    public final void setMaxWidth(double value) {
        autoCompletionPopup.setMaxWidth(value);
    }

    /**
     * Return the max width of the popup.
     *
     * @return the max width of the popup.
     */
    public final double getMaxWidth() {
        return autoCompletionPopup.getMaxWidth();
    }

    /**
     * Return the property associated with the max width.
     */
    public final DoubleProperty maxWidthProperty() {
        return autoCompletionPopup.maxWidthProperty();
    }

    /***************************************************************************
     *                                                                         *
     * Protected methods                                                       *
     *                                                                         *
     **************************************************************************/

    /**
     * Complete the current user-input with the provided completion.
     * Sub-classes have to provide a concrete implementation.
     */
    protected abstract void completeUserInput(T completion);


    /**
     * Show the auto completion popup
     */
    protected void showPopup(){
        autoCompletionPopup.show(completionTarget);
        selectFirstSuggestion(autoCompletionPopup);
    }

    /**
     * Hide the auto completion targets
     */
    protected void hidePopup(){
        autoCompletionPopup.hide();
    }

    protected void fireAutoCompletion(T completion){
        Event.fireEvent(this, new AutoCompletionBinding.AutoCompletionEvent<>(completion));
    }


    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/

    /**
     * Selects the first suggestion (if any), so the user can choose it
     * by pressing enter immediately.
     */
    private void selectFirstSuggestion(AutoCompletePopup<?> autoCompletionPopup){
        Skin<?> skin = autoCompletionPopup.getSkin();
        if(skin instanceof AutoCompletePopupSkin){
            AutoCompletePopupSkin<?> au = (AutoCompletePopupSkin<?>)skin;
            ListView<?> li = (ListView<?>)au.getNode();
            if(li.getItems() != null && !li.getItems().isEmpty()){
                li.getSelectionModel().select(0);
            }
        }
    }

    /**
     * Occurs when the user text has changed and the suggestions require an update
     * @param userText
     */
    private void onUserInputChanged(final String userText){
        synchronized (suggestionsTaskLock) {
            if(suggestionsTask != null && suggestionsTask.isRunning()){
                // cancel the current running task
                suggestionsTask.cancel();
            }
            // create a new fetcher task
            suggestionsTask = new AutoCompletionBinding.FetchSuggestionsTask(userText, delay);
            Thread thread = new Thread(suggestionsTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Shall changes to the user input be ignored?
     */
    private boolean isIgnoreInputChanges(){
        return ignoreInputChanges;
    }

    /**
     * If IgnoreInputChanges is set to true, all changes to the user input are
     * ignored. This is primary used to avoid self triggering while
     * auto completing.
     */
    private void setIgnoreInputChanges(boolean state){
        ignoreInputChanges = state;
    }

    /***************************************************************************
     *                                                                         *
     * Inner classes and interfaces                                            *
     *                                                                         *
     **************************************************************************/


    /**
     * Represents a suggestion fetch request
     *
     */
    public interface ISuggestionRequest {
        /**
         * Is this request canceled?
         * @return {@code true} if the request is canceled, otherwise {@code false}
         */
        boolean isCancelled();

        /**
         * Get the user text to which suggestions shall be found
         * @return {@link String} containing the user text
         */
        String getUserText();
    }



    /**
     * This task is responsible to fetch suggestions asynchronous
     * by using the current defined suggestionProvider
     *
     */
    private class FetchSuggestionsTask extends Task<Void> implements AutoCompletionBinding.ISuggestionRequest {
        private final String userText;
        private final long delay;

        public FetchSuggestionsTask(String userText, long delay){
            this.userText = userText;
            this.delay = delay;
        }

        @Override
        protected Void call() throws Exception {
            Callback<AutoCompletionBinding.ISuggestionRequest, Collection<? extends T>> provider = suggestionProvider;
            if(provider != null){
                long startTime = System.currentTimeMillis();
                long sleepTime = startTime + delay - System.currentTimeMillis();
                if (sleepTime > 0 && !isCancelled()) {
                    Thread.sleep(sleepTime);
                }
                if(!isCancelled()){
                    final Collection<? extends T> fetchedSuggestions = provider.call(this);
                    Platform.runLater(() -> {
                        if(fetchedSuggestions != null && !fetchedSuggestions.isEmpty()){
                            autoCompletionPopup.getSuggestions().setAll(fetchedSuggestions);
                            showPopup();
                        }else{
                            // No suggestions found, so hide the popup
                            hidePopup();
                        }
                    });
                }
            }else {
                // No suggestion provider
                hidePopup();
            }
            return null;
        }

        @Override
        public String getUserText() {
            return userText;
        }
    }

    /***************************************************************************
     *                                                                         *
     * Events                                                                  *
     *                                                                         *
     **************************************************************************/


    // --- AutoCompletionEvent

    /**
     * Represents an Event which is fired after an auto completion.
     */
    @SuppressWarnings("serial")
    public static class AutoCompletionEvent<TE> extends Event {

        /**
         * The event type that should be listened to by people interested in 
         * knowing when an auto completion has been performed.
         */
        @SuppressWarnings("rawtypes")
        public static final EventType<AutoCompletionBinding.AutoCompletionEvent> AUTO_COMPLETED = new EventType<>("SNOW_AUTO_COMPLETED"); //$NON-NLS-1$

        private final TE completion;

        /**
         * Creates a new event that can subsequently be fired.
         */
        public AutoCompletionEvent(TE completion) {
            super(AUTO_COMPLETED);
            this.completion = completion;
        }
    }


    private ObjectProperty<EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>>> onAutoCompleted;

    /**
     * Set a event handler which is invoked after an auto completion.
     */
    public final void setOnAutoCompleted(EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>> value) {
        onAutoCompletedProperty().set( value);
    }

    public final EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>> getOnAutoCompleted() {
        return onAutoCompleted == null ? null : onAutoCompleted.get();
    }

    public final ObjectProperty<EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>>> onAutoCompletedProperty() {
        if (onAutoCompleted == null) {
            onAutoCompleted = new ObjectPropertyBase<EventHandler<AutoCompletionBinding.AutoCompletionEvent<T>>>() {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override protected void invalidated() {
                    eventHandlerManager.setEventHandler(
                            AutoCompletionBinding.AutoCompletionEvent.AUTO_COMPLETED,
                            (EventHandler<AutoCompletionBinding.AutoCompletionEvent>)(Object)get());
                }

                @Override
                public Object getBean() {
                    return AutoCompletionBinding.this;
                }

                @Override
                public String getName() {
                    return "onAutoCompleted"; //$NON-NLS-1$
                }
            };
        }
        return onAutoCompleted;
    }


    /***************************************************************************
     *                                                                         *
     * EventTarget Implementation                                              *
     *                                                                         *
     **************************************************************************/

    final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    /**
     * Registers an event handler to this EventTarget. The handler is called when the
     * menu item receives an {@code Event} of the specified type during the bubbling
     * phase of event delivery.
     *
     * @param <E> the specific event class of the handler
     * @param eventType the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    public <E extends Event> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    /**
     * Unregisters a previously registered event handler from this EventTarget. One
     * handler might have been registered for different event types, so the
     * caller needs to specify the particular event type from which to
     * unregister the handler.
     *
     * @param <E> the specific event class of the handler
     * @param eventType the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    public <E extends Event> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

    /** {@inheritDoc} */
    @Override public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.prepend(eventHandlerManager);
    }


}

/*
 * Copyright (c) 2014, 2015, ControlsFX
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
package cz.hudecekpetr.snowride.fx.autocompletion;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;

/**
 * Represents a binding between a text field and a auto-completion popup
 *
 * @param <T>
 */
@SuppressWarnings({"WeakerAccess", "DanglingJavadoc"})
public class AutoCompletionTextFieldBinding<T extends IAutocompleteOption> extends AutoCompletionBinding<T> {

    /***************************************************************************
     *                                                                         *
     * Static properties and methods                                           *
     *                                                                         *
     **************************************************************************/

    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<T>() {
            @Override
            public String toString(T t) {
                return t == null ? null : t.toString();
            }

            @SuppressWarnings("unchecked")
            @Override
            public T fromString(String string) {
                return (T) string;
            }
        };
    }

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/


    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new auto-completion binding between the given textField
     * and the given suggestion provider.
     */
    public AutoCompletionTextFieldBinding(final TextField textField,
                                          Callback<ISuggestionRequest, Collection<? extends T>> suggestionProvider) {

        this(textField, suggestionProvider, AutoCompletionTextFieldBinding.defaultStringConverter());
    }

    /**
     * Creates a new auto-completion binding between the given textField
     * and the given suggestion provider.
     */
    public AutoCompletionTextFieldBinding(final TextField textField,
                                          Callback<ISuggestionRequest, Collection<? extends T>> suggestionProvider,
                                          final StringConverter<T> converter) {

        super(textField, suggestionProvider, converter);

        getCompletionTarget().textProperty().addListener(textChangeListener);
        getCompletionTarget().focusedProperty().addListener(focusChangedListener);
    }


    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * {@inheritDoc}
     */
    @Override
    public TextField getCompletionTarget() {
        return super.getCompletionTarget();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        getCompletionTarget().textProperty().removeListener(textChangeListener);
        getCompletionTarget().focusedProperty().removeListener(focusChangedListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void completeUserInput(T completion) {
        String newText = completion.getAutocompleteText();
        getCompletionTarget().setText(newText);
        getCompletionTarget().positionCaret(newText.length());
    }


    /***************************************************************************
     *                                                                         *
     * Event Listeners                                                         *
     *                                                                         *
     **************************************************************************/


    private final ChangeListener<String> textChangeListener = (obs, oldText, newText) -> {
        if (getCompletionTarget().isFocused()) {
            setUserInput(newText);
        }
    };

    private final ChangeListener<Boolean> focusChangedListener = (obs, oldFocused, newFocused) -> {
        if (!newFocused)
            hidePopup();
    };
}

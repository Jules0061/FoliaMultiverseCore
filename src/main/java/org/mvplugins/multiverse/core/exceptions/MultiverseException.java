package org.mvplugins.multiverse.core.exceptions;

import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.locale.message.LocalizableMessage;
import org.mvplugins.multiverse.core.locale.message.Message;

public class MultiverseException extends Exception implements LocalizableMessage {

    private final transient @Nullable Message message;

    public MultiverseException(@Nullable String message) {
        super(message);
        this.message = message != null ? Message.of(message) : null;
    }

    public MultiverseException(@Nullable Message message) {
        super(message != null ? message.formatted() : null);
        this.message = message;
    }

    public MultiverseException(@Nullable String message, @Nullable Throwable cause) {
        this(message != null ? Message.of(message) : null, cause);
    }

    public MultiverseException(@Nullable Message message, @Nullable Throwable cause) {
        super(message != null ? message.formatted() : null, cause);
        this.message = message;
    }

    public final @Nullable Message getLocalizableMessage() {
        return message;
    }
}

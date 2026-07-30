package org.mvplugins.multiverse.core.locale.message;

import io.vavr.control.Either;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MessageReplacement {

    @Contract(value = "_ -> new", pure = true)
    public static MessageReplacement.Key replace(@NotNull String key) {
        return new MessageReplacement.Key(key);
    }

    private final @NotNull String key;
    private final @NotNull Either<String, Message> replacement;

    private MessageReplacement(@NotNull String key, @NotNull Message replacement) {
        this.key = key;
        this.replacement = Either.right(replacement);
    }

    private MessageReplacement(@NotNull String key, @Nullable Object replacement) {
        this.key = key;
        this.replacement = (replacement instanceof Message message)
                ? Either.right(message)
                : Either.left(String.valueOf(replacement));
    }

    public @NotNull String getKey() {
        return key;
    }

    public @NotNull Either<String, Message> getReplacement() {
        return replacement;
    }

    public static final class Key {

        private final @NotNull String key2;

        private Key(@NotNull String key) {
            this.key2 = key;
        }

        @Contract(value = "_ -> new", pure = true)
        public MessageReplacement with(@NotNull Message replacement) {
            return new MessageReplacement(key2, replacement);
        }

        @Contract(value = "_ -> new", pure = true)
        public MessageReplacement with(@Nullable Object replacement) {
            if (replacement instanceof LocalizableMessage localizableMessage
                    && localizableMessage.getLocalizableMessage() != null) {
                return new MessageReplacement(key2, localizableMessage.getLocalizableMessage());
            }
            if (replacement instanceof Throwable throwable) {
                return new MessageReplacement(key2, throwable.getLocalizedMessage());
            }
            return new MessageReplacement(key2, replacement);
        }
    }

    public enum Replace {
        COUNT(replace("{count}")),
        DESTINATION(replace("{destination}")),
        ERROR(replace("{error}")),
        GAMERULE(replace("{gamerule}")),
        LOCATION(replace("{location}")),
        NAME(replace("{name}")),
        NAMESPACE(replace("{namespace}")),
        PLAYER(replace("{player}")),
        REASON(replace("{reason}")),
        VALUE(replace("{value}")),
        WORLD(replace("{world}")),
        ;

        private final Key replaceKey;

        Replace(Key replaceKey) {
            this.replaceKey = replaceKey;
        }

        @Contract(value = "_ -> new", pure = true)
        public MessageReplacement with(@NotNull Message replacement) {
            return replaceKey.with(replacement);
        }

        @Contract(value = "_ -> new", pure = true)
        public MessageReplacement with(@Nullable Object replacement) {
            return replaceKey.with(replacement);
        }
    }
}

package org.mvplugins.multiverse.core.locale.message;

import java.util.Objects;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.Locales;
import co.aikar.locales.MessageKeyProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed class Message permits LocalizedMessage {

    @Contract(value = "_, _ -> new", pure = true)
    public static Message of(@NotNull String message, @NotNull MessageReplacement... replacements) {
        Objects.requireNonNull(message, "message must not be null");
        for (MessageReplacement replacement : replacements) {
            Objects.requireNonNull(replacement, "replacements must not contain null");
        }

        return new Message(message, replacements);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static LocalizedMessage of(@NotNull MessageKeyProvider messageKeyProvider, @NotNull MessageReplacement... replacements) {
        return of(messageKeyProvider, "{error_key: %s}".formatted(messageKeyProvider.getMessageKey().getKey()), replacements);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static LocalizedMessage of(
            @NotNull MessageKeyProvider messageKeyProvider,
            @NotNull String nonLocalizedMessage,
            @NotNull MessageReplacement... replacements) {
        Objects.requireNonNull(messageKeyProvider, "messageKeyProvider must not be null");
        Objects.requireNonNull(nonLocalizedMessage, "message must not be null");
        for (MessageReplacement replacement : replacements) {
            Objects.requireNonNull(replacement, "replacements must not contain null");
        }

        return new LocalizedMessage(messageKeyProvider, nonLocalizedMessage, replacements);
    }

    private final @NotNull String message;
    protected final @NotNull MessageReplacement[] replacements;

    protected Message(@NotNull String message, @NotNull MessageReplacement... replacements) {
        this.message = message;
        this.replacements = replacements;
    }

    public @NotNull String[] getReplacements() {
        return toReplacementsArray(replacements);
    }

    public @NotNull String[] getReplacements(@NotNull Locales locales, @Nullable CommandIssuer commandIssuer) {
        return toReplacementsArray(locales, commandIssuer, replacements);
    }

    public @NotNull String raw() {
        return message;
    }

    public @NotNull String formatted() {
        String[] parsedReplacements = getReplacements();
        if (parsedReplacements.length == 0) {
            return raw();
        }
        return ACFUtil.replaceStrings(message, parsedReplacements);
    }

    public @NotNull String formatted(@NotNull Locales locales) {
        return formatted(locales, null);
    }

    public @NotNull String formatted(@NotNull CommandIssuer commandIssuer) {
        return formatted(commandIssuer.getManager().getLocales(), commandIssuer);
    }

    public @NotNull String formatted(@NotNull Locales locales, @Nullable CommandIssuer commandIssuer) {
        String[] parsedReplacements = getReplacements(locales, commandIssuer);
        if (parsedReplacements.length == 0) {
            return raw();
        }
        return ACFUtil.replaceStrings(message, parsedReplacements);
    }

    private static String[] toReplacementsArray(@NotNull MessageReplacement... replacements) {
        String[] replacementsArray = new String[replacements.length * 2];
        int i = 0;
        for (MessageReplacement replacement : replacements) {
            replacementsArray[i++] = replacement.getKey();
            replacementsArray[i++] = replacement.getReplacement().fold(s -> s, Message::formatted);
        }
        return replacementsArray;
    }

    private static String[] toReplacementsArray(
            @NotNull Locales locales,
            @Nullable CommandIssuer commandIssuer,
            @NotNull MessageReplacement... replacements) {
        String[] replacementsArray = new String[replacements.length * 2];
        int i = 0;
        for (MessageReplacement replacement : replacements) {
            replacementsArray[i++] = replacement.getKey();
            replacementsArray[i++] = replacement.getReplacement().fold(
                    str -> str,
                    message -> message.formatted(locales, commandIssuer));
        }
        return replacementsArray;
    }
}

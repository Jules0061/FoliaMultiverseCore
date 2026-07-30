package org.mvplugins.multiverse.core.utils.text;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.utils.ReflectHelper;

@ApiStatus.AvailableSince("5.1")
public final class ChatTextFormatter {

    private static final TextFormatter wrapper;

    static {
        if (ReflectHelper.hasClass("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer")
         && ReflectHelper.hasClass("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer")) {
            wrapper = new AdventureTextFormatter();
        } else {
            wrapper = new ChatColorTextFormatter();
        }
    }

    @ApiStatus.AvailableSince("5.1")
    public static void sendFormattedMessage(@NotNull CommandSender sender, @Nullable String message) {
        wrapper.sendFormattedMessage(sender, message);
    }

    public static @Nullable String removeColor(@Nullable String message) {
        return wrapper.removeColor(message);
    }

    @ApiStatus.AvailableSince("5.1")
    public static @Nullable String removeAmpColor(@Nullable String message) {
        return wrapper.removeAmpColor(message);
    }

    @ApiStatus.AvailableSince("5.1")
    public static @Nullable String removeSectionColor(@Nullable String message) {
        return wrapper.removeSectionColor(message);
    }

    @ApiStatus.AvailableSince("5.1")
    public static @Nullable String colorize(@Nullable String message) {
        return wrapper.colorize(message);
    }
}

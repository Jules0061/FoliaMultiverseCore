package org.mvplugins.multiverse.core.utils.compatibility;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

@ApiStatus.AvailableSince("5.8")
public final class GameRuleCompatibility {

    @ApiStatus.AvailableSince("5.8")
    public static Stream<GameRule<?>> values() {
        return Registry.GAME_RULE.stream().map(gameRule -> (GameRule<?>) gameRule);
    }

    @ApiStatus.AvailableSince("5.8")
    @SuppressWarnings("unchecked")
    public static <T> @Nullable GameRule<T> getByName(@Nullable String rule) {
        if (rule == null) {
            return null;
        }
        NamespacedKey key = NamespacedKey.fromString(rule);
        if (key == null) {
            return null;
        }
        return (GameRule<T>) Registry.GAME_RULE.get(key);
    }

    @ApiStatus.AvailableSince("5.8")
    @SuppressWarnings("removal")
    public static @NotNull String getName(@NotNull GameRule<?> gameRule) {
        return gameRule.getName();
    }

    private GameRuleCompatibility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

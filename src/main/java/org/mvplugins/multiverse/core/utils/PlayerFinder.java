package org.mvplugins.multiverse.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.base.Strings;
import io.vavr.control.Try;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerFinder {

    private static final List<String> VANILLA_SELECTORS = List.of("@a", "@e", "@r", "@p", "@s");

    public static @Nullable Player get(@Nullable String playerIdentifier) {
        return get(playerIdentifier, Bukkit.getConsoleSender());
    }

    public static @Nullable Player get(@Nullable String playerIdentifier, @NotNull CommandSender sender) {
        if (playerIdentifier == null) {
            return null;
        }

        Player targetPlayer = getByName(playerIdentifier);
        if (targetPlayer != null) {
            return targetPlayer;
        }

        targetPlayer = getByUuid(playerIdentifier);
        if (targetPlayer != null) {
            return targetPlayer;
        }

        return getBySelector(playerIdentifier, sender);
    }

    public static @NotNull List<Player> getMulti(@Nullable String playerIdentifiers) {
        return getMulti(playerIdentifiers, Bukkit.getConsoleSender());
    }

    public static @NotNull List<Player> getMulti(@Nullable String playerIdentifiers,
                                                 @NotNull CommandSender sender
    ) {
        return tryGetMulti(playerIdentifiers, sender)
                .getOrElse(Collections.emptyList());
    }

    @ApiStatus.AvailableSince("5.4")
    public static @NotNull Try<@NotNull List<Player>> tryGetMulti(@Nullable String playerIdentifiers,
                                                                   @NotNull CommandSender sender
    ) {
        if (playerIdentifiers == null || Strings.isNullOrEmpty(playerIdentifiers)) {
            return Try.success(Collections.emptyList());
        }

        if (isSelector(playerIdentifiers)) {
            return tryGetMultiBySelector(playerIdentifiers, sender);
        }

        List<Player> playerResults = new ArrayList<>();
        String[] playerIdentifierArray = REPatterns.COMMA.split(playerIdentifiers);
        for (String playerIdentifier : playerIdentifierArray) {
            Player targetPlayer = getByName(playerIdentifier);
            if (targetPlayer != null) {
                playerResults.add(targetPlayer);
                continue;
            }
            targetPlayer = getByUuid(playerIdentifier);
            if (targetPlayer != null) {
                playerResults.add(targetPlayer);
                continue;
            }
            Try<@NotNull List<Player>> selectorParseResult = tryGetMultiBySelector(playerIdentifier, sender);
            if  (selectorParseResult.isFailure()) {
                return Try.failure(selectorParseResult.getCause());
            }
            playerResults.addAll(selectorParseResult.getOrElse(Collections.emptyList()));
        }
        return Try.success(playerResults);
    }

    @ApiStatus.AvailableSince("5.4")
    public static boolean isSelector(@NotNull String playerIdentifier) {
        return VANILLA_SELECTORS.stream().anyMatch(playerIdentifier::startsWith);
    }

    @Nullable
    public static Player getByName(@NotNull String playerName) {
        return Bukkit.getPlayerExact(playerName);
    }

    public static @Nullable Player getByUuid(@NotNull String playerUuid) {
        if (!REPatterns.UUID.matcher(playerUuid).matches()) {
            return null;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(playerUuid);
        } catch (Exception e) {
            return null;
        }
        return getByUuid(uuid);
    }

    public static @Nullable Player getByUuid(@NotNull UUID playerUuid) {
        return Bukkit.getPlayer(playerUuid);
    }

    public static @Nullable Player getBySelector(@NotNull String playerSelector,
                                                 @NotNull CommandSender sender
    ) {
        List<Player> matchedPlayers = getMultiBySelector(playerSelector, sender);
        if (matchedPlayers.isEmpty()) {
            Logging.fine("No player found with selector '%s' for %s.", playerSelector, sender.getName());
            return null;
        }
        if (matchedPlayers.size() > 1) {
            Logging.warning("Ambiguous selector result '%s' for %s (more than one player matched) - %s",
                    playerSelector, sender.getName(), matchedPlayers.toString());
            return null;
        }
        return matchedPlayers.get(0);
    }

    public static @NotNull List<Player> getMultiBySelector(@NotNull String playerSelector,
                                                            @NotNull CommandSender sender
    ) {
        return tryGetMultiBySelector(playerSelector, sender)
                .onFailure(throwable -> Logging.warning(
                        "Error selecting entities with selector '%s' for %s: %s",
                        playerSelector, sender.getName(), throwable.getMessage()
                ))
                .getOrElse(Collections::emptyList);
    }

    @ApiStatus.AvailableSince("5.4")
    public static @NotNull Try<@NotNull List<Player>> tryGetMultiBySelector(@NotNull String playerSelector,
                                                                            @NotNull CommandSender sender
    ) {
        if (playerSelector.charAt(0) != '@') {
            return Try.success(Collections.emptyList());
        }
        return Try.of(() -> Bukkit.selectEntities(sender, playerSelector).stream()
                .filter(e -> e instanceof Player)
                .map(e -> ((Player) e))
                .collect(Collectors.toList()));
    }
}

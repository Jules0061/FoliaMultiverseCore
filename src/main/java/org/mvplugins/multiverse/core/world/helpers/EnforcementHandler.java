package org.mvplugins.multiverse.core.world.helpers;

import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.permissions.CorePermissionsChecker;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;

@Service
public final class EnforcementHandler {

    private final CoreConfig config;
    private final CorePermissionsChecker permissionsChecker;
    private final Provider<WorldManager> worldManagerProvider;

    @Inject
    EnforcementHandler(CoreConfig config, CorePermissionsChecker permissionsChecker, Provider<WorldManager> worldManagerProvider) {
        this.config = config;
        this.permissionsChecker = permissionsChecker;
        this.worldManagerProvider = worldManagerProvider;
    }

    public void handleAllGameModeEnforcement(@NotNull LoadedMultiverseWorld world) {
        if (!config.getEnforceGameMode()) {
            return;
        }
        world.getPlayers().peek(players -> players.forEach(this::handleGameModeEnforcement));
    }

    public void handleGameModeEnforcement(@NotNull Player player) {
        if (!config.getEnforceGameMode()) {
            return;
        }
        worldManagerProvider.get().getLoadedWorld(player.getWorld()).peek(world -> {
            if (permissionsChecker.hasGameModeBypassPermission(player, world)) {
                Logging.finer("Player is immune to gamemode enforcement: %s", player.getName());
                return;
            }
            Logging.finer("Handling gamemode for player in world '%s': %s, Changing to %s",
                    world.getName(), player.getName(), world.getGameMode());
            player.setGameMode(world.getGameMode());
        }).onEmpty(() ->
                Logging.fine("Player %s is not in a Multiverse world, gamemode enforcement will not apply",
                        player.getName()));
    }

    public void handleAllFlightEnforcement(@NotNull LoadedMultiverseWorld world) {
        if (!config.getEnforceFlight()) {
            return;
        }
        world.getPlayers().peek(players -> players.forEach(this::handleFlightEnforcement));
    }

    public void handleFlightEnforcement(@NotNull Player player) {
        if (!config.getEnforceFlight()) {
            return;
        }
        worldManagerProvider.get().getLoadedWorld(player.getWorld()).peek(world -> {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                player.setAllowFlight(true);
                player.setFlying(true);
                return;
            }

            if (world.isAllowFlight()) {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    player.setAllowFlight(true);
                }
                return;
            }

            if (player.getAllowFlight() && player.getGameMode() != GameMode.CREATIVE) {
                if (player.isFlying()) {
                    player.setFlying(false);
                }
                player.setAllowFlight(false);
            }
        }).onEmpty(() ->
                Logging.fine("Player %s is not in a Multiverse world, flight enforcement will not apply",
                        player.getName()));
    }
}

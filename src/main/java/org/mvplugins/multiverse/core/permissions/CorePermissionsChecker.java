package org.mvplugins.multiverse.core.permissions;

import jakarta.inject.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.destination.Destination;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.destination.DestinationSuggestionPacket;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mvplugins.multiverse.core.permissions.PermissionUtils.concatPermission;
import static org.mvplugins.multiverse.core.permissions.PermissionUtils.hasPermission;

@Service
public final class CorePermissionsChecker {

    private final CoreConfig config;
    private final DestinationsProvider destinationsProvider;
    private final WorldManager worldManager;

    @Inject
    CorePermissionsChecker(
            @NotNull CoreConfig config,
            @NotNull DestinationsProvider destinationsProvider,
            @NotNull WorldManager worldManager) {
        this.config = config;
        this.destinationsProvider = destinationsProvider;
        this.worldManager = worldManager;
    }

    @ApiStatus.AvailableSince("5.2")
    public boolean hasJoinLocationBypassPermission(@NotNull CommandSender sender) {
        return hasPermission(sender, CorePermissions.JOINLOCATION_BYPASS);
    }

    public boolean hasWorldAccessPermission(@NotNull CommandSender sender, @NotNull MultiverseWorld world) {
        return hasPermission(sender, concatPermission(CorePermissions.WORLD_ACCESS, world.getName()));
    }

    public boolean hasWorldExemptPermission(@NotNull CommandSender sender, @NotNull MultiverseWorld world) {
        return hasPermission(sender, concatPermission(CorePermissions.WORLD_EXEMPT, world.getName()));
    }

    public boolean hasPlayerLimitBypassPermission(@NotNull CommandSender sender, @NotNull MultiverseWorld world) {
        return hasPermission(sender, concatPermission(CorePermissions.PLAYERLIMIT_BYPASS, world.getName()));
    }

    public boolean hasGameModeBypassPermission(@NotNull CommandSender sender, @NotNull MultiverseWorld world) {
        return hasPermission(sender, concatPermission(CorePermissions.GAMEMODE_BYPASS, world.getName()));
    }

    public boolean checkSpawnPermission(
            @NotNull CommandSender teleporter,
            @NotNull List<Entity> entities,
            @NotNull MultiverseWorld world) {
        return Scope.getApplicableScopes(teleporter, entities).stream()
                .allMatch(scope -> checkSpawnPermission(teleporter, scope, world));
    }

    public boolean checkSpawnPermission(
            @NotNull CommandSender teleporter,
            @NotNull Entity entity,
            @NotNull MultiverseWorld world) {
        return checkSpawnPermission(teleporter, Scope.getApplicableScope(teleporter, entity), world);
    }

    public boolean checkSpawnPermission(
            @NotNull CommandSender teleporter,
            @NotNull Scope scope,
            @NotNull MultiverseWorld world) {
        if (config.getUseFinerTeleportPermissions()) {
            return hasSpawnPermission(teleporter, scope, world);
        }
        return hasSpawnPermission(teleporter, scope, null);
    }

    public boolean hasAnySpawnPermission(@NotNull CommandSender sender) {
        return hasAnySpawnPermission(sender, Scope.values());
    }

    public boolean hasAnySpawnPermission(@NotNull CommandSender sender, @NotNull Scope scope) {
        return hasAnySpawnPermission(sender, new Scope[]{scope});
    }

    public boolean hasAnySpawnPermission(@NotNull CommandSender sender, @NotNull Scope[] scopes) {
        if (config.getUseFinerTeleportPermissions()) {
            return worldManager.getLoadedWorlds().stream().anyMatch(world ->
                    Arrays.stream(scopes).anyMatch(scope -> hasSpawnPermission(sender, scope, world)));
        }
        return Arrays.stream(scopes).anyMatch(scope -> hasSpawnPermission(sender, scope, null));
    }

    private boolean hasSpawnPermission(@NotNull CommandSender sender, @NotNull Scope scope, @Nullable MultiverseWorld world) {
        if (world == null) {
            return hasPermission(sender, concatPermission(CorePermissions.SPAWN, scope.getScope()));
        }
        return hasPermission(sender, concatPermission(CorePermissions.SPAWN, scope.getScope(), world.getName()));
    }

    public boolean checkDestinationPacketPermission(
            @NotNull CommandSender teleporter,
            @NotNull List<Entity> teleportees,
            @NotNull DestinationSuggestionPacket packet) {
        return Scope.getApplicableScopes(teleporter, teleportees).stream()
                .allMatch(scope -> checkDestinationPacketPermission(teleporter, scope, packet));
    }

    public boolean checkDestinationPacketPermission(
            @NotNull CommandSender teleporter,
            @NotNull Entity teleportee,
            @NotNull DestinationSuggestionPacket packet) {
        return checkDestinationPacketPermission(teleporter, Scope.getApplicableScope(teleporter, teleportee), packet);
    }

    public boolean checkDestinationPacketPermission(
            @NotNull CommandSender teleporter,
            @NotNull Scope scope,
            @NotNull DestinationSuggestionPacket packet) {
        return hasTeleportPermission(
                teleporter,
                scope,
                packet.destination().getIdentifier(),
                config.getUseFinerTeleportPermissions() ? packet.finerPermissionSuffix() : null);
    }

    public boolean checkTeleportPermission(
            @NotNull CommandSender teleporter,
            @NotNull List<Entity> teleportees,
            @NotNull DestinationInstance<?, ?> destination) {
        return Scope.getApplicableScopes(teleporter, teleportees).stream()
                .allMatch(scope -> checkTeleportPermission(teleporter, scope, destination));
    }

    public boolean checkTeleportPermission(
            @NotNull CommandSender teleporter,
            @NotNull Entity teleportee,
            @NotNull DestinationInstance<?, ?> destination) {
        return checkTeleportPermission(teleporter, Scope.getApplicableScope(teleporter, teleportee), destination);
    }

    public boolean checkTeleportPermission(
            @NotNull CommandSender teleporter,
            @NotNull Scope scope,
            @NotNull DestinationInstance<?, ?> destination) {
        if (config.getUseFinerTeleportPermissions()) {
            return hasTeleportPermission(teleporter, scope, destination.getIdentifier(), destination.getFinerPermissionSuffix().getOrNull());
        }
        return hasTeleportPermission(teleporter, scope, destination.getIdentifier(), null);
    }

    public boolean hasAnyTeleportPermission(@NotNull CommandSender sender) {
        return hasAnyTeleportPermission(sender, Scope.values());
    }

    public boolean hasAnyTeleportPermission(@NotNull CommandSender sender, @NotNull Scope scope) {
        return hasAnyTeleportPermission(sender, new Scope[]{scope});
    }

    public boolean hasAnyTeleportPermission(@NotNull CommandSender sender, @NotNull Scope[] scopes) {
        if (!config.getUseFinerTeleportPermissions()) {
            for (Destination destination : destinationsProvider.getDestinations()) {
                for (Scope scope : scopes) {
                    if (hasTeleportPermission(sender, scope, destination.getIdentifier(), null)) {
                        return true;
                    }
                }
            }
            return false;
        }

        for (DestinationSuggestionPacket suggestion : destinationsProvider.suggestDestinations(sender, null)) {
            for (Scope scope : scopes) {
                if (hasTeleportPermission(sender, scope, suggestion.destination().getIdentifier(), suggestion.finerPermissionSuffix())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasTeleportPermission(@NotNull CommandSender sender, @NotNull Scope scope, @NotNull String identifier, @Nullable String finerPermissionSuffix) {
        if (finerPermissionSuffix == null) {
            return hasPermission(sender, concatPermission(CorePermissions.TELEPORT, scope.getScope(), identifier));
        }
        return hasPermission(sender, concatPermission(CorePermissions.TELEPORT, scope.getScope(), identifier, finerPermissionSuffix));
    }

    public enum Scope {
        SELF("self"),
        OTHER("other"),
        ;

        private final String scope;

        Scope(String scope) {
            this.scope = scope;
        }

        public String getScope() {
            return scope;
        }

        @Override
        public String toString() {
            return scope;
        }

        public static Scope getApplicableScope(CommandSender teleporter, Entity entity) {
            if (teleporter instanceof Entity senderEntity && senderEntity.equals(entity)) {
                return Scope.SELF;
            }
            return Scope.OTHER;
        }

        public static List<Scope> getApplicableScopes(CommandSender sender, List<Entity> entities) {
            List<Scope> applicableScopes = new ArrayList<>(Scope.values().length);
            if (sender instanceof Entity entity) {
                if (entities.contains(entity)) {
                    applicableScopes.add(Scope.SELF);
                }
                if (entities.stream().anyMatch(e -> !e.equals(entity))) {
                    applicableScopes.add(Scope.OTHER);
                }
            } else {
                applicableScopes.add(Scope.OTHER);
            }
            return applicableScopes;
        }
    }
}

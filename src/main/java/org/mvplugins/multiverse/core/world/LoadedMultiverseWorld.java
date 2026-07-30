package org.mvplugins.multiverse.core.world;

import java.util.List;
import java.util.UUID;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Option;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.teleportation.BlockSafety;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.world.entity.EntityPurger;
import org.mvplugins.multiverse.core.world.location.NullSpawnLocation;
import org.mvplugins.multiverse.core.world.location.SpawnLocation;

public final class LoadedMultiverseWorld extends MultiverseWorld {

    private final UUID worldUid;

    private final BlockSafety blockSafety;
    private final LocationManipulation locationManipulation;
    private final EntityPurger entityPurger;

    LoadedMultiverseWorld(
            @NotNull World world,
            @NotNull WorldConfig worldConfig,
            @NotNull CoreConfig config,
            @NotNull BlockSafety blockSafety,
            @NotNull LocationManipulation locationManipulation,
            @NotNull EntityPurger entityPurger
    ) {
        super(worldConfig, config);
        this.worldUid = world.getUID();
        this.blockSafety = blockSafety;
        this.locationManipulation = locationManipulation;
        this.entityPurger = entityPurger;

        setupWorldConfig(world);
        setupSpawnLocation(world);
        purgeEntitiesOnLoad();
    }

    private void setupWorldConfig(World world) {
        worldConfig.setMVWorld(this);
        worldConfig.load();
        worldConfig.setEnvironment(world.getEnvironment());
        worldConfig.setSeed(world.getSeed());
    }

    private void setupSpawnLocation(World world) {
        Location spawnLocation = worldConfig.getSpawnLocation();
        if (spawnLocation == null || spawnLocation instanceof NullSpawnLocation) {
            SpawnLocation newLocation = new SpawnLocation(readSpawnFromWorld(world));
            worldConfig.setSpawnLocation(newLocation);
        }
    }

    private Location readSpawnFromWorld(World world) {
        Location location = world.getSpawnLocation();

        if (blockSafety.canSpawnAtLocationSafely(location)) {
            return location;
        }

        if (!this.getAdjustSpawn()) {
            Logging.fine("Spawn location from world.dat file was unsafe!!");
            Logging.fine("NOT adjusting spawn for '" + this.getAliasOrName() + "' because you told me not to.");
            Logging.fine("To turn on spawn adjustment for this world simply type:");
            Logging.fine("/mv modify %s set adjust-spawn true", getName());
            return location;
        }

        Logging.warning("Spawn location from world.dat file was unsafe. Adjusting...");
        Logging.warning("Original Location: " + locationManipulation.strCoordsRaw(location));
        Location newSpawn = blockSafety.findSafeSpawnLocation(location);
        if (newSpawn != null) {
            Logging.info("New Spawn for '%s' is located at: %s",
                    this.getName(), locationManipulation.locationToString(newSpawn));
            return newSpawn;
        }

        Logging.fine("Checking for a safe location using top block...");
        Location newerSpawn;
        newerSpawn = blockSafety.getTopBlock(new Location(world, 0, 0, 0));
        if (newerSpawn != null) {
            Logging.info("New Spawn for '%s' is located at: %s",
                    this.getName(), locationManipulation.locationToString(newerSpawn));
            return newerSpawn;
        }

        Logging.severe("Safe spawn NOT found!!!");
        return location;
    }

    private void purgeEntitiesOnLoad() {
        if (config.isAutoPurgeEntities()) {
            entityPurger.purgeEntities(this);
        }
    }

    public UUID getUID() {
        return worldUid;
    }

    public Option<World> getBukkitWorld() {
        return Option.of(Bukkit.getWorld(worldUid));
    }

    public Option<WorldType> getWorldType() {
        return getBukkitWorld().map(World::getWorldType);
    }

    public Option<Boolean> canGenerateStructures() {
        return getBukkitWorld().map(World::canGenerateStructures);
    }

    public Option<List<Player>> getPlayers() {
        return getBukkitWorld().map(World::getPlayers);
    }

    public Option<WorldBorder> getWorldBorder() {
        return getBukkitWorld().map(World::getWorldBorder);
    }

    @Override
    void setWorldConfig(@NotNull WorldConfig worldConfig) {
        super.setWorldConfig(worldConfig);
        setupWorldConfig(getBukkitWorld().get());
    }

    @Override
    public String toString() {
        return "LoadedMultiverseWorld{"
                + "key='" + getKey() + "', "
                + "name='" + getName() + "', "
                + "env='" + getEnvironment() + "', "
                + "type='" + getWorldType().getOrNull() + "', "
                + "gen='" + getGenerator() + "'"
                + '}';
    }
}

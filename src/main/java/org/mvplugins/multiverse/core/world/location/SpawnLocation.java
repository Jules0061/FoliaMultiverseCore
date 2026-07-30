package org.mvplugins.multiverse.core.world.location;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

@SerializableAs("MVSpawnLocation")
public class SpawnLocation extends Location implements ConfigurationSerializable {
    private Reference<World> worldRef;

    public SpawnLocation(double x, double y, double z) {
        super(null, x, y, z);
    }

    public SpawnLocation(double x, double y, double z, float yaw, float pitch) {
        super(null, x, y, z, yaw, pitch);
    }

    public SpawnLocation(Location loc) {
        this(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public Location toBukkitLocation() {
        return new Location(getWorld(), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    @Override
    public World getWorld() {
        return (this.worldRef != null) ? this.worldRef.get() : null;
    }

    @Override
    public void setWorld(World world) {
        this.worldRef = new WeakReference<>(world);
    }

    @Override
    public @NotNull Chunk getChunk() {
        World world = this.worldRef != null ? this.worldRef.get() : null;
        if (world != null) {
            return world.getChunkAt(this);
        }
        throw new IllegalStateException("World is null");
    }

    @Override
    public @NotNull Block getBlock() {
        World world = this.worldRef != null ? this.worldRef.get() : null;
        if (world != null) {
            return world.getBlockAt(this);
        }
        throw new IllegalStateException("World is null");
    }

    @Override
    @NotNull
    public SpawnLocation clone() {
        try {
            return (SpawnLocation) super.clone();
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("x", getX());
        map.put("y", getY());
        map.put("z", getZ());
        map.put("pitch", getPitch());
        map.put("yaw", getYaw());
        return map;
    }

    public static @NotNull SpawnLocation deserialize(Map<String, Object> args) {
        double x = ((Number) args.get("x")).doubleValue();
        double y = ((Number) args.get("y")).doubleValue();
        double z = ((Number) args.get("z")).doubleValue();
        float pitch = ((Number) args.get("pitch")).floatValue();
        float yaw = ((Number) args.get("yaw")).floatValue();
        return new SpawnLocation(x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "Location{world=" + getWorld()
                + ",x=" + this.getX()
                + ",y=" + this.getY()
                + ",z=" + this.getZ()
                + ",pitch=" + this.getPitch()
                + ",yaw=" + this.getYaw()
                + '}';
    }
}

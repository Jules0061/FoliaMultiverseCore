package org.mvplugins.multiverse.core.world.location;

import java.util.Collections;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@SerializableAs("MVNullLocation (It's a bug if you see this in your config file)")
public final class NullSpawnLocation extends SpawnLocation {
    private static final NullSpawnLocation INSTANCE = new NullSpawnLocation();

    public static NullSpawnLocation get() {
        return INSTANCE;
    }

    private NullSpawnLocation() {
        super(0, -1, 0);
    }

    @Override
    public @NotNull NullSpawnLocation clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Collections.emptyMap();
    }

    public static NullSpawnLocation deserialize(Map<String, Object> args) {
        return new NullSpawnLocation();
    }

    @Override
    public @NotNull Vector toVector() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullSpawnLocation;
    }

    @Override
    public String toString() {
        return "Location{null}";
    }
}

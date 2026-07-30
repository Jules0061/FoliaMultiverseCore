package org.mvplugins.multiverse.core.anchor;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.world.location.UnloadedWorldLocation;

public final class MultiverseAnchor {

    private final String name;
    private UnloadedWorldLocation location;

    MultiverseAnchor(String name, UnloadedWorldLocation location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location.toBukkitLocation();
    }

    void setLocation(Location location) {
        this.location = new UnloadedWorldLocation(location);
    }

    @Nullable World getLocationWorld() {
        if (location == null) {
            return null;
        }
        return location.getWorld();
    }
}

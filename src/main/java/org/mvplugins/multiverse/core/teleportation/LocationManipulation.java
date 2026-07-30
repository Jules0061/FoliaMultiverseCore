package org.mvplugins.multiverse.core.teleportation;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Strings;
import io.vavr.control.Try;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.core.world.location.UnloadedWorldLocation;

@Service
public final class LocationManipulation {
    private static final Map<String, Integer> ORIENTATION_INTS;

    static {

        ORIENTATION_INTS = Map.ofEntries(
                Map.entry("n", 180),
                Map.entry("ne", 225),
                Map.entry("e", 270),
                Map.entry("se", 315),
                Map.entry("s", 0),
                Map.entry("sw", 45),
                Map.entry("w", 90),
                Map.entry("nw", 135),
                Map.entry("north", 180),
                Map.entry("northeast", 225),
                Map.entry("east", 270),
                Map.entry("southeast", 315),
                Map.entry("south", 0),
                Map.entry("southwest", 45),
                Map.entry("west", 90),
                Map.entry("northwest", 135)
        );
    }

    public String locationToString(Location location) {
        if (location == null) {
            return "";
        }
        String worldName = location instanceof UnloadedWorldLocation unloadedWorldLocation
                ? unloadedWorldLocation.getWorldName()
                : Try.of(() -> location.getWorld().getName()).getOrNull();
        if (worldName == null) {
            return "";
        }
        return String.format(Locale.ENGLISH, "%s:%.2f,%.2f,%.2f:%.2f:%.2f", worldName,
                location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location getBlockLocation(Location l) {
        l.setX(l.getBlockX());
        l.setY(l.getBlockY());
        l.setZ(l.getBlockZ());
        return l;
    }

    @Nullable
    public Location stringToLocation(String locationString) {
        if (locationString == null) {
            return null;
        }

        String[] split = REPatterns.COLON.split(locationString);
        if (split.length < 2 || split.length > 4) {
            return null;
        }
        String[] xyzsplit = REPatterns.COMMA.split(split[1]);
        if (xyzsplit.length != 3) {
            return null;
        }

        String worldName = split[0];
        if (Strings.isNullOrEmpty(worldName)) {
            return null;
        }

        try {
            float pitch = 0;
            float yaw = 0;
            if (split.length >= 3) {
                yaw = (float) Double.parseDouble(split[2]);
            }
            if (split.length == 4) {
                pitch = (float) Double.parseDouble(split[3]);
            }
            return new UnloadedWorldLocation(worldName, Double.parseDouble(xyzsplit[0]), Double.parseDouble(xyzsplit[1]), Double.parseDouble(xyzsplit[2]), yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String strCoords(Location l) {
        String result = "";
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(2);
        result += ChatColor.WHITE + "X: " + ChatColor.AQUA + df.format(l.getX()) + " ";
        result += ChatColor.WHITE + "Y: " + ChatColor.AQUA + df.format(l.getY()) + " ";
        result += ChatColor.WHITE + "Z: " + ChatColor.AQUA + df.format(l.getZ()) + " ";
        result += ChatColor.WHITE + "P: " + ChatColor.GOLD + df.format(l.getPitch()) + " ";
        result += ChatColor.WHITE + "Y: " + ChatColor.GOLD + df.format(l.getYaw()) + " ";
        return result;
    }

    public String strCoordsRaw(Location l) {
        if (l == null) {
            return "null";
        }
        String result = "";
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(2);
        result += "X: " + df.format(l.getX()) + " ";
        result += "Y: " + df.format(l.getY()) + " ";
        result += "Z: " + df.format(l.getZ()) + " ";
        result += "P: " + df.format(l.getPitch()) + " ";
        result += "Y: " + df.format(l.getYaw()) + " ";
        return result;
    }

    public String getDirection(Location location) {
        double r = (location.getYaw() % 360) + 180;
        String dir;
        if (r < 22.5)
            dir = "n";
        else if (r < 67.5)
            dir = "ne";
        else if (r < 112.5)
            dir = "e";
        else if (r < 157.5)
            dir = "se";
        else if (r < 202.5)
            dir = "s";
        else if (r < 247.5)
            dir = "sw";
        else if (r < 292.5)
            dir = "w";
        else if (r < 337.5)
            dir = "nw";
        else
            dir = "n";

        return dir;
    }

    public float getYaw(String orientation) {
        if (orientation == null) {
            return 0;
        }
        if (ORIENTATION_INTS.containsKey(orientation.toLowerCase(Locale.ENGLISH))) {
            return ORIENTATION_INTS.get(orientation.toLowerCase(Locale.ENGLISH));
        }
        return 0;
    }

    public float getSpeed(Vector v) {
        return (float) Math.sqrt(v.getX() * v.getX() + v.getZ() * v.getZ());
    }

    public Vector getTranslatedVector(Vector v, String direction) {
        if (direction == null) {
            return v;
        }
        float speed = getSpeed(v);
        float halfSpeed = (float) (speed / 2.0);
        if (direction.equalsIgnoreCase("n")) {
            return new Vector(0, 0, -1 * speed);
        } else if (direction.equalsIgnoreCase("ne")) {
            return new Vector(halfSpeed, 0, -1 * halfSpeed);
        } else if (direction.equalsIgnoreCase("e")) {
            return new Vector(speed, 0, 0);
        } else if (direction.equalsIgnoreCase("se")) {
            return new Vector(halfSpeed, 0, halfSpeed);
        } else if (direction.equalsIgnoreCase("s")) {
            return new Vector(0, 0, speed);
        } else if (direction.equalsIgnoreCase("sw")) {
            return new Vector(-1 * halfSpeed, 0, halfSpeed);
        } else if (direction.equalsIgnoreCase("w")) {
            return new Vector(-1 * speed, 0, 0);
        } else if (direction.equalsIgnoreCase("nw")) {
            return new Vector(-1 * halfSpeed, 0, -1 * halfSpeed);
        }
        return v;
    }

    public Location getNextBlock(Vehicle v) {
        Vector vector = v.getVelocity();
        Location location = v.getLocation();
        int x = vector.getX() < 0 ? vector.getX() == 0 ? 0 : -1 : 1;
        int z = vector.getZ() < 0 ? vector.getZ() == 0 ? 0 : -1 : 1;
        return location.add(x, 0, z);
    }
}

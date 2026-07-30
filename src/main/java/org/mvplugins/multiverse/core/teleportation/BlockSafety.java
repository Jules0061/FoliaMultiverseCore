package org.mvplugins.multiverse.core.teleportation;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Option;
import jakarta.inject.Inject;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.config.CoreConfig;

@Service
public final class BlockSafety {

    private static final int FINEST_DEBUG_LEVEL = 3;

    private final CoreConfig config;
    private final LocationManipulation locationManipulation;

    @Inject
    BlockSafety(@NotNull CoreConfig config, @NotNull LocationManipulation locationManipulation) {
        this.config = config;
        this.locationManipulation = locationManipulation;
    }

    public boolean isBlockAboveAir(Location location) {
        return location.getBlock().getRelative(0, -1, 0).getType().isAir();
    }

    public boolean isEntityOnTrack(Location location) {
        return location.getBlock().getBlockData() instanceof Rail;
    }

    public Location getTopBlock(Location location) {
        Location check = location.clone();
        int maxHeight = Option.of(location.getWorld()).map(World::getMaxHeight).getOrElse(127);
        check.setY(maxHeight);
        while (check.getY() > 0) {
            if (canSpawnAtLocationSafely(check)) {
                return check;
            }
            check.setY(check.getY() - 1);
        }
        return null;
    }

    public Location getBottomBlock(Location location) {
        Location check = location.clone();
        int minHeight = Option.of(location.getWorld()).map(World::getMinHeight).getOrElse(0);
        check.setY(minHeight);
        while (check.getY() < 127) {
            if (canSpawnAtLocationSafely(check)) {
                return check;
            }
            check.setY(check.getY() + 1);
        }
        return null;
    }

    public boolean canSpawnCartSafely(Minecart cart) {
        if (isBlockAboveAir(cart.getLocation())) {
            return true;
        }
        return isEntityOnTrack(locationManipulation.getNextBlock(cart));
    }

    public boolean canSpawnVehicleSafely(Vehicle vehicle) {
        return isBlockAboveAir(vehicle.getLocation());
    }

    public boolean canSpawnAtLocationSafely(@NotNull Location location) {
        return canSpawnAtBlockSafely(location.getBlock());
    }

    public boolean canSpawnAtBlockSafely(@NotNull Block block) {
        boolean logFinest = Logging.getDebugLevel() >= FINEST_DEBUG_LEVEL;
        if (logFinest) {
            Logging.finest("Checking spawn safety for location: %s, %s, %s", block.getX(), block.getY(), block.getZ());
        }
        if (!block.getWorld().getWorldBorder().isInside(block.getLocation())) {
            if (logFinest) {
                Logging.finest("Location is outside world border.");
            }
            return false;
        }
        if (isUnsafeSpawnBody(block)) {
            if (logFinest) {
                Logging.finest("Unsafe location for player's body: " + block);
            }
            return false;
        }
        Block airBlockForHead = block.getRelative(0, 1, 0);
        if (isUnsafeSpawnBody(airBlockForHead)) {
            if (logFinest) {
                Logging.finest("Unsafe location for player's head: " + airBlockForHead);
            }
            return false;
        }
        Block standingOnBlock = block.getRelative(0, -1, 0);
        if (isUnsafeSpawnPlatform(standingOnBlock)) {
            if (logFinest) {
                Logging.finest("Unsafe location due to invalid platform: " + standingOnBlock);
            }
            return false;
        }
        if (logFinest) {
            Logging.finest("Location is safe.");
        }
        return true;
    }

    private boolean isUnsafeSpawnBody(@NotNull Block block) {
        Material blockMaterial = block.getType();
        return blockMaterial.isSolid() || blockMaterial == Material.FIRE;
    }

    private boolean isUnsafeSpawnPlatform(@NotNull Block block) {
        return !block.getType().isSolid() || isDeepWater(block);
    }

    private boolean isDeepWater(@NotNull Block block) {
        if (block.getType() != Material.WATER) {
            return false;
        }
        return block.getRelative(0, -1, 0).getType() == Material.WATER;
    }

    public @Nullable Location findSafeSpawnLocation(@NotNull Location location) {
        return findSafeSpawnLocation(
                location,
                config.getSafeLocationHorizontalSearchRadius(),
                config.getSafeLocationVerticalSearchRadius());
    }

    public @Nullable Location findSafeSpawnLocation(@NotNull Location location, int horizontalRange, int verticalRange) {
        Block safeBlock = findSafeSpawnBlock(location.getBlock(), horizontalRange, verticalRange);
        if (safeBlock == null) {
            return null;
        }
        return new Location(
                location.getWorld(),
                safeBlock.getX() + 0.5,
                safeBlock.getY(),
                safeBlock.getZ() + 0.5,
                location.getYaw(),
                location.getPitch());
    }

    public @Nullable Block findSafeSpawnBlock(@NotNull Block block) {
        return findSafeSpawnBlock(
                block,
                config.getSafeLocationHorizontalSearchRadius(),
                config.getSafeLocationVerticalSearchRadius());
    }

    public @Nullable Block findSafeSpawnBlock(@NotNull Block block, int horizontalRange, int verticalRange) {
        Block searchResult = searchAroundXZ(block, horizontalRange);
        if (searchResult != null) {
            return searchResult;
        }
        int maxHeight = block.getWorld().getMaxHeight();
        int minHeight = block.getWorld().getMinHeight();
        for (int i = 1; i <= verticalRange; i++) {
            if (block.getY() + i < maxHeight) {
                searchResult = searchAroundXZ(block.getRelative(0, i, 0), horizontalRange);
                if (searchResult != null) {
                    return searchResult;
                }
            }
            if (block.getY() - i >= minHeight) {
                searchResult = searchAroundXZ(block.getRelative(0, -i, 0), horizontalRange);
                if (searchResult != null) {
                    return searchResult;
                }
            }
        }
        return null;
    }

    @Nullable
    private Block searchAroundXZ(Block block, int radius) {
        if (canSpawnAtBlockSafely(block)) {
            return block;
        }
        for (int r = 1; r <= radius; r++) {
            boolean radiusX = true;
            boolean incrementOffset = false;
            int offset = 0;
            int noOfIterations = r * 2 + 1;
            for (int i = 0; i < noOfIterations; i++) {
                Block searchResult = radiusX
                        ? searchPlusMinusPermutation(block, r, offset)
                        : searchPlusMinusPermutation(block, offset, r);
                if (searchResult != null) {
                    return searchResult;
                }
                if (incrementOffset) {
                    offset++;
                }
                radiusX = !radiusX;
                incrementOffset = !incrementOffset;
            }
        }
        return null;
    }

    @Nullable
    private Block searchPlusMinusPermutation(Block block, int x, int z) {
        Block relative = block.getRelative(-x, 0, -z);
        if (canSpawnAtBlockSafely(relative)) {
            return relative;
        }
        if (z != 0) {
            relative = block.getRelative(-x, 0, z);
            if (canSpawnAtBlockSafely(relative)) {
                return relative;
            }
        }
        if (x != 0) {
            relative = block.getRelative(x, 0, -z);
            if (canSpawnAtBlockSafely(relative)) {
                return relative;
            }
            if (z != 0) {
                relative = block.getRelative(x, 0, z);
                if (canSpawnAtBlockSafely(relative)) {
                    return relative;
                }
            }
        }
        return null;
    }

    public @Nullable Location findPortalBlockNextTo(Location location) {
        if (location.getWorld() == null) {
            return null;
        }
        Block b = location.getWorld().getBlockAt(location);
        Location foundLocation = null;
        if (b.getType() == Material.NETHER_PORTAL) {
            return location;
        }
        if (b.getRelative(BlockFace.NORTH).getType() == Material.NETHER_PORTAL) {
            foundLocation = getCloserBlock(location, b.getRelative(BlockFace.NORTH).getLocation(), foundLocation);
        }
        if (b.getRelative(BlockFace.SOUTH).getType() == Material.NETHER_PORTAL) {
            foundLocation = getCloserBlock(location, b.getRelative(BlockFace.SOUTH).getLocation(), foundLocation);
        }
        if (b.getRelative(BlockFace.EAST).getType() == Material.NETHER_PORTAL) {
            foundLocation = getCloserBlock(location, b.getRelative(BlockFace.EAST).getLocation(), foundLocation);
        }
        if (b.getRelative(BlockFace.WEST).getType() == Material.NETHER_PORTAL) {
            foundLocation = getCloserBlock(location, b.getRelative(BlockFace.WEST).getLocation(), foundLocation);
        }
        return foundLocation;
    }

    private Location getCloserBlock(Location source, Location blockA, Location blockB) {
        if (blockB == null) {
            return blockA;
        }
        blockA.add(.5, 0, .5);
        blockB.add(.5, 0, .5);

        double testA = source.distance(blockA);
        double testB = source.distance(blockB);

        if (testA <= testB) {
            return blockA;
        }
        return blockB;
    }
}

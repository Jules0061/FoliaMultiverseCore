package org.mvplugins.multiverse.core.utils.position;

import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.exceptions.utils.position.PositionParseException;
import org.mvplugins.multiverse.core.utils.REPatterns;

@ApiStatus.AvailableSince("5.3")
public class EntityPosition {

    @ApiStatus.AvailableSince("5.3")
    public static EntityPosition ofAbsolute(double x, double y, double z, double pitch, double yaw) {
        return new EntityPosition(
                VectorPosition.ofAbsolute(x, y, z),
                FaceDirection.ofAbsolute(pitch, yaw)
        );
    }

    @ApiStatus.AvailableSince("5.3")
    public static EntityPosition ofLocation(Location location) {
        return new EntityPosition(VectorPosition.ofLocation(location), FaceDirection.ofLocation(location));
    }

    @ApiStatus.AvailableSince("5.3")
    public static EntityPosition fromString(String positionStr) throws PositionParseException {
        String[] parts = REPatterns.COLON.split(positionStr, 2);
        return parts.length == 2
                ? new EntityPosition(VectorPosition.fromString(parts[0]), FaceDirection.fromString(parts[1]))
                : new EntityPosition(VectorPosition.fromString(parts[0]), FaceDirection.ofAbsolute(0, 0));
    }

    private final VectorPosition vector;
    private final FaceDirection direction;

    @ApiStatus.AvailableSince("5.3")
    public EntityPosition(VectorPosition vector, FaceDirection direction) {
        this.vector = vector;
        this.direction = direction;
    }

    @ApiStatus.AvailableSince("5.3")
    public VectorPosition getVector() {
        return vector;
    }

    @ApiStatus.AvailableSince("5.3")
    public FaceDirection getDirection() {
        return direction;
    }

    @ApiStatus.AvailableSince("5.3")
    public void augmentBukkitLocation(Location base) {
        vector.augmentBukkitLocation(base);
        direction.augmentBukkitLocation(base);
    }

    @ApiStatus.AvailableSince("5.3")
    public Location toBukkitLocation(Location base) {
        return new Location(
                base.getWorld(),
                vector.getX().getValue(base.getX()),
                vector.getY().getValue(base.getY()),
                vector.getZ().getValue(base.getZ()),
                (float) direction.getYaw().getValue(base.getYaw()),
                (float) direction.getPitch().getValue(base.getPitch())
        );
    }

    @Override
    public String toString() {
        return vector + ":" + direction;
    }
}

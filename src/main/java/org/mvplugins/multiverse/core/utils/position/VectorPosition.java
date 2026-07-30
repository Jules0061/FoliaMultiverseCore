package org.mvplugins.multiverse.core.utils.position;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.exceptions.utils.position.PositionParseException;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.utils.REPatterns;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@ApiStatus.AvailableSince("5.3")
public class VectorPosition {

    @ApiStatus.AvailableSince("5.3")
    public static VectorPosition ofAbsolute(double x, double y, double z) {
        return new VectorPosition(
                PositionNumber.ofAbsolute(x),
                PositionNumber.ofAbsolute(y),
                PositionNumber.ofAbsolute(z)
        );
    }

    @ApiStatus.AvailableSince("5.3")
    public static VectorPosition ofVector(Vector vector) {
        return new VectorPosition(
                PositionNumber.ofAbsolute(vector.getX()),
                PositionNumber.ofAbsolute(vector.getY()),
                PositionNumber.ofAbsolute(vector.getZ())
        );
    }

    public static VectorPosition ofLocation(Location location) {
        return new VectorPosition(
                PositionNumber.ofAbsolute(location.getX()),
                PositionNumber.ofAbsolute(location.getY()),
                PositionNumber.ofAbsolute(location.getZ())
        );
    }

    @ApiStatus.AvailableSince("5.3")
    public static VectorPosition fromString(String coordStr) throws PositionParseException {
        String[] parts = REPatterns.COMMA.split(coordStr);
        if (parts.length != 3) {
            throw new PositionParseException(Message.of(MVCorei18n.EXCEPTION_POSITIONPARSE_INVALIDCOORDINATES,
                    replace("{format}").with(coordStr)));
        }
        return new VectorPosition(
                PositionNumber.fromString(parts[0]),
                PositionNumber.fromString(parts[1]),
                PositionNumber.fromString(parts[2])
        );
    }

    private final PositionNumber x;
    private final PositionNumber y;
    private final PositionNumber z;

    @ApiStatus.AvailableSince("5.3")
    public VectorPosition(PositionNumber x, PositionNumber y, PositionNumber z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @ApiStatus.AvailableSince("5.3")
    public PositionNumber getX() {
        return x;
    }

    @ApiStatus.AvailableSince("5.3")
    public PositionNumber getY() {
        return y;
    }

    @ApiStatus.AvailableSince("5.3")
    public PositionNumber getZ() {
        return z;
    }

    @ApiStatus.AvailableSince("5.3")
    public void augmentBukkitVector(Vector base) {
        base.setX(x.getValue(base.getX()));
        base.setY(y.getValue(base.getY()));
        base.setZ(z.getValue(base.getZ()));
    }

    @ApiStatus.AvailableSince("5.3")
    public void augmentBukkitLocation(Location location) {
        location.setX(x.getValue(location.getX()));
        location.setY(y.getValue(location.getY()));
        location.setZ(z.getValue(location.getZ()));
    }

    @ApiStatus.AvailableSince("5.3")
    public Vector toBukkitVector(Vector base) {
        return new Vector(
                x.getValue(base.getX()),
                y.getValue(base.getY()),
                z.getValue(base.getZ())
        );
    }

    @ApiStatus.AvailableSince("5.3")
    public Location toBukkitLocation(Location base) {
        return new Location(
                base.getWorld(),
                x.getValue(base.getX()),
                y.getValue(base.getY()),
                z.getValue(base.getZ()),
                base.getYaw(),
                base.getPitch()
        );
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}

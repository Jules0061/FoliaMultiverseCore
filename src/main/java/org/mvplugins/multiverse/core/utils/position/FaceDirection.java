package org.mvplugins.multiverse.core.utils.position;

import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.exceptions.utils.position.PositionParseException;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.utils.REPatterns;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@ApiStatus.AvailableSince("5.3")
public class FaceDirection {

    @ApiStatus.AvailableSince("5.3")
    public static FaceDirection ofAbsolute(double pitch, double yaw) {
        return new FaceDirection(
                PositionNumber.ofAbsolute(pitch),
                PositionNumber.ofAbsolute(yaw)
        );
    }

    @ApiStatus.AvailableSince("5.3")
    public static FaceDirection ofLocation(org.bukkit.Location location) {
        return new FaceDirection(
                PositionNumber.ofAbsolute(location.getPitch()),
                PositionNumber.ofAbsolute(location.getYaw())
        );
    }

    @ApiStatus.AvailableSince("5.3")
    public static FaceDirection fromString(String directionStr) throws PositionParseException {
        String[] parts = REPatterns.COLON.split(directionStr, 2);
        if (parts.length != 2) {
            throw new PositionParseException(Message.of(MVCorei18n.EXCEPTION_POSITIONPARSE_INVALIDDIRECTION,
                    replace("{format}").with(directionStr)));
        }
        PositionNumber pitch = PositionNumber.fromString(parts[0]);
        PositionNumber yaw = PositionNumber.fromString(parts[1]);
        return new FaceDirection(pitch, yaw);
    }

    private final PositionNumber pitch;
    private final PositionNumber yaw;

    @ApiStatus.AvailableSince("5.3")
    public FaceDirection(PositionNumber pitch, PositionNumber yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @ApiStatus.AvailableSince("5.3")
    public PositionNumber getPitch() {
        return pitch;
    }

    @ApiStatus.AvailableSince("5.3")
    public PositionNumber getYaw() {
        return yaw;
    }

    @ApiStatus.AvailableSince("5.3")
    public void augmentBukkitLocation(Location base) {
        base.setPitch((float) pitch.getValue(base.getPitch()));
        base.setYaw((float) yaw.getValue(base.getYaw()));
    }

    @Override
    public String toString() {
        return pitch + ":" + yaw;
    }
}

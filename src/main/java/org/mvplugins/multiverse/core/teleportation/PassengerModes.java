package org.mvplugins.multiverse.core.teleportation;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("5.1")
public enum PassengerModes implements PassengerMode {
    @ApiStatus.AvailableSince("5.1")
    DEFAULT(false, false, false, false),

    @ApiStatus.AvailableSince("5.1")
    DISMOUNT_PASSENGERS(true, false, false, false),

    @ApiStatus.AvailableSince("5.1")
    DISMOUNT_VEHICLE(true, false, true, false),

    @ApiStatus.AvailableSince("5.1")
    DISMOUNT_ALL(true, false, true, false),

    @ApiStatus.AvailableSince("5.1")
    RETAIN_PASSENGERS(true, true, false, false),

    @ApiStatus.AvailableSince("5.1")
    RETAIN_VEHICLE(false, false, true, true),

    @ApiStatus.AvailableSince("5.1")
    RETAIN_ALL(true, true, true, true),
    ;

    private final boolean dismountPassengers;
    private final boolean passengersFollow;
    private final boolean dismountVehicle;
    private final boolean vehicleFollow;

    PassengerModes(boolean dismountPassengers, boolean mountPassengers, boolean dismountVehicle, boolean mountVehicle) {
        this.dismountPassengers = dismountPassengers;
        this.passengersFollow = mountPassengers;
        this.dismountVehicle = dismountVehicle;
        this.vehicleFollow = mountVehicle;
    }

    @Override
    public boolean isDismountPassengers() {
        return dismountPassengers;
    }

    @Override
    public boolean isPassengersFollow() {
        return passengersFollow;
    }

    @Override
    public boolean isDismountVehicle() {
        return dismountVehicle;
    }

    @Override
    public boolean isVehicleFollow() {
        return vehicleFollow;
    }
}

package org.mvplugins.multiverse.core.teleportation;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("5.1")
public interface PassengerMode {
    @ApiStatus.AvailableSince("5.1")
    boolean isDismountPassengers();

    @ApiStatus.AvailableSince("5.1")
    boolean isPassengersFollow();

    @ApiStatus.AvailableSince("5.1")
    boolean isDismountVehicle();

    @ApiStatus.AvailableSince("5.1")
    boolean isVehicleFollow();
}

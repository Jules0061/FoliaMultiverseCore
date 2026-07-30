package org.mvplugins.multiverse.core.destination;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.destination.core.WorldDestination;

public record DestinationSuggestionPacket(Destination<?, ?, ?> destination, String destinationString, String finerPermissionSuffix) {

    @ApiStatus.AvailableSince("5.1")
    public String parsableString() {
        return destination instanceof WorldDestination
                ? destinationString
                : destination.getIdentifier() + ":" + destinationString;
    }
}

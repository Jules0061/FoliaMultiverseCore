package org.mvplugins.multiverse.core.command.context;

import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("5.1")
public record PlayerLocation(Location value) {
}

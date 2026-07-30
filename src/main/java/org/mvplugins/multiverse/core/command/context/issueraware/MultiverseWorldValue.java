package org.mvplugins.multiverse.core.command.context.issueraware;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

@ApiStatus.AvailableSince("5.1")
public final class MultiverseWorldValue extends IssuerAwareValue {

    private final MultiverseWorld world;

    @ApiStatus.AvailableSince("5.1")
    public MultiverseWorldValue(boolean byIssuer, MultiverseWorld world) {
        super(byIssuer);
        this.world = world;
    }

    @ApiStatus.AvailableSince("5.1")
    public MultiverseWorld value() {
        return world;
    }
}

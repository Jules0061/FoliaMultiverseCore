package org.mvplugins.multiverse.core.command.context.issueraware;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("5.1")
public final class PlayerArrayValue extends IssuerAwareValue {

    private final Player[] players;

    @ApiStatus.AvailableSince("5.1")
    public PlayerArrayValue(boolean byIssuer, Player[] players) {
        super(byIssuer);
        this.players = players;
    }

    @ApiStatus.AvailableSince("5.1")
    public Player[] value() {
        return players;
    }
}

package org.mvplugins.multiverse.core.command.context.issueraware;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("5.1")
public abstract class IssuerAwareValue {
    protected final boolean byIssuer;

    @ApiStatus.AvailableSince("5.1")
    protected IssuerAwareValue(boolean byIssuer) {
        this.byIssuer = byIssuer;
    }

    @ApiStatus.AvailableSince("5.1")
    public boolean isByIssuer() {
        return byIssuer;
    }
}

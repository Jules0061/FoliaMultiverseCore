package org.mvplugins.multiverse.core.world;

import org.bukkit.PortalType;

public enum AllowedPortalType {
    NONE(PortalType.CUSTOM),

    ALL(PortalType.CUSTOM),

    NETHER(PortalType.NETHER),

    END(PortalType.ENDER);

    private final PortalType type;

    AllowedPortalType(PortalType type) {
        this.type = type;
    }

    public PortalType getActualPortalType() {
        return this.type;
    }

    public boolean isPortalAllowed(PortalType portalType) {
        return this != NONE && (getActualPortalType() == portalType || this == ALL);
    }
}

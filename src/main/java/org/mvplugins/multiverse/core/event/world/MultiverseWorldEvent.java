package org.mvplugins.multiverse.core.event.world;

import org.bukkit.event.Event;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

abstract class MultiverseWorldEvent<W extends MultiverseWorld> extends Event {
    protected final W world;

    MultiverseWorldEvent(W world) {
        this.world = world;
    }

    public W getWorld() {
        return world;
    }
}

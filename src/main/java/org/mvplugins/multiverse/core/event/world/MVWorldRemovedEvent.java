package org.mvplugins.multiverse.core.event.world;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

public final class MVWorldRemovedEvent extends MultiverseWorldEvent<MultiverseWorld> {
    private static final HandlerList HANDLERS = new HandlerList();

    public MVWorldRemovedEvent(MultiverseWorld world) {
        super(world);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

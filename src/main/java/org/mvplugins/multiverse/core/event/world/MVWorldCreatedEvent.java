package org.mvplugins.multiverse.core.event.world;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;

public final class MVWorldCreatedEvent extends MultiverseWorldEvent<LoadedMultiverseWorld> {
    private static final HandlerList HANDLERS = new HandlerList();

    public MVWorldCreatedEvent(LoadedMultiverseWorld world) {
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

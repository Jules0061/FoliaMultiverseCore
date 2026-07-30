package org.mvplugins.multiverse.core.event.world;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;

public final class MVWorldDeleteEvent extends MultiverseWorldEvent<LoadedMultiverseWorld> implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    public MVWorldDeleteEvent(@NotNull LoadedMultiverseWorld world) {
        super(world);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

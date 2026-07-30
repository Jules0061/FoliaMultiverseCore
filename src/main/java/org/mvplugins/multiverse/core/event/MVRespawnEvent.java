package org.mvplugins.multiverse.core.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public final class MVRespawnEvent extends PlayerEvent implements Cancellable {
    private Location location;
    private boolean cancelled = false;

    public MVRespawnEvent(Location spawningAt, Player player) {
        super(player);
        this.location = spawningAt;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Location getRespawnLocation() {
        return this.location;
    }

    public void setRespawnLocation(Location location) {
        this.location = location;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

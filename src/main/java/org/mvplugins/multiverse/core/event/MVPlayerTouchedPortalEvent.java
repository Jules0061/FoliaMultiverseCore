package org.mvplugins.multiverse.core.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MVPlayerTouchedPortalEvent extends Event implements Cancellable {
    private final Player player;
    private final Location location;
    private boolean isCancelled;
    private boolean canUse = true;

    public MVPlayerTouchedPortalEvent(Player p, Location l) {
        this.player = p;
        this.location = l;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Location getBlockTouched() {
        return this.location;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean canUseThisPortal() {
        return this.canUse;
    }

    public void setCanUseThisPortal(boolean canUse) {
        this.canUse = canUse;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
}

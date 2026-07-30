package org.mvplugins.multiverse.core.event;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;

public final class MVTeleportDestinationEvent extends Event implements Cancellable {
    private final Entity teleportee;
    private final CommandSender teleporter;
    private final DestinationInstance<?, ?> dest;
    private boolean isCancelled;

    public MVTeleportDestinationEvent(DestinationInstance<?, ?> dest, Entity teleportee, CommandSender teleporter) {
        this.teleportee = teleportee;
        this.teleporter = teleporter;
        this.dest = dest;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Entity getTeleportee() {
        return this.teleportee;
    }

    public Location getFrom() {
        return this.teleportee.getLocation();
    }

    public CommandSender getTeleporter() {
        return this.teleporter;
    }

    public DestinationInstance<?, ?> getDestination() {
        return this.dest;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}

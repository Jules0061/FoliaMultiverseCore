package org.mvplugins.multiverse.core.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MVDebugModeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int level;

    public MVDebugModeEvent(int level) {
        this.level = level;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public int getLevel() {
        return level;
    }
}

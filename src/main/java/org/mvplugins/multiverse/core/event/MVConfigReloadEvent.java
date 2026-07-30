package org.mvplugins.multiverse.core.event;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MVConfigReloadEvent extends Event {
    private final List<String> configsLoaded;

    public MVConfigReloadEvent(List<String> configsLoaded) {
        this.configsLoaded = configsLoaded;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public void addConfig(String config) {
        this.configsLoaded.add(config);
    }

    public List<String> getAllConfigsLoaded() {
        return this.configsLoaded;
    }
}

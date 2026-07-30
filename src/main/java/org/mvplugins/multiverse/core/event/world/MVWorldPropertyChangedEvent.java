package org.mvplugins.multiverse.core.event.world;

import org.bukkit.event.HandlerList;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

public final class MVWorldPropertyChangedEvent<T> extends MultiverseWorldEvent<MultiverseWorld> {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String name;
    private final T oldValue;
    private final T newValue;

    public MVWorldPropertyChangedEvent(@NotNull MultiverseWorld world, @NotNull String name, T oldValue, T value) {
        super(world);
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = value;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return this.newValue;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

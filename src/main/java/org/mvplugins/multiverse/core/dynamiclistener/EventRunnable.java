package org.mvplugins.multiverse.core.dynamiclistener;

import org.bukkit.event.Event;

@FunctionalInterface
public interface EventRunnable<T extends Event> {
    void onEvent(T event);
}

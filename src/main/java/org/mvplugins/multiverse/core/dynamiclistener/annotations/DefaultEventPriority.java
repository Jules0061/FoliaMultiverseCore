package org.mvplugins.multiverse.core.dynamiclistener.annotations;

import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.dynamiclistener.EventPriorityMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultEventPriority {
    @NotNull EventPriority value();
}

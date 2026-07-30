package org.mvplugins.multiverse.core.utils;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Option;
import jakarta.inject.Inject;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import java.lang.reflect.Field;

@Service
public final class WorldTickDeferrer {

    private final MVScheduler scheduler;

    private final Option<Object> console;
    private final Option<Field> isIteratingOverLevelsMethod;

    @Inject
    WorldTickDeferrer(@NotNull MVScheduler scheduler, @NotNull Server server) {
        this.scheduler = scheduler;
        this.console = ReflectHelper.tryGetMethod(server.getClass(), "getServer")
                .onFailure(throwable -> Logging.fine("Unable to find getServer method."))
                .flatMap(getServerMethod -> ReflectHelper.tryInvokeMethod(server, getServerMethod))
                .onFailure(throwable -> Logging.fine("Unable to find console."))
                .toOption();
        this.isIteratingOverLevelsMethod = console.toTry()
                .map(Object::getClass)
                .flatMap(consoleClazz -> ReflectHelper.tryGetField(consoleClazz, "isIteratingOverLevels"))
                .onFailure(throwable -> Logging.fine("Unable to find isIteratingOverLevels field."))
                .toOption();
    }

    public void deferWorldTick(Runnable action) {
        if (!isIteratingOverLevels()) {
            action.run();
            return;
        }
        Logging.fine("Deferring world tick...");
        scheduler.runGlobalLater(action, 1L);
    }

    private boolean isIteratingOverLevels() {
        return isIteratingOverLevelsMethod
                .flatMap(field -> console
                        .flatMap(c -> ReflectHelper.tryGetFieldValue(c, field, Boolean.class).toOption()))
                .getOrElse(false);
    }
}

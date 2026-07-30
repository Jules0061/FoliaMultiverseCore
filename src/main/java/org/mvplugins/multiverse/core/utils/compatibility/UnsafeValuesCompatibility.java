package org.mvplugins.multiverse.core.utils.compatibility;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.utils.ReflectHelper;

import java.lang.reflect.Method;

@ApiStatus.AvailableSince("5.7")
public final class UnsafeValuesCompatibility {

    private static final Try<Method> GET_MAIN_LEVEL_NAME_METHOD;

    static {
        GET_MAIN_LEVEL_NAME_METHOD = ReflectHelper.tryGetMethod(UnsafeValues.class, "getMainLevelName");
    }

    @ApiStatus.AvailableSince("5.7")
    public static Option<String> getMainLevelName() {
        return GET_MAIN_LEVEL_NAME_METHOD
                .flatMap(method -> ReflectHelper.tryInvokeMethod(Bukkit.getUnsafe(), method))
                .map(String.class::cast)
                .toOption();
    }

    private UnsafeValuesCompatibility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

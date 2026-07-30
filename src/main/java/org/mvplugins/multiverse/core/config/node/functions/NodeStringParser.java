package org.mvplugins.multiverse.core.config.node.functions;

import io.vavr.control.Try;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface NodeStringParser<T> {
    @NotNull Try<T> parse(@Nullable String string, @NotNull Class<T> type);
}

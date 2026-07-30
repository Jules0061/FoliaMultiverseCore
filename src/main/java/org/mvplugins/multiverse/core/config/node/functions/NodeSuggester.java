package org.mvplugins.multiverse.core.config.node.functions;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface NodeSuggester {
    @NotNull Collection<String> suggest(@Nullable String input);
}

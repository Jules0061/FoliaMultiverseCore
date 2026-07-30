package org.mvplugins.multiverse.core.world.generators;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GeneratorPlugin {
    @NotNull Collection<String> suggestIds(@Nullable String currentIdInput);

    @Nullable Collection<String> getExampleUsages();

    @Nullable String getInfoLink();

    @NotNull String getPluginName();
}

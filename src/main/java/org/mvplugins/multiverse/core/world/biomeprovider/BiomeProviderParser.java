package org.mvplugins.multiverse.core.world.biomeprovider;

import org.bukkit.generator.BiomeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface BiomeProviderParser {

    BiomeProvider parseBiomeProvider(@NotNull String worldName, @NotNull String params);

    Collection<String> suggestParams(@NotNull String currentInput);
}

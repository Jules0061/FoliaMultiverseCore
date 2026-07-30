package org.mvplugins.multiverse.core.world.biomeprovider;

import jakarta.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.generator.BiomeProvider;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.utils.REPatterns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public final class BiomeProviderFactory {

    private final Map<String, BiomeProviderParser> biomeProviderParsers;

    @Inject
    BiomeProviderFactory() {
        biomeProviderParsers = new HashMap<>();
        registerBiomeProviderParser("single", new SingleBiomeProviderParser());
    }

    public void registerBiomeProviderParser(@NotNull String key, @NotNull BiomeProviderParser biomeProviderParser) {
        biomeProviderParsers.put("@" + key, biomeProviderParser);
    }

    public BiomeProvider parseBiomeProvider(@NotNull String worldName, @NotNull String biomeProviderString) {
        if (biomeProviderString.isEmpty()) {
            return null;
        }
        if (biomeProviderString.startsWith("@")) {
            String[] split = REPatterns.COLON.split(biomeProviderString, 2);
            BiomeProviderParser biomeProviderParser = biomeProviderParsers.get(split[0]);
            if (biomeProviderParser != null) {
                return biomeProviderParser.parseBiomeProvider(worldName, split.length > 1 ? split[1] : "");
            }
        }
        return WorldCreator.getBiomeProviderForName(worldName, biomeProviderString, Bukkit.getConsoleSender());
    }

    public Collection<String> suggestBiomeString(@NotNull String currentInput) {
        String[] split = REPatterns.COLON.split(currentInput, 2);
        if (split.length < 2) {
            return biomeProviderParsers.keySet().stream()
                    .map(key -> currentInput.equals(key) ? key + ":" : key)
                    .toList();
        }
        BiomeProviderParser biomeProviderParser = biomeProviderParsers.get(split[0]);
        if (biomeProviderParser != null) {
            return biomeProviderParser.suggestParams(split[1]).stream().map(key -> split[0] + ":" + key).toList();
        }
        return Collections.emptyList();
    }
}

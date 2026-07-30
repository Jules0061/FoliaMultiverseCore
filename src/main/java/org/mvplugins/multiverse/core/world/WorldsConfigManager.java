package org.mvplugins.multiverse.core.world;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.inject.Inject;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;
import org.mvplugins.multiverse.core.world.key.WorldKeyParseFailReason;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
final class WorldsConfigManager {
    private static final String CONFIG_FILENAME = "worlds.yml";

    private final SortedMap<WorldKeyOrName, WorldConfig> worldConfigMap;
    private final File worldConfigFile;
    private YamlConfiguration worldsConfig;

    private final MultiverseCore multiverseCore;

    @Inject
    WorldsConfigManager(@NotNull MultiverseCore core, @NotNull MultiverseCore multiverseCore) {
        worldConfigMap = new TreeMap<>();
        worldConfigFile = core.getDataFolder().toPath().resolve(CONFIG_FILENAME).toFile();

        this.multiverseCore = multiverseCore;
    }

    Try<NewAndRemovedWorlds> load() {
        return Try.of(() -> {
            loadWorldYmlFile();
            return parseNewAndRemovedWorlds();
        }).onFailure(e -> {
            Logging.severe("Failed to load worlds.yml file: %s", e.getMessage());
        });
    }

    private void loadWorldYmlFile() throws IOException, InvalidConfigurationException {
        boolean exists = worldConfigFile.exists();
        if (!exists && !worldConfigFile.createNewFile()) {
            throw new IllegalStateException("Could not create worlds.yml config file");
        }
        if (exists) {
            migrateRemoveOldConfigSerializable();
        }
        worldsConfig = new YamlConfiguration();
        worldsConfig.load(worldConfigFile);
    }

    private void migrateRemoveOldConfigSerializable() {
        Try.of(() -> Files.readString(worldConfigFile.toPath()))
                .mapTry(configData -> {
                    if (!configData.contains("==: MVWorld")) {
                        throw new ConfigMigratedException();
                    }

                    Path oldWorldConfig = worldConfigFile.toPath().getParent()
                            .resolve(CONFIG_FILENAME + ".old." + System.currentTimeMillis());
                    Files.copy(worldConfigFile.toPath(), oldWorldConfig, COPY_ATTRIBUTES, REPLACE_EXISTING);

                    return configData.replace("==: MVWorld", "w@: world")
                            .replace("==: MVSpawnSettings", "")
                            .replace("==: MVSpawnSubSettings", "")
                            .replace("==: MVEntryFee", "");
                })
                .andThenTry(configData -> Files.writeString(worldConfigFile.toPath(), configData))
                .andThenTry(() -> {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(worldConfigFile);

                    ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
                    if (worldsSection == null) {
                        worldsSection = config.createSection("worlds");
                    }

                    List<String> worldNames = getOldConfigWorldNames(worldsSection);

                    Map<String, ConfigurationSection> worldConfigMap = new HashMap<>();
                    for (String worldName : worldNames) {
                        ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
                        if (worldSection != null) {
                            worldConfigMap.put(worldName, worldSection);
                        }
                    }

                    config.set("worlds", null);

                    for (Map.Entry<String, ConfigurationSection> entry : worldConfigMap.entrySet()) {
                        config.set(encodeConfigKey(entry.getKey()), entry.getValue());
                    }
                    config.save(worldConfigFile);
                })
                .onFailure(e -> {
                    if (e instanceof ConfigMigratedException) {
                        Logging.fine("Config already migrated");
                        return;
                    }
                    Logging.warning("Failed to migrate old worlds.yml file: %s", e.getMessage());
                    e.printStackTrace();
                });
    }

    private @NotNull List<String> getOldConfigWorldNames(ConfigurationSection worldsSection) {
        List<String> worldNames = new ArrayList<>();
        recursiveGetOldConfigWorldNames(worldsSection, worldNames);
        return worldNames;
    }

    private void recursiveGetOldConfigWorldNames(ConfigurationSection section, List<String> worldNames) {
        Set<String> keys = section.getKeys(false);
        if (keys.isEmpty()) {
            return;
        }

        if (keys.contains("w@")) {
            worldNames.add(section.getCurrentPath().substring(7));
            return;
        }

        for (String key : keys) {
            ConfigurationSection dataSection = section.getConfigurationSection(key);
            if (dataSection == null) {
                continue;
            }
            recursiveGetOldConfigWorldNames(dataSection, worldNames);
        }
    }

    private NewAndRemovedWorlds parseNewAndRemovedWorlds() {
        List<WorldKeyOrName> allWorldsInConfig = worldsConfig.getKeys(false)
                .stream()
                .map(keyStr -> decodeConfigKey(keyStr)
                        .onFailure(reason ->
                                Logging.warning("Failed to parse world key '%s' in config: %s", keyStr, reason))
                        .getOrNull())
                .filter(Objects::nonNull)
                .toList();

        List<WorldConfig> newWorldsAdded = new ArrayList<>();

        for (WorldKeyOrName worldKeyStr : allWorldsInConfig) {
            getWorldConfig(worldKeyStr)
                    .peek(config -> config.load(getWorldConfigSection(worldKeyStr))
                            .onFailure(e -> Logging.warning("Failed to load world config for world '%s': %s",
                                    worldKeyStr, e.getMessage())))
                    .orElse(() -> {
                        WorldConfig newWorldConfig = new WorldConfig(
                                worldKeyStr,
                                getWorldConfigSection(worldKeyStr),
                                multiverseCore);
                        worldConfigMap.put(worldKeyStr, newWorldConfig);
                        newWorldsAdded.add(newWorldConfig);
                        return Option.of(newWorldConfig);
                    })
                    .peek(WorldConfig::save);
        }

        List<WorldKeyOrName> worldsRemoved = worldConfigMap.keySet().stream()
                .filter(worldName -> !allWorldsInConfig.contains(worldName))
                .toList();

        for (WorldKeyOrName s : worldsRemoved) {
            worldConfigMap.remove(s);
        }

        return new NewAndRemovedWorlds(newWorldsAdded, worldsRemoved);
    }

    boolean isLoaded() {
        return worldsConfig != null;
    }

    Try<Void> save() {
        return Try.run(() -> {
            if (!isLoaded()) {
                throw new IllegalStateException("WorldsConfigManager is not loaded!");
            }
            worldsConfig = new YamlConfiguration();
            worldConfigMap.forEach((keyOrName, worldConfig) -> {
                worldConfig.save().onFailure(e -> {
                    throw new RuntimeException("Failed to save world " + keyOrName + " in config: " + e.getMessage(), e);
                });
                worldsConfig.set(encodeConfigKey(keyOrName), worldConfig.getConfigurationSection());
            });
            worldsConfig.save(worldConfigFile);
        }).onFailure(e -> {
            Logging.severe("Failed to save worlds.yml file: %s", e.getMessage());
        });
    }

    @NotNull Option<WorldConfig> getWorldConfig(@NotNull WorldKeyOrName keyOrName) {
        return Option.of(worldConfigMap.get(keyOrName));
    }

    @NotNull WorldConfig migrateWorldConfigKey(@NotNull WorldConfig worldConfig, @NotNull NamespacedKey toKey) {
        worldConfig.save();
        WorldKeyOrName newKeyOrName = WorldKeyOrName.parseKey(toKey);
        WorldConfig migratedWorldConfig = new WorldConfig(
                newKeyOrName,
                worldConfig.getConfigurationSection(),
                multiverseCore
        );
        deleteWorldConfig(worldConfig);
        worldConfigMap.put(newKeyOrName, migratedWorldConfig);
        return migratedWorldConfig;
    }

    @NotNull WorldConfig addWorldConfig(@NotNull NamespacedKey namespacedKey) {
        WorldKeyOrName keyOrName = WorldKeyOrName.parseKey(namespacedKey);
        if (worldConfigMap.containsKey(keyOrName)) {
            throw new IllegalStateException("WorldConfig for world " + namespacedKey + " already exists.");
        }
        WorldConfig worldConfig = new WorldConfig(keyOrName, getWorldConfigSection(keyOrName), multiverseCore);
        worldConfigMap.put(keyOrName, worldConfig);
        return worldConfig;
    }

    void deleteWorldConfig(@NotNull WorldConfig worldConfig) {
        worldConfigMap.remove(worldConfig.getWorldKeyOrName());
        worldsConfig.set(encodeConfigKey(worldConfig.getWorldKeyOrName()), null);
    }

    @NotNull
    private ConfigurationSection getWorldConfigSection(@NotNull WorldKeyOrName keyOrName) {
        String encodeWorldKey = encodeConfigKey(keyOrName);
        ConfigurationSection section = worldsConfig.getConfigurationSection(encodeWorldKey);
        return section == null ? worldsConfig.createSection(encodeWorldKey) : section;
    }

    private String encodeConfigKey(@NotNull WorldKeyOrName worldKeyOrName) {
        return encodeConfigKey(worldKeyOrName.serialise());
    }

    private String encodeConfigKey(@NotNull String worldName) {
        return worldName.replace(".", "[dot]");
    }

    private Attempt<WorldKeyOrName, WorldKeyParseFailReason> decodeConfigKey(@NotNull String worldKeyStr) {
        return WorldKeyOrName.parse(worldKeyStr.replace("[dot]", "."));
    }

    private static final class ConfigMigratedException extends RuntimeException {
        private ConfigMigratedException() {
            super("Config migrated");
        }
    }
}

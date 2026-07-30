package org.mvplugins.multiverse.core.world.generators;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.utils.FileUtils;
import org.mvplugins.multiverse.core.utils.REPatterns;

@Service
public final class GeneratorProvider implements Listener {
    private final Map<String, String> defaultGenerators;
    private final Map<String, GeneratorPlugin> generatorPlugins;
    private final CoreConfig coreConfig;
    private final FileUtils fileUtils;

    @Inject
    GeneratorProvider(@NotNull MultiverseCore plugin, @NotNull CoreConfig coreConfig, @NotNull FileUtils fileUtils) {
        this.coreConfig = coreConfig;
        this.fileUtils = fileUtils;
        defaultGenerators = new HashMap<>();
        generatorPlugins = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadDefaultWorldGenerators();
        loadPluginGenerators();
    }

    private void loadDefaultWorldGenerators() {
        File bukkitConfigFile = fileUtils.getBukkitConfig();
        if (bukkitConfigFile == null) {
            Logging.warning("Any default world generators will not be loaded!");
            return;
        }

        FileConfiguration bukkitConfig = YamlConfiguration.loadConfiguration(bukkitConfigFile);
        ConfigurationSection worldSection = bukkitConfig.getConfigurationSection("worlds");
        if (worldSection != null) {
            Set<String> keys = worldSection.getKeys(false);
            keys.forEach(key -> defaultGenerators.put(key, bukkitConfig.getString("worlds." + key + ".generator", "")));
        }
    }

    private void loadPluginGenerators() {
        if (!coreConfig.getAutoDetectGeneratorPlugins()) {
            return;
        }
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (testIsGeneratorPlugin(plugin)) {
                registerGeneratorPlugin(new SimpleGeneratorPlugin(plugin.getName()));
            }
        }
    }

    private boolean testIsGeneratorPlugin(Plugin plugin) {
        String worldName = Bukkit.getWorlds().stream().findFirst().map(World::getName).orElse("world");
        try {
            return plugin.getDefaultWorldGenerator(worldName, "") != null;
        } catch (UnsupportedOperationException e) {
            return false;
        } catch (Throwable t) {
            Logging.warning("Plugin %s threw an exception when testing if it is a generator plugin! ",
                    plugin.getName());
            Logging.warning("Error by plugin %s: %s", plugin.getName(), t.getMessage());
            Logging.warning("This is NOT a bug in Multiverse. Do NOT report this to Multiverse support. " +
                    "Enable debug mode with `/mv debug 1` to see the full stack trace.");
            if (coreConfig.getGlobalDebug() >= 1) {
                t.printStackTrace();
            }
            return true;
        }
    }

    public @Nullable String getDefaultGeneratorForWorld(String worldName) {
        return defaultGenerators.getOrDefault(worldName, null);
    }

    public boolean registerGeneratorPlugin(@NotNull GeneratorPlugin generatorPlugin) {
        var registeredGenerator = generatorPlugins.get(generatorPlugin.getPluginName());
        if (registeredGenerator == null || registeredGenerator instanceof SimpleGeneratorPlugin) {
            generatorPlugins.put(generatorPlugin.getPluginName(), generatorPlugin);
            Logging.finer("Registered generator plugin: %s", generatorPlugin.getPluginName());
            return true;
        }
        Logging.severe("Generator plugin with name %s is already registered!", generatorPlugin.getPluginName());
        return false;
    }

    @Nullable
    public String parseGeneratorString(@NotNull String worldName, @Nullable String generatorString) {
        return Strings.isNullOrEmpty(generatorString)
                ? getDefaultGeneratorForWorld(worldName)
                : generatorString;
    }

    public boolean unregisterGeneratorPlugin(@NotNull String pluginName) {
        if (generatorPlugins.containsKey(pluginName)) {
            generatorPlugins.remove(pluginName);
            return true;
        }
        Logging.severe("Generator plugin with name %s is not registered!", pluginName);
        return false;
    }

    public boolean isGeneratorPluginRegistered(@NotNull String pluginName) {
        return generatorPlugins.containsKey(pluginName);
    }

    public @Nullable GeneratorPlugin getGeneratorPlugin(@NotNull String pluginName) {
        return generatorPlugins.get(pluginName);
    }

    public Collection<String> suggestGeneratorString(@Nullable String currentInput) {
        String[] genSpilt = currentInput == null ? new String[0] : REPatterns.COLON.split(currentInput, 2);
        String generatorName = genSpilt[0];
        String generatorId = genSpilt.length > 1 ? genSpilt[1] : "";
        return generatorPlugins.entrySet().stream()
                .flatMap(entry -> {
                    var ids = entry.getValue().suggestIds(entry.getKey().equals(generatorName) ? generatorId : "");
                    if (ids.isEmpty()) {
                        return Stream.of(entry.getKey());
                    }
                    return ids.stream().map(id -> Strings.isNullOrEmpty(id)
                            ? entry.getKey()
                            : entry.getKey() + ":" + id);
                })
                .toList();
    }

    public Collection<GeneratorPlugin> getRegisteredGeneratorPlugins() {
        return generatorPlugins.values();
    }

    @EventHandler
    private void onPluginEnable(PluginEnableEvent event) {
        if (!coreConfig.getAutoDetectGeneratorPlugins()) {
            return;
        }
        if (!testIsGeneratorPlugin(event.getPlugin())) {
            Logging.finest("Plugin %s is not a generator plugin.", event.getPlugin().getName());
            return;
        }
        if (!registerGeneratorPlugin(new SimpleGeneratorPlugin(event.getPlugin().getName()))) {
            Logging.severe("Failed to register generator plugin %s!", event.getPlugin().getName());
        }
    }

    @EventHandler
    private void onPluginDisable(PluginDisableEvent event) {
        if (!isGeneratorPluginRegistered(event.getPlugin().getName())) {
            Logging.finest("Plugin %s is not a generator plugin.", event.getPlugin().getName());
            return;
        }
        if (!unregisterGeneratorPlugin(event.getPlugin().getName())) {
            Logging.severe("Failed to unregister generator plugin %s!", event.getPlugin().getName());
        }
    }
}

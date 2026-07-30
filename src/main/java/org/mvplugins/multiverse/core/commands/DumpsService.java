package org.mvplugins.multiverse.core.commands;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.module.MultiverseModulesRegistry;
import org.mvplugins.multiverse.core.commands.DumpsLogPoster.UploadType;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.event.MVDumpsDebugInfoEvent;
import org.mvplugins.multiverse.core.utils.FileUtils;
import org.mvplugins.multiverse.core.utils.MVScheduler;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.core.world.WorldManager;

@Service
final class DumpsService {

    private final MultiverseCore plugin;
    private final MVScheduler scheduler;
    private final WorldManager worldManager;
    private final FileUtils fileUtils;

    @Inject
    DumpsService(@NotNull MultiverseCore plugin,
                 @NotNull MVScheduler scheduler,
                 @NotNull WorldManager worldManager,
                 @NotNull FileUtils fileUtils) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.worldManager = worldManager;
        this.fileUtils = fileUtils;
    }

    void postLogs(MVCommandIssuer issuer, UploadType servicesType) {
        issuer.sendInfo(MVCorei18n.DUMPS_STARTING);

        MVDumpsDebugInfoEvent versionEvent = createAndCallDebugInfoEvent();
        scheduler.runAsync(new DumpsLogPoster(plugin, issuer, servicesType, getLogs(), versionEvent));
    }

    private @NotNull String getLogs() {
        Path logsPath = fileUtils.getServerFolder().toPath().resolve("logs/latest.log");
        File logsFile = logsPath.toFile();

        if (!logsFile.exists()) {
            Logging.warning("Could not read logs/latest.log");
            return "Could not find log";
        }

        return readLogsFromFile(logsPath);
    }

    private @NotNull String readLogsFromFile(Path logsPath) {
        String logs = "Could not read log";

        try {
            logs = Files.readString(logsPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logging.severe("Could not read logs/latest.log as UTF-8. Trying ISO-8859-1. See below for stack trace");
            e.printStackTrace();

            try {
                logs = Files.readString(logsPath, StandardCharsets.ISO_8859_1);
            } catch (IOException ex) {
                Logging.severe("Could not read ./logs/latest.log as ISO-8859-1. See below for stack trace");
                ex.printStackTrace();
            }
        }

        return logs;
    }

    private MVDumpsDebugInfoEvent createAndCallDebugInfoEvent() {
        MVDumpsDebugInfoEvent event = new MVDumpsDebugInfoEvent();
        addDebugInfoToEvent(event);
        plugin.getServer().getPluginManager().callEvent(event);
        return event;
    }

    private void addDebugInfoToEvent(MVDumpsDebugInfoEvent event) {
        event.putDetailedDebugInfo("version.md", this.getDebugInfoString());

        if (fileUtils.getServerProperties() != null) {
            event.putDetailedDebugInfo(fileUtils.getServerProperties().getName(), fileUtils.getServerProperties());
        } else {
            Logging.warning("/mv dumps could not find server.properties. Not including file");
        }

        if (fileUtils.getBukkitConfig() != null) {
            event.putDetailedDebugInfo(fileUtils.getBukkitConfig().getName(), fileUtils.getBukkitConfig());
        } else {
            Logging.warning("/mv dumps could not find bukkit.yml. Not including file");
        }

        File spigotYml = fileUtils.getServerFolder().toPath().resolve("spigot.yml").toFile();
        if (spigotYml.isFile()) {
            event.putDetailedDebugInfo(spigotYml.getName(), spigotYml);
        } else {
            Logging.warning("/mv dumps could not find spigot.yml. Not including file");
        }

        File paperGlobalYml = fileUtils.getServerFolder().toPath().resolve("config/paper-global.yml").toFile();
        if (paperGlobalYml.isFile()) {
            event.putDetailedDebugInfo(paperGlobalYml.getName(), paperGlobalYml);
        } else {
            Logging.warning("/mv dumps could not find paper-global.yml. Not including file");
        }

        File paperWorldDefaultsYml = fileUtils.getServerFolder().toPath().resolve("config/paper-world-defaults.yml").toFile();
        if (paperWorldDefaultsYml.isFile()) {
            event.putDetailedDebugInfo(paperWorldDefaultsYml.getName(), paperWorldDefaultsYml);
        } else {
            Logging.warning("/mv dumps could not find paper-global.yml. Not including file");
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        event.putDetailedDebugInfo("multiverse-core/config.yml", configFile);

        File worldsFile = new File(plugin.getDataFolder(), "worlds.yml");
        event.putDetailedDebugInfo("multiverse-core/worlds.yml", worldsFile);

        File anchorsFile = new File(plugin.getDataFolder(), "anchors.yml");
        event.putDetailedDebugInfo("multiverse-core/anchors.yml", anchorsFile);
    }

    private String getDebugInfoString() {
        return "# Multiverse-Core Version info" + "\n\n"
                + " - Multiverse-Core Version: " + this.plugin.getDescription().getVersion() + '\n'
                + " - Bukkit Version: " + this.plugin.getServer().getVersion() + '\n'
                + " - Loaded Worlds: " + worldManager.getLoadedWorlds() + '\n'
                + " - Multiverse Plugins Loaded: " + StringFormatter.joinAnd(MultiverseModulesRegistry.get().getRegisteredPlugins()) + '\n'
                + " - Multiverse Plugins Count: " + MultiverseModulesRegistry.get().getPluginCount() + '\n';
    }
}

package org.mvplugins.multiverse.core.anchor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.inject.Inject;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.utils.result.Result;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.entrycheck.WorldEntryCheckerProvider;
import org.mvplugins.multiverse.core.world.location.UnloadedWorldLocation;

@Service
public final class AnchorManager {

    private static final String ANCHORS_FILE = "anchors.yml";
    private static final String ANCHORS_CONFIG_SECTION = "anchors";

    private final Map<String, MultiverseAnchor> anchors;
    private FileConfiguration anchorConfig;

    private final Plugin plugin;
    private final LocationManipulation locationManipulation;
    private final WorldEntryCheckerProvider entryCheckerProvider;
    private final WorldManager worldManager;

    @Inject
    AnchorManager(
            MultiverseCore plugin,
            LocationManipulation locationManipulation,
            WorldEntryCheckerProvider entryCheckerProvider,
            WorldManager worldManager
    ) {
        this.plugin = plugin;
        this.locationManipulation = locationManipulation;
        this.entryCheckerProvider = entryCheckerProvider;
        this.worldManager = worldManager;

        this.anchors = new HashMap<>();
    }

    public Try<Void> loadAnchors() {
        anchors.clear();
        return Try.run(() -> {
            anchorConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), ANCHORS_FILE));
            parseAnchorsFromConfig();
        });
    }

    private void parseAnchorsFromConfig() {
        var anchorsSection = getAnchorsConfigSection();
        Set<String> anchorKeys = anchorsSection.getKeys(false);
        for (String key : anchorKeys) {
            Location anchorLocation = locationManipulation.stringToLocation(anchorsSection.getString(key, ""));
            if (anchorLocation != null) {
                Logging.config("Loading anchor:  '%s'...", key);
                anchors.put(key, new MultiverseAnchor(key, UnloadedWorldLocation.fromLocation(anchorLocation)));
            } else {
                Logging.warning("The location for anchor '%s' is INVALID.", key);
            }
        }
    }

    private ConfigurationSection getAnchorsConfigSection() {
        var anchorsConfigSection = anchorConfig.getConfigurationSection(ANCHORS_CONFIG_SECTION);
        if (anchorsConfigSection == null) {
            anchorsConfigSection = anchorConfig.createSection(ANCHORS_CONFIG_SECTION);
        }
        return anchorsConfigSection;
    }

    public Try<Void> saveAllAnchors() {
        return Try.run(() -> anchorConfig.save(new File(plugin.getDataFolder(), ANCHORS_FILE)))
                .onFailure(failure ->
                        Logging.severe("Failed to save anchors.yml. Please check your file permissions."));
    }

    public Option<MultiverseAnchor> getAnchor(String anchorName) {
        return Option.of(anchors.get(anchorName));
    }

    public Try<Void> setAnchor(@NotNull String anchorName, @NotNull String location) {
        Location parsed = locationManipulation.stringToLocation(location);
        if (parsed == null) {
            return Try.failure(new IllegalArgumentException("The location for anchor '" + anchorName + "' is INVALID."));
        }
        return setAnchor(anchorName, parsed);
    }

    public Try<Void> setAnchor(@NotNull String anchorName, @NotNull Location location) {
        Option.of(anchors.get(anchorName))
                .peek(anchor -> anchor.setLocation(UnloadedWorldLocation.fromLocation(location)))
                .onEmpty(() -> anchors.put(anchorName, new MultiverseAnchor(anchorName, UnloadedWorldLocation.fromLocation(location))));
        getAnchorsConfigSection().set(anchorName, locationManipulation.locationToString(location));
        return saveAllAnchors();
    }

    public List<MultiverseAnchor> getAllAnchors() {
        return anchors.values().stream().toList();
    }

    public List<MultiverseAnchor> getAnchors(@Nullable Player player) {
        if (player == null) {
            return getAllAnchors();
        } else {
            return getAnchorsForPlayer(player);
        }
    }

    private List<MultiverseAnchor> getAnchorsForPlayer(@NotNull Player player) {
        return anchors.values().stream()
                .filter(anchor -> shouldIncludeAnchorForPlayer(anchor, player))
                .toList();
    }

    private boolean shouldIncludeAnchorForPlayer(MultiverseAnchor anchor, Player player) {
        return worldManager.getWorld(anchor.getLocationWorld())
                .map(mvWorld -> entryCheckerProvider.forSender(player).canAccessWorld(mvWorld))
                .peek(result -> Logging.finer("Result for %s can access anchor %s: %s", player.getName(), anchor.getName(), result))
                .map(Result::isSuccess)
                .getOrElse(true);
    }

    public Try<Void> deleteAnchor(@NotNull MultiverseAnchor anchor) {
        if (anchors.containsKey(anchor.getName())) {
            anchors.remove(anchor.getName());
            getAnchorsConfigSection().set(anchor.getName(), null);
            return saveAllAnchors();
        }
        return Try.failure(new IllegalArgumentException("Anchor does not exist"));
    }
}

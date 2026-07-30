package org.mvplugins.multiverse.core.config.migration.action;

import java.util.Optional;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.configuration.ConfigurationSection;

public final class MoveMigratorAction implements MigratorAction {

    public static MoveMigratorAction of(String fromPath, String toPath) {
        return new MoveMigratorAction(fromPath, toPath);
    }

    private final String fromPath;
    private final String toPath;

    private MoveMigratorAction(String fromPath, String toPath) {
        this.fromPath = fromPath;
        this.toPath = toPath;
    }

    @Override
    public void migrate(ConfigurationSection config) {
        Optional.ofNullable(config.get(fromPath))
                .ifPresent(value -> {
                    config.set(fromPath, null);
                    config.set(toPath, value);
                    Logging.config("Moved path %s to %s", fromPath, toPath);
                });
    }
}

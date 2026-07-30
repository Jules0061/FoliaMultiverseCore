package org.mvplugins.multiverse.core.config.migration.action;

import org.bukkit.configuration.ConfigurationSection;

public interface MigratorAction {

    void migrate(ConfigurationSection config);
}

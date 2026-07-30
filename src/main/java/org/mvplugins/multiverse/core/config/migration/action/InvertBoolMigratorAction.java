package org.mvplugins.multiverse.core.config.migration.action;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.configuration.ConfigurationSection;

public final class InvertBoolMigratorAction implements MigratorAction {

    public static InvertBoolMigratorAction of(String path) {
        return new InvertBoolMigratorAction(path);
    }

    private final String path;

    private InvertBoolMigratorAction(String path) {
        this.path = path;
    }

    @Override
    public void migrate(ConfigurationSection config) {
        boolean boolValue = !config.getBoolean(path);
        config.set(path, boolValue);
        Logging.config("Inverted %s to boolean %s", path, boolValue);
    }
}

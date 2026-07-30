package org.mvplugins.multiverse.core.config.migration;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.config.migration.action.MigratorAction;

public class VersionMigrator implements Comparable<VersionMigrator> {

    public static Builder builder(double version) {
        return new Builder(version);
    }

    private final double version;
    private final List<MigratorAction> actions;

    protected VersionMigrator(double version, List<MigratorAction> actions) {
        this.version = version;
        this.actions = actions;
    }

    public void migrate(ConfigurationSection config) {
        actions.forEach(action -> action.migrate(config));
    }

    public double getVersion() {
        return version;
    }

    @Override
    public int compareTo(@NotNull VersionMigrator o) {
        return Double.compare(version, o.version);
    }

    public static class Builder {
        private final double version;
        private final List<MigratorAction> actions = new ArrayList<>();

        public Builder(double version) {
            this.version = version;
        }

        public Builder addAction(MigratorAction action) {
            actions.add(action);
            return this;
        }

        public VersionMigrator build() {
            return new VersionMigrator(version, actions);
        }
    }
}

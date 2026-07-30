package org.mvplugins.multiverse.core.config.migration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.configuration.ConfigurationSection;

import org.mvplugins.multiverse.core.config.node.ValueNode;

public class ConfigMigrator {

    public static Builder builder(ValueNode<Double> versionNode) {
        return new Builder(versionNode);
    }

    private final ValueNode<Double> versionNode;
    private final List<VersionMigrator> versionMigrators;

    protected ConfigMigrator(ValueNode<Double> versionNode, List<VersionMigrator> versionMigrators) {
        this.versionNode = versionNode;
        this.versionMigrators = versionMigrators;
    }

    public void migrate(ConfigurationSection config) {
        if (config.getKeys(false).isEmpty()) {
            config.set(versionNode.getPath(), getLatestVersion());
            return;
        }

        for (VersionMigrator versionMigrator : versionMigrators) {
            double versionNumber = config.getDouble(versionNode.getPath());
            if (versionNumber < versionMigrator.getVersion()) {
                Logging.info("Migrating config from version %s to %s...", versionNumber, versionMigrator.getVersion());
                versionMigrator.migrate(config);
                config.set(versionNode.getPath(), versionMigrator.getVersion());
            }
        }
    }

    private double getLatestVersion() {
        if (versionMigrators.isEmpty()) {
            return 0.0;
        }
        return versionMigrators.get(versionMigrators.size() - 1).getVersion();
    }

    public static class Builder {
        private final ValueNode<Double> versionNode;
        private final List<VersionMigrator> versionMigrators;

        public Builder(ValueNode<Double> versionNode) {
            this.versionNode = versionNode;
            this.versionMigrators = new ArrayList<>();
        }

        public Builder addVersionMigrator(VersionMigrator versionMigrator) {
            versionMigrators.add(versionMigrator);
            return this;
        }

        public ConfigMigrator build() {
            Collections.sort(versionMigrators);
            return new ConfigMigrator(versionNode, versionMigrators);
        }
    }
}

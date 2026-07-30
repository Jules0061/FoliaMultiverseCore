package org.mvplugins.multiverse.core.config.handle;

import java.util.logging.Logger;

import io.vavr.control.Try;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.node.NodeGroup;

public class ConfigurationSectionHandle<C extends ConfigurationSection> extends BaseConfigurationHandle<C> {
    public static <C extends ConfigurationSection> Builder<C, ? extends Builder<C, ?>> builder(
            @NotNull C configurationSection, @NotNull NodeGroup nodes) {
        return new Builder<>(configurationSection, nodes);
    }

    protected ConfigurationSectionHandle(
            @NotNull C configurationSection,
            @Nullable Logger logger,
            @NotNull NodeGroup nodes,
            @Nullable ConfigMigrator migrator) {
        super(logger, nodes, migrator);
        this.config = configurationSection;
    }

    public Try<Void> load(@NotNull C section) {
        this.config = section;
        return load();
    }

    public static class Builder<C extends ConfigurationSection, B extends Builder<C, B>> extends BaseConfigurationHandle.Builder<C, B> {
        protected final C configurationSection;

        protected Builder(@NotNull C configurationSection, @NotNull NodeGroup nodes) {
            super(nodes);
            this.configurationSection = configurationSection;
        }

        @Override
        public @NotNull ConfigurationSectionHandle<C> build() {
            return new ConfigurationSectionHandle<>(configurationSection, logger, nodes, migrator);
        }
    }
}

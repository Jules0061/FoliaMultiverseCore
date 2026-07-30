package org.mvplugins.multiverse.core.config.handle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.node.ListValueNode;
import org.mvplugins.multiverse.core.config.node.MapValueNode;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.core.config.node.ValueNode;

@SuppressWarnings("rawtypes,unchecked")
public abstract class BaseConfigurationHandle<C extends ConfigurationSection> {

    protected final @Nullable Logger logger;
    protected final @NotNull NodeGroup nodes;
    protected final @Nullable ConfigMigrator migrator;
    protected volatile @NotNull Map<ValueNode, Object> nodeValueMap;

    protected C config;

    protected BaseConfigurationHandle(
            @Nullable Logger logger,
            @NotNull NodeGroup nodes,
            @Nullable ConfigMigrator migrator) {
        this.logger = logger;
        this.nodes = nodes;
        this.migrator = migrator;
        this.nodeValueMap = new HashMap<>(nodes.size());
    }

    public Try<Void> load() {
        return Try.run(() -> {
            migrateConfig();
            setUpNodes();
        }).onFailure(e -> {
            Logging.severe("Failed to load configuration: %s", e.getMessage());
        });
    }

    protected void migrateConfig() {
        if (migrator != null) {
            migrator.migrate(config);
        }
    }

    protected void setUpNodes() {
        if (nodes.isEmpty()) {
            nodeValueMap = new HashMap<>();
            return;
        }

        Map<ValueNode, Object> newValueMap = new HashMap<>(nodes.size());
        nodes.forEach(node -> {
            if (node instanceof ValueNode valueNode) {
                var value = deserializeNodeFromConfig(valueNode);
                newValueMap.put(valueNode, value);
            }
        });
        nodeValueMap = newValueMap;

        newValueMap.forEach((valueNode, value) -> {
            valueNode.onLoad(value);
            valueNode.onLoadAndChange(Bukkit.getConsoleSender(), null, value);
        });
    }

    protected <T> T deserializeNodeFromConfig(ValueNode<T> node) {
        return Try.of(() -> {
                    var value = config.get(node.getPath());
                    return value == null ? node.getDefaultValue() : node.deserialize(value);
                })
                .flatMap(value -> node.validate(value).map(ignore -> value))
                .onFailure(e -> Logging.warning("Failed to deserialize node %s: %s",
                        node.getPath(), e.getMessage()))
                .getOrElse(node::getDefaultValue);
    }

    public Try<Void> save() {
        return Try.run(() -> nodes.forEach(node -> {
            if (!(node instanceof ValueNode valueNode)) {
                return;
            }
            serializeNodeToConfig(valueNode);
        }));
    }

    protected void serializeNodeToConfig(ValueNode node) {
        var value = nodeValueMap.get(node);
        if (value == null) {
            value = node.getDefaultValue();
        }
        config.set(node.getPath(), node.serialize(value));
    }

    public boolean isLoaded() {
        return !nodeValueMap.isEmpty();
    }

    public <T> T get(@NotNull ValueNode<T> node) {
        return (T) nodeValueMap.get(node);
    }

    public <T> Try<Void> set(@NotNull ValueNode<T> node, T value) {
        return set(Bukkit.getConsoleSender(), node, value);
    }

    public <K, V> Try<Void> set(@NotNull MapValueNode<K, V> node, K key, V value) {
        return node.validateEntry(key, value).map(ignore -> {
            Map<K,V> map = get(node);
            map.put(key, value);
            return null;
        });
    }

    @ApiStatus.AvailableSince("5.4")
    public <T> Try<Void> set(@NotNull CommandSender sender, @NotNull ValueNode<T> node, T value) {
        return node.validate(value).map(ignore -> {
            T oldValue = get(node);
            Map<ValueNode, Object> newValueMap = new HashMap<>(nodeValueMap);
            newValueMap.put(node, value);
            nodeValueMap = newValueMap;
            node.onLoadAndChange(sender, oldValue, value);
            node.onChange(sender, oldValue, value);
            return null;
        });
    }

    public <I> Try<Void> add(@NotNull ListValueNode<I> node, I itemValue) {
        return node.validateItem(itemValue).map(ignore -> {
            List<I> list = get(node);
            list.add(itemValue);
            node.onSetItemValue(null, itemValue);
            return null;
        });
    }

    public <I> Try<Void> remove(@NotNull ListValueNode<I> node, I itemValue) {
        return node.validateItem(itemValue).map(ignore -> {
            List<I> list = get(node);
            if (!list.remove(itemValue)) {
                throw new IllegalArgumentException("Cannot remove item as it is already not in the list!");
            }
            node.onSetItemValue(itemValue, null);
            return null;
        });
    }

    public <K, V> Try<Void> remove(@NotNull MapValueNode<K, V> node, K key) {
        return node.validateKey(key).map(ignore -> {
            Map<K,V> map = get(node);
            V value = map.remove(key);
            if (value == null) {
                throw new IllegalArgumentException("Cannot remove entry as it is already not in the map!");
            }
            return null;
        });
    }

    public <T> Try<Void> reset(@NotNull ValueNode<T> node) {
        return set(node, node.getDefaultValue());
    }

    public C getConfig() {
        return config;
    }

    @NotNull NodeGroup getNodes() {
        return nodes;
    }

    public abstract static class Builder<C extends ConfigurationSection, B extends BaseConfigurationHandle.Builder<C, B>> {

        protected final @NotNull NodeGroup nodes;
        protected @Nullable Logger logger;
        protected @Nullable ConfigMigrator migrator;

        protected Builder(@NotNull NodeGroup nodes) {
            this.nodes = nodes;
        }

        public B logger(@Nullable Logger logger) {
            this.logger = logger;
            return self();
        }

        public B logger(Plugin plugin) {
            this.logger = plugin.getLogger();
            return self();
        }

        public B migrator(@Nullable ConfigMigrator migrator) {
            this.migrator = migrator;
            return self();
        }

        public abstract @NotNull BaseConfigurationHandle<C> build();

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }
    }
}

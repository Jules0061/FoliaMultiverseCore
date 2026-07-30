package org.mvplugins.multiverse.core.inject.binder;

import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.jetbrains.annotations.NotNull;

public abstract class PluginBinder<T extends Plugin> extends AbstractBinder {

    private final T plugin;

    protected PluginBinder(@NotNull T plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public T getPlugin() {
        return plugin;
    }

    @Override
    protected final void configure() {
        var bindingBuilder = bindPlugin(getPlugin());
        bindingBuilder.to(Plugin.class);
        bindPluginClass(bindingBuilder);
        bind(plugin.getLogger()).to(Logger.class);
    }

    private ScopedBindingBuilder<T> bindPlugin(T plugin) {
        return bind(plugin);
    }

    protected abstract ScopedBindingBuilder<T> bindPluginClass(ScopedBindingBuilder<T> bindingBuilder);
}

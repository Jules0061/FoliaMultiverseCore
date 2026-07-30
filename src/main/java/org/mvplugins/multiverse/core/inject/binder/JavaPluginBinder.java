package org.mvplugins.multiverse.core.inject.binder;

import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.jetbrains.annotations.NotNull;

public abstract class JavaPluginBinder<T extends JavaPlugin> extends PluginBinder<T> {

    protected JavaPluginBinder(@NotNull T plugin) {
        super(plugin);
    }

    @Override
    protected ScopedBindingBuilder<T> bindPluginClass(ScopedBindingBuilder<T> bindingBuilder) {
        return bindingBuilder.to(JavaPlugin.class);
    }
}

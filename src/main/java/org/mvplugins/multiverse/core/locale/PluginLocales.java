package org.mvplugins.multiverse.core.locale;

import co.aikar.commands.BukkitLocales;
import jakarta.inject.Inject;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandManager;

@Service
public final class PluginLocales extends BukkitLocales {

    private static final String DEFAULT_LOCALE_FOLDER_PATH = "locales";

    @Inject
    public PluginLocales(MVCommandManager manager) {
        super(manager);
    }

    public boolean addFileResClassLoader(@NotNull Plugin plugin) {
        return this.addBundleClassLoader(new FileResClassLoader(plugin, DEFAULT_LOCALE_FOLDER_PATH));
    }

    public boolean addFileResClassLoader(@NotNull Plugin plugin, @NotNull String localesFolderPath) {
        return this.addBundleClassLoader(new FileResClassLoader(plugin, localesFolderPath));
    }
}

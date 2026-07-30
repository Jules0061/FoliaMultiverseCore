package org.mvplugins.multiverse.core.economy;

import com.dumptruckman.minecraft.util.Logging;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.locale.MVCorei18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

final class VaultHandler implements Listener {

    private Economy economy;
    private final MVCommandManager commandManager;

    VaultHandler(final Plugin plugin, @NotNull MVCommandManager commandManager) {
        this.commandManager = commandManager;
        Bukkit.getPluginManager().registerEvents(new VaultListener(), plugin);
        setupVaultEconomy();
    }

    private boolean setupVaultEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            final RegisteredServiceProvider<Economy> economyProvider = Bukkit
                    .getServicesManager()
                    .getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                Logging.fine("Vault economy enabled.");
                economy = economyProvider.getProvider();
            } else {
                Logging.finer("Vault economy not detected.");
                economy = null;
            }
        } else {
            Logging.finer("Vault was not found.");
            economy = null;
        }

        return economy != null;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    void showReceipt(Player player, double price) {
        if (price > 0D) {
            commandManager.getCommandIssuer(player).sendInfo(MVCorei18n.ECONOMY_VAULT_WITHDRAW,
                    replace("{price}").with(economy.format(price)));
        } else if (price < 0D) {
            commandManager.getCommandIssuer(player).sendInfo(MVCorei18n.ECONOMY_VAULT_DEPOSIT,
                    replace("{price}").with(economy.format(price)));
        }
    }

    private final class VaultListener implements Listener {
        @EventHandler
        private void vaultEnabled(PluginEnableEvent event) {
            if (event.getPlugin().getName().equals("Vault")) {
                setupVaultEconomy();
            }
        }

        @EventHandler
        private void vaultDisabled(PluginDisableEvent event) {
            if (event.getPlugin().getName().equals("Vault")) {
                Logging.fine("Vault economy disabled");
                economy = null;
            }
        }
    }
}

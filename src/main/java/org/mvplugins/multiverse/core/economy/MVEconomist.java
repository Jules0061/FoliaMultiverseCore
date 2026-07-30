package org.mvplugins.multiverse.core.economy;

import jakarta.inject.Inject;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

@Service
public final class MVEconomist {
    public static final String VAULT_ECONOMY_CODE = "@vault-economy";
    public static final Material VAULT_ECONOMY_MATERIAL = Material.AIR;

    private final VaultHandler vaultHandler;
    private final ItemEconomy itemEconomy;

    @Inject
    MVEconomist(MultiverseCore plugin, ItemEconomy itemEconomy, MVCommandManager commandManager) {
        vaultHandler = new VaultHandler(plugin, commandManager);
        this.itemEconomy = itemEconomy;
    }

    private boolean isUsingVault(Material currency) {
        return !isItemCurrency(currency) && getVaultHandler().hasEconomy();
    }

    public boolean isUsingEconomyPlugin() {
        return getVaultHandler().hasEconomy();
    }

    public String formatPrice(double amount, @Nullable Material currency) {
        if (isUsingVault(currency)) {
            return getVaultHandler().getEconomy().format(amount);
        } else {
            return itemEconomy.getFormattedPrice(amount, currency);
        }
    }

    public String getEconomyName() {
        if (getVaultHandler().hasEconomy()) {
            return getVaultHandler().getEconomy().getName();
        } else {
            return itemEconomy.getName();
        }
    }

    public boolean isPlayerWealthyEnough(Player player, double amount, Material currency) {
        if (amount <= 0D) {
            return true;
        } else if (isUsingVault(currency)) {
            return getVaultHandler().getEconomy().has(player, amount);
        } else {
            return itemEconomy.hasEnough(player, amount, currency);
        }
    }

    public String getNSFMessage(Material currency, String message) {
        return "Sorry, you don't have enough " + (isItemCurrency(currency) ? "items" : "funds") + ". " + message;
    }

    public void payEntryFee(Player player, MultiverseWorld world) {
        payEntryFee(player, world.getPrice(), world.getCurrency());
    }

    public void payEntryFee(Player player, double price, Material currency) {
        if (price == 0D) {
            return;
        }

        if (price < 0) {
            this.deposit(player, -price, currency);
        } else {
            this.withdraw(player, price, currency);
        }
    }

    public void deposit(Player player, double amount, @Nullable Material currency) {
        if (isUsingVault(currency)) {
            getVaultHandler().getEconomy().depositPlayer(player, amount);
            getVaultHandler().showReceipt(player, amount);
        } else {
            itemEconomy.deposit(player, amount, currency);
        }
    }

    public void withdraw(Player player, double amount, @Nullable Material currency) {
        if (isUsingVault(currency)) {
            getVaultHandler().getEconomy().withdrawPlayer(player, amount);
            getVaultHandler().showReceipt(player, amount);
        } else {
            itemEconomy.withdraw(player, amount, currency);
        }
    }

    public double getBalance(Player player) throws IllegalStateException {
        return getBalance(player, null);
    }

    public double getBalance(Player player, World world) throws IllegalStateException {
        if (!isUsingEconomyPlugin()) {
            throw new IllegalStateException("getBalance is only available when using an economy plugin with Vault");
        }
        if (world != null) {
            return getVaultHandler().getEconomy().getBalance(player, world.getName());
        } else {
            return getVaultHandler().getEconomy().getBalance(player);
        }
    }

    public void setBalance(Player player, double amount) throws IllegalStateException {
        setBalance(player, null, amount);
    }

    public void setBalance(Player player, World world, double amount) throws IllegalStateException {
        if (!isUsingEconomyPlugin()) {
            throw new IllegalStateException("getBalance is only available when using an economy plugin with Vault");
        }
        if (world != null) {
            getVaultHandler().getEconomy().withdrawPlayer(player, world.getName(), getBalance(player, world));
            getVaultHandler().getEconomy().depositPlayer(player, world.getName(), amount);
        } else {
            getVaultHandler().getEconomy().withdrawPlayer(player, getBalance(player));
            getVaultHandler().getEconomy().depositPlayer(player, amount);
        }
    }

    private VaultHandler getVaultHandler() {
        return vaultHandler;
    }

    public static boolean isItemCurrency(Material currency) {
        return currency != VAULT_ECONOMY_MATERIAL;
    }

}

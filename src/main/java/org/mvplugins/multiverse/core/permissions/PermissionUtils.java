package org.mvplugins.multiverse.core.permissions;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

public final class PermissionUtils {

    private static boolean debugPermissions = false;

    private PermissionUtils() {
    }

    public static boolean isDebugPermissions() {
        return debugPermissions;
    }

    public static void setDebugPermissions(boolean debugPermissions) {
        PermissionUtils.debugPermissions = debugPermissions;
    }

    @ApiStatus.AvailableSince("5.4")
    public static void registerPermissionWithWildcards(Permission permission) {
        Bukkit.getServer().getPluginManager().addPermission(permission);
        String[] split = permission.getName().split("\\.");
        StringBuilder prefix = new StringBuilder();
        Arrays.stream(Arrays.copyOfRange(split, 0, split.length - 1)).forEach(s -> {
            prefix.append(s).append(".");
            Permission perm = getOrAddPermission(prefix + "*");
            permission.addParent(perm, true);
        });
    }

    private static Permission getOrAddPermission(String permission) {
        Permission perm = Bukkit.getServer().getPluginManager().getPermission(permission);
        if (perm == null) {
            perm = new Permission(permission, PermissionDefault.FALSE);
            Bukkit.getServer().getPluginManager().addPermission(perm);
        }
        return perm;
    }

    public static String concatPermission(String permission, String... child) {
        return permission + "." + String.join(".", child);
    }

    public static boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            if (debugPermissions && !(sender instanceof ConsoleCommandSender)) {
                Logging.finer("Checking sender [%s] has permission [%s] : YES", sender.getName(), permission);
            }
            return true;
        }
        if (debugPermissions && !(sender instanceof ConsoleCommandSender)) {
            Logging.finer("Checking sender [%s] has permission [%s] : NO", sender.getName(), permission);
        }
        return false;
    }
}

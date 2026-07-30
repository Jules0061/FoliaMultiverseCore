package org.mvplugins.multiverse.core.command;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandPermission;
import io.vavr.control.Option;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.permissions.CorePermissionsChecker;
import org.mvplugins.multiverse.core.permissions.PermissionUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import static org.mvplugins.multiverse.core.permissions.CorePermissionsChecker.*;

@Service
public class MVCommandPermissions {
    private final Map<String, Predicate<CommandIssuer>> permissionsCheckMap;

    @Inject
    MVCommandPermissions(@NotNull CorePermissionsChecker permissionsChecker) {
        this.permissionsCheckMap = new HashMap<>();

        registerPermissionChecker("mvteleport", issuer -> permissionsChecker.hasAnyTeleportPermission(issuer.getIssuer()));
        registerPermissionChecker("mvteleportother", issuer -> permissionsChecker.hasAnyTeleportPermission(issuer.getIssuer(), Scope.OTHER));
        registerPermissionChecker("mvspawn", issuer -> permissionsChecker.hasAnySpawnPermission(issuer.getIssuer()));
        registerPermissionChecker("mvspawnother", issuer -> permissionsChecker.hasAnySpawnPermission(issuer.getIssuer(), Scope.OTHER));
    }

    public void registerPermissionChecker(String id, Predicate<CommandIssuer> checker) {
        permissionsCheckMap.put(prepareId(id), checker);
    }

    private static @NotNull String prepareId(String id) {
        return (id.startsWith("@") ? "" : "@") + id.toLowerCase(Locale.ENGLISH);
    }

    boolean hasPermission(CommandIssuer issuer, String permission) {
        return Option.of(permissionsCheckMap.get(permission))
                .map(checker -> checker.test(issuer))
                .getOrElse(() -> PermissionUtils.hasPermission(issuer.getIssuer(), permission));
    }
}

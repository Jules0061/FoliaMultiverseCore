package org.mvplugins.multiverse.core.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import jakarta.inject.Inject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.command.queue.ConfirmMode;
import org.mvplugins.multiverse.core.config.handle.CommentedConfigurationHandle;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.config.migration.action.BooleanMigratorAction;
import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.migration.action.IntegerMigratorAction;
import org.mvplugins.multiverse.core.config.migration.action.InvertBoolMigratorAction;
import org.mvplugins.multiverse.core.config.migration.action.MoveMigratorAction;
import org.mvplugins.multiverse.core.config.migration.VersionMigrator;
import org.mvplugins.multiverse.core.config.migration.action.SetMigratorAction;
import org.mvplugins.multiverse.core.teleportation.PassengerMode;
import org.mvplugins.multiverse.core.teleportation.PassengerModes;
import org.mvplugins.multiverse.core.world.helpers.DimensionFinder.DimensionFormat;

@Service
public final class CoreConfig {
    public static final String CONFIG_FILENAME = "config.yml";

    private final Path configPath;
    private final CoreConfigNodes configNodes;
    private final CommentedConfigurationHandle configHandle;
    private final StringPropertyHandle stringPropertyHandle;

    @Inject
    CoreConfig(
            @NotNull MultiverseCore core,
            @NotNull CoreConfigNodes configNodes
    ) {
        this.configPath = Path.of(core.getDataFolder().getPath(), CONFIG_FILENAME);
        this.configNodes = configNodes;
        this.configHandle = CommentedConfigurationHandle.builder(configPath, configNodes.getNodes())
                .logger(Logging.getLogger())
                .migrator(ConfigMigrator.builder(configNodes.version)
                        .addVersionMigrator(VersionMigrator.builder(5.0)
                                .addAction(MoveMigratorAction.of("multiverse-configuration.enforceaccess", "world.enforce-access"))
                                .addAction(BooleanMigratorAction.of("world.enforce-access"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.prefixchat", "messaging.enable-chat-prefix"))
                                .addAction(BooleanMigratorAction.of("messaging.enable-chat-prefix"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.prefixchatformat", "messaging.chat-prefix-format"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.teleportintercept", "world.teleport-intercept"))
                                .addAction(BooleanMigratorAction.of("world.teleport-intercept"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.firstspawnoverride", "spawn.first-spawn-override"))
                                .addAction(BooleanMigratorAction.of("spawn.first-spawn-override"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.displaypermerrors", "misc.debug-permissions"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.globaldebug", "misc.global-debug"))
                                .addAction(IntegerMigratorAction.of("misc.global-debug"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.silentstart", "misc.silent-start"))
                                .addAction(BooleanMigratorAction.of("misc.silent-start"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.firstspawnworld", "spawn.first-spawn-location"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.defaultportalsearch", "portal.use-custom-portal-search"))
                                .addAction(BooleanMigratorAction.of("portal.use-custom-portal-search"))
                                .addAction(InvertBoolMigratorAction.of("portal.use-custom-portal-search"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.portalsearchradius", "portal.custom-portal-search-radius"))
                                .addAction(IntegerMigratorAction.of("portal.custom-portal-search-radius"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.autopurge", "world.auto-purge-entities"))
                                .addAction(BooleanMigratorAction.of("world.auto-purge-entities"))
                                .addAction(MoveMigratorAction.of("multiverse-configuration.idonotwanttodonate", "misc.show-donation-message"))
                                .addAction(BooleanMigratorAction.of("misc.show-donation-message"))
                                .addAction(InvertBoolMigratorAction.of("misc.show-donation-message"))
                                .addAction(SetMigratorAction.of("command.show-legacy-aliases", true))
                                .build())
                        .addVersionMigrator(VersionMigrator.builder(5.1)
                                .addAction(MoveMigratorAction.of("world.teleport-intercept", "teleport.teleport-intercept"))
                                .addAction(MoveMigratorAction.of("world.resolve-alias-name", "command.resolve-alias-name"))
                                .build())
                        .addVersionMigrator(VersionMigrator.builder(5.2)
                                .addAction(MoveMigratorAction.of("spawn.default-respawn-to-world-spawn", "world.enforce-respawn-at-world-spawn"))
                                .build())
                        .build())
                .build();
        this.stringPropertyHandle = new StringPropertyHandle(configHandle);
    }

    private void migrateFromOldConfigFile() {
        String content;
        try {
            content = Files.readString(configPath);
        } catch (IOException e) {
            return;
        }
        content = content.replace("==: com.onarandombox.MultiverseCore.MultiverseCoreConfiguration", "");
        try {
            Files.writeString(configPath, content);
        } catch (IOException e) {
        }
    }

    public Try<Void> load() {
        return Try.run(this::migrateFromOldConfigFile)
                .flatMap(ignore -> configHandle.load())
                .onFailure(e -> {
                    Logging.severe("Failed to load Multiverse-Core config.yml!");
                    e.printStackTrace();
                });
    }

    public boolean isLoaded() {
        return configHandle.isLoaded();
    }

    public Try<Void> save() {
        return configHandle.save();
    }

    public StringPropertyHandle getStringPropertyHandle() {
        return stringPropertyHandle;
    }

    public Try<Void> setAutoImportDefaultWorlds(boolean autoImportDefaultWorlds) {
        return configHandle.set(configNodes.autoImportDefaultWorlds, autoImportDefaultWorlds);
    }

    public boolean getAutoImportDefaultWorlds() {
        return configHandle.get(configNodes.autoImportDefaultWorlds);
    }

    public Try<Void> setAutoImport3rdPartyWorlds(boolean autoImport3rdPartyWorlds) {
        return configHandle.set(configNodes.autoImport3rdPartyWorlds, autoImport3rdPartyWorlds);
    }

    public boolean getAutoImport3rdPartyWorlds() {
        return configHandle.get(configNodes.autoImport3rdPartyWorlds);
    }

    public Try<Void> setEnforceAccess(boolean enforceAccess) {
        return configHandle.set(configNodes.enforceAccess, enforceAccess);
    }

    public boolean getEnforceAccess() {
        return configHandle.get(configNodes.enforceAccess);
    }

    public Try<Void> setEnforceGameMode(boolean enforceGameMode) {
        return configHandle.set(configNodes.enforceGamemode, enforceGameMode);
    }

    public boolean getEnforceGameMode() {
        return configHandle.get(configNodes.enforceGamemode);
    }

    public Try<Void> setEnforceFlight(boolean enforceFlight) {
        return configHandle.set(configNodes.enforceFlight, enforceFlight);
    }

    public boolean getEnforceFlight() {
        return configHandle.get(configNodes.enforceFlight);
    }

    @ApiStatus.AvailableSince("5.5")
    public Try<Void> setGamemodeAndFlightEnforceDelay(int delayTicks) {
        return configHandle.set(configNodes.gamemodeAndFlightEnforceDelay, delayTicks);
    }

    @ApiStatus.AvailableSince("5.5")
    public int getGamemodeAndFlightEnforceDelay() {
        return configHandle.get(configNodes.gamemodeAndFlightEnforceDelay);
    }

    @ApiStatus.AvailableSince("5.3")
    public Try<Void> setApplyEntitySpawnRate(boolean applyEntitySpawnRate) {
        return configHandle.set(configNodes.applyEntitySpawnRate, applyEntitySpawnRate);
    }

    @ApiStatus.AvailableSince("5.3")
    public boolean getApplyEntitySpawnRate() {
        return configHandle.get(configNodes.applyEntitySpawnRate);
    }

    @ApiStatus.AvailableSince("5.3")
    public Try<Void> setApplyEntitySpawnLimit(boolean applyEntitySpawnLimit) {
        return configHandle.set(configNodes.applyEntitySpawnLimit, applyEntitySpawnLimit);
    }

    @ApiStatus.AvailableSince("5.3")
    public boolean getApplyEntitySpawnLimit() {
        return configHandle.get(configNodes.applyEntitySpawnLimit);
    }

    public Try<Void> setAutoPurgeEntities(boolean autopurge) {
        return configHandle.set(configNodes.autoPurgeEntities, autopurge);
    }

    public boolean isAutoPurgeEntities() {
        return configHandle.get(configNodes.autoPurgeEntities);
    }

    public Try<Void> setNetherWorldNameFormat(DimensionFormat netherWorldNameFormat) {
        return configHandle.set(configNodes.netherWorldNameFormat, netherWorldNameFormat);
    }

    public DimensionFormat getNetherWorldNameFormat() {
        return configHandle.get(configNodes.netherWorldNameFormat);
    }

    public Try<Void> setEndWorldNameFormat(DimensionFormat endWorldNameFormat) {
        return configHandle.set(configNodes.endWorldNameFormat, endWorldNameFormat);
    }

    public DimensionFormat getEndWorldNameFormat() {
        return configHandle.get(configNodes.endWorldNameFormat);
    }

    public Try<Void> setUseFinerTeleportPermissions(boolean useFinerTeleportPermissions) {
        return configHandle.set(configNodes.useFinerTeleportPermissions, useFinerTeleportPermissions);
    }

    public boolean getUseFinerTeleportPermissions() {
        return configHandle.get(configNodes.useFinerTeleportPermissions);
    }

    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setPassengerMode(PassengerModes passengerMode) {
        return configHandle.set(configNodes.passengerMode, passengerMode);
    }

    @ApiStatus.AvailableSince("5.1")
    public PassengerMode getPassengerMode() {
        return configHandle.get(configNodes.passengerMode);
    }

    public Try<Void> setConcurrentTeleportLimit(int concurrentTeleportLimit) {
        return configHandle.set(configNodes.concurrentTeleportLimit, concurrentTeleportLimit);
    }

    public int getConcurrentTeleportLimit() {
        return configHandle.get(configNodes.concurrentTeleportLimit);
    }

    public Try<Void> setTeleportIntercept(boolean teleportIntercept) {
        return configHandle.set(configNodes.teleportIntercept, teleportIntercept);
    }

    public boolean getTeleportIntercept() {
        return configHandle.get(configNodes.teleportIntercept);
    }

    public Try<Void> setFirstSpawnOverride(boolean firstSpawnOverride) {
        return configHandle.set(configNodes.firstSpawnOverride, firstSpawnOverride);
    }

    public Try<Void> setSafeLocationHorizontalSearchRadius(int searchRadius) {
        return configHandle.set(configNodes.safeLocationHorizontalSearchRadius, searchRadius);
    }

    public int getSafeLocationHorizontalSearchRadius() {
        return configHandle.get(configNodes.safeLocationHorizontalSearchRadius);
    }

    public Try<Void> setSafeLocationVerticalSearchRadius(int searchRadius) {
        return configHandle.set(configNodes.safeLocationVerticalSearchRadius, searchRadius);
    }

    public int getSafeLocationVerticalSearchRadius() {
        return configHandle.get(configNodes.safeLocationVerticalSearchRadius);
    }

    public boolean getFirstSpawnOverride() {
        return configHandle.get(configNodes.firstSpawnOverride);
    }

    public Try<Void> setFirstSpawnLocation(String firstSpawnWorld) {
        return configHandle.set(configNodes.firstSpawnLocation, firstSpawnWorld);
    }

    public String getFirstSpawnLocation() {
        return configHandle.get(configNodes.firstSpawnLocation);
    }

    public Try<Void> setEnableJoinDestination(boolean enableJoinDestination) {
        return configHandle.set(configNodes.enableJoinDestination, enableJoinDestination);
    }

    public boolean getEnableJoinDestination() {
        return  configHandle.get(configNodes.enableJoinDestination);
    }

    public Try<Void> setJoinDestination(String alwaysSpawnDestination) {
        return configHandle.set(configNodes.joinDestination, alwaysSpawnDestination);
    }

    public String getJoinDestination() {
        return  configHandle.get(configNodes.joinDestination);
    }

    public Try<Void> setDefaultRespawnInOverworld(boolean defaultRespawnInOverworld) {
        return configHandle.set(configNodes.defaultRespawnInOverworld, defaultRespawnInOverworld);
    }

    public boolean getDefaultRespawnInOverworld() {
        return configHandle.get(configNodes.defaultRespawnInOverworld);
    }

    public Try<Void> setDefaultRespawnWithinSameWorld(boolean defaultRespawnToWorldSpawn) {
        return configHandle.set(configNodes.defaultRespawnWithinSameWorld, defaultRespawnToWorldSpawn);
    }

    public boolean getDefaultRespawnWithinSameWorld() {
        return configHandle.get(configNodes.defaultRespawnWithinSameWorld);
    }

    public Try<Void> setEnforceRespawnAtWorldSpawn(boolean enforceRespawnAtWorldSpawn) {
        return configHandle.set(configNodes.enforceRespawnAtWorldSpawn, enforceRespawnAtWorldSpawn);
    }

    public boolean getEnforceRespawnAtWorldSpawn() {
        return configHandle.get(configNodes.enforceRespawnAtWorldSpawn);
    }

    public Try<Void> setUseCustomPortalSearch(boolean useDefaultPortalSearch) {
        return configHandle.set(configNodes.useCustomPortalSearch, useDefaultPortalSearch);
    }

    public boolean isUsingCustomPortalSearch() {
        return configHandle.get(configNodes.useCustomPortalSearch);
    }

    public Try<Void> setCustomPortalSearchRadius(int searchRadius) {
        return configHandle.set(configNodes.customPortalSearchRadius, searchRadius);
    }

    public int getCustomPortalSearchRadius() {
        return configHandle.get(configNodes.customPortalSearchRadius);
    }

    public Try<Void> setEnablePrefixChat(boolean prefixChat) {
        return configHandle.set(configNodes.enableChatPrefix, prefixChat);
    }

    public boolean isEnablePrefixChat() {
        return configHandle.get(configNodes.enableChatPrefix);
    }

    public Try<Void> setPrefixChatFormat(String prefixChatFormat) {
        return configHandle.set(configNodes.chatPrefixFormat, prefixChatFormat);
    }

    public String getPrefixChatFormat() {
        return configHandle.get(configNodes.chatPrefixFormat);
    }

    public Try<Void> setRegisterPapiHook(boolean registerPapiHook) {
        return configHandle.set(configNodes.registerPapiHook, registerPapiHook);
    }

    public boolean isRegisterPapiHook() {
        return configHandle.get(configNodes.registerPapiHook);
    }

    @ApiStatus.AvailableSince("5.5")
    public Try<Void> setWarnInvalidPapiFormat(boolean warnInvalidPapiFormat) {
        return configHandle.set(configNodes.warnInvalidPapiFormat, warnInvalidPapiFormat);
    }

    @ApiStatus.AvailableSince("5.5")
    public boolean getWarnInvalidPapiFormat() {
        return configHandle.get(configNodes.warnInvalidPapiFormat);
    }

    @ApiStatus.AvailableSince("5.5")
    public Try<Void> setInvalidPapiFormatReturnsBlank(boolean invalidPapiFormatReturnsBlank) {
        return configHandle.set(configNodes.invalidPapiFormatReturnsBlank, invalidPapiFormatReturnsBlank);
    }

    @ApiStatus.AvailableSince("5.5")
    public  boolean getInvalidPapiFormatReturnsBlank() {
        return configHandle.get(configNodes.invalidPapiFormatReturnsBlank);
    }

    public Try<Void> setDefaultLocale(Locale defaultLocale) {
        return configHandle.set(configNodes.defaultLocale, defaultLocale);
    }

    public Locale getDefaultLocale() {
        return configHandle.get(configNodes.defaultLocale);
    }

    public Try<Void> setPerPlayerLocale(boolean perPlayerLocale) {
        return configHandle.set(configNodes.perPlayerLocale, perPlayerLocale);
    }

    public boolean getPerPlayerLocale() {
        return configHandle.get(configNodes.perPlayerLocale);
    }

    public Try<Void> setResolveAliasName(boolean resolveAliasInCommands) {
        return configHandle.set(configNodes.resolveAliasName, resolveAliasInCommands);
    }

    public boolean getResolveAliasName() {
        return configHandle.get(configNodes.resolveAliasName);
    }

    @ApiStatus.AvailableSince("5.4")
    public Try<Void> setSimplifiedDestinationTabCompletion(boolean simplifiedDestinationTabCompletion) {
        return configHandle.set(configNodes.simplifiedDestinationTabCompletion, simplifiedDestinationTabCompletion);
    }

    @ApiStatus.AvailableSince("5.4")
    public boolean getSimplifiedDestinationTabCompletion() {
        return configHandle.get(configNodes.simplifiedDestinationTabCompletion);
    }

    public Try<Void> setConfirmMode(ConfirmMode confirmMode) {
        return configHandle.set(configNodes.confirmMode, confirmMode);
    }

    public ConfirmMode getConfirmMode() {
        return configHandle.get(configNodes.confirmMode);
    }

    public Try<Void> setUseConfirmOtp(boolean useConfirmOtp) {
        return configHandle.set(configNodes.useConfirmOtp, useConfirmOtp);
    }

    public boolean getUseConfirmOtp() {
        return configHandle.get(configNodes.useConfirmOtp);
    }

    public Integer getConfirmTimeout() {
        return configHandle.get(configNodes.confirmTimeout);
    }

    public Try<Void> setConfirmTimeout(int confirmTimeout) {
        return configHandle.set(configNodes.confirmTimeout, confirmTimeout);
    }

    public boolean getShowLegacyAliases() {
        return configHandle.get(configNodes.showLegacyAliases);
    }

    public Try<Void> setShowLegacyAliases(boolean showLegacyAliases) {
        return configHandle.set(configNodes.showLegacyAliases, showLegacyAliases);
    }

    public Try<Void> setEventPriorityPlayerPortal(EventPriority eventPriorityPlayerPortal) {
        return configHandle.set(configNodes.eventPriorityPlayerPortal, eventPriorityPlayerPortal);
    }

    public EventPriority getEventPriorityPlayerPortal() {
        return configHandle.get(configNodes.eventPriorityPlayerPortal);
    }

    public Try<Void> setEventPriorityPlayerRespawn(EventPriority eventPriorityPlayerRespawn) {
        return configHandle.set(configNodes.eventPriorityPlayerRespawn, eventPriorityPlayerRespawn);
    }

    public EventPriority getEventPriorityPlayerRespawn() {
        return configHandle.get(configNodes.eventPriorityPlayerRespawn);
    }

    public Try<Void> getEventPriorityPlayerSpawnLocation(EventPriority eventPriorityPlayerSpawnLocation) {
        return configHandle.set(configNodes.eventPriorityPlayerSpawnLocation, eventPriorityPlayerSpawnLocation);
    }

    public EventPriority getEventPriorityPlayerSpawnLocation() {
        return configHandle.get(configNodes.eventPriorityPlayerSpawnLocation);
    }

    public Try<Void> setEventPriorityPlayerTeleport(EventPriority eventPriorityPlayerTeleport) {
        return configHandle.set(configNodes.eventPriorityPlayerTeleport, eventPriorityPlayerTeleport);
    }

    public EventPriority getEventPriorityPlayerTeleport() {
        return configHandle.get(configNodes.eventPriorityPlayerTeleport);
    }

    @ApiStatus.AvailableSince("5.5")
    public Try<Void> setEventPriorityPlayerWorldChange(EventPriority eventPriorityPlayerWorldChange) {
        return configHandle.set(configNodes.eventPriorityPlayerWorldChange, eventPriorityPlayerWorldChange);
    }

    @ApiStatus.AvailableSince("5.5")
    public EventPriority getEventPriorityPlayerWorldChange() {
        return configHandle.get(configNodes.eventPriorityPlayerWorldChange);
    }

    public Try<Void> setBukkitYmlPath(String bukkitYmlPath) {
        return configHandle.set(configNodes.bukkitYmlPath, bukkitYmlPath);
    }

    public String getBukkitYmlPath() {
        return configHandle.get(configNodes.bukkitYmlPath);
    }

    @ApiStatus.AvailableSince("5.3")
    public Try<Void> setServerPropertiesPath(String serverPropertiesPath) {
        return configHandle.set(configNodes.serverPropertiesPath, serverPropertiesPath);
    }

    @ApiStatus.AvailableSince("5.3")
    public String getServerPropertiesPath() {
        return configHandle.get(configNodes.serverPropertiesPath);
    }

    @ApiStatus.AvailableSince("5.3")
    public Try<Void> setAutoDetectGeneratorPlugins(boolean autoDetectGeneratorPlugins) {
        return configHandle.set(configNodes.autoDetectGeneratorPlugins, autoDetectGeneratorPlugins);
    }

    @ApiStatus.AvailableSince("5.3")
    public boolean getAutoDetectGeneratorPlugins() {
        return configHandle.get(configNodes.autoDetectGeneratorPlugins);
    }

    public Try<Void> setGlobalDebug(int globalDebug) {
        return configHandle.set(configNodes.globalDebug, globalDebug);
    }

    public int getGlobalDebug() {
        return configHandle.get(configNodes.globalDebug);
    }

    public Try<Void> setDebugPermissions(boolean debugPermissions) {
        return configHandle.set(configNodes.debugPermissions, debugPermissions);
    }

    public boolean getDebugPermissions() {
        return configHandle.get(configNodes.debugPermissions);
    }

    public Try<Void> setSilentStart(boolean silentStart) {
        return configHandle.set(configNodes.silentStart, silentStart);
    }

    public boolean getSilentStart() {
        return configHandle.get(configNodes.silentStart);
    }

    public Try<Void> setShowDonateMessage(boolean showDonateMessage) {
        return configHandle.set(configNodes.showDonationMessage, showDonateMessage);
    }

    public boolean isShowingDonateMessage() {
        return configHandle.get(configNodes.showDonationMessage);
    }

    @ApiStatus.Internal
    public FileConfiguration getConfig() {
        return configHandle.getConfig();
    }
}

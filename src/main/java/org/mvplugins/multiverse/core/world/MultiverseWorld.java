package org.mvplugins.multiverse.core.world;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.UnmodifiableView;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.utils.text.ChatTextFormatter;
import org.mvplugins.multiverse.core.world.helpers.WorldFolderResolver;
import org.mvplugins.multiverse.core.world.location.SpawnLocation;
import org.mvplugins.multiverse.core.world.entity.EntitySpawnConfig;

public sealed class MultiverseWorld permits LoadedMultiverseWorld {
    WorldConfig worldConfig;

    protected final CoreConfig config;
    private String colourlessAlias = "";

    MultiverseWorld(WorldConfig worldConfig, CoreConfig config) {
        this.worldConfig = worldConfig;
        this.config = config;
        this.worldConfig.setMVWorld(this);
        updateColourlessAlias();
    }

    @ApiStatus.AvailableSince("5.7")
    @NotNull
    public NamespacedKey getKey() {
        return worldConfig.getWorldKeyOrName().usableKey();
    }

    @NotNull
    public String getName() {
        return worldConfig.getLegacyWorldName();
    }

    @ApiStatus.AvailableSince("5.7")
    public File getOfflineWorldFolder() {
        return WorldFolderResolver.resolve(this);
    }

    public String getTabCompleteName() {
        return config.getResolveAliasName() ? getColourlessAlias() : getName();
    }

    public boolean isLoaded() {
        return worldConfig.isLoadedWorld();
    }

    @ApiStatus.AvailableSince("5.5")
    public Option<LoadedMultiverseWorld> asLoadedWorld() {
        return Option.of(worldConfig.getMVWorld())
                .filter(world -> world instanceof LoadedMultiverseWorld)
                .map(world -> (LoadedMultiverseWorld) world);
    }

    public StringPropertyHandle getStringPropertyHandle() {
        return worldConfig.getStringPropertyHandle();
    }

    public boolean getAdjustSpawn() {
        return worldConfig.getAdjustSpawn();
    }

    public Try<Void> setAdjustSpawn(boolean adjustSpawn) {
        return worldConfig.setAdjustSpawn(adjustSpawn);
    }

    public String getAlias() {
        return worldConfig.getAlias();
    }

    public Try<Void> setAlias(String alias) {
        return worldConfig.setAlias(alias);
    }

    public String getAliasOrName() {
        return Strings.isNullOrEmpty(worldConfig.getAlias()) ? getName() : worldConfig.getAlias();
    }

    public String getColourlessAlias() {
        return colourlessAlias;
    }

    void updateColourlessAlias() {
        colourlessAlias = ChatTextFormatter.removeColor(getAliasOrName());
    }

    public boolean isAllowAdvancementGrant() {
        return worldConfig.isAllowAdvancementGrant();
    }

    public Try<Void> setAllowAdvancementGrant(boolean allowAdvancementGrant) {
        return worldConfig.setAllowAdvancementGrant(allowAdvancementGrant);
    }

    public boolean isAllowFlight() {
        return worldConfig.isAllowFlight();
    }

    public Try<Void> setAllowFlight(boolean allowFlight) {
        return worldConfig.setAllowFlight(allowFlight);
    }

    public boolean isAllowWeather() {
        return worldConfig.isAllowWeather();
    }

    public Try<Void> setAllowWeather(boolean allowWeather) {
        return worldConfig.setAllowWeather(allowWeather);
    }

    public boolean getAnchorRespawn() {
        return worldConfig.getAnchorRespawn();
    }

    public Try<Void> setAnchorSpawn(boolean anchorSpawn) {
        return worldConfig.setAnchorSpawn(anchorSpawn);
    }

    public boolean getAutoHeal() {
        return worldConfig.getAutoHeal();
    }

    public Try<Void> setAutoHeal(boolean autoHeal) {
        return worldConfig.setAutoHeal(autoHeal);
    }

    public boolean isAutoLoad() {
        return worldConfig.isAutoLoad();
    }

    public Try<Void> setAutoLoad(boolean autoLoad) {
        return worldConfig.setAutoLoad(autoLoad);
    }

    public boolean getBedRespawn() {
        return worldConfig.getBedRespawn();
    }

    public Try<Void> setBedRespawn(boolean bedRespawn) {
        return worldConfig.setBedRespawn(bedRespawn);
    }

    public @NotNull String getBiome() {
        return worldConfig.getBiome();
    }

    public Material getCurrency() {
        return worldConfig.getEntryFeeCurrency();
    }

    public Try<Void> setCurrency(Material currency) {
        return worldConfig.setEntryFeeCurrency(currency);
    }

    public Difficulty getDifficulty() {
        return worldConfig.getDifficulty();
    }

    public Try<Void> setDifficulty(Difficulty difficulty) {
        return worldConfig.setDifficulty(difficulty);
    }

    public World.Environment getEnvironment() {
        return worldConfig.getEnvironment();
    }

    public GameMode getGameMode() {
        return worldConfig.getGameMode();
    }

    public Try<Void> setGameMode(GameMode gameMode) {
        return worldConfig.setGameMode(gameMode);
    }

    public String getGenerator() {
        return worldConfig.getGenerator();
    }

    @ApiStatus.AvailableSince("5.7")
    public String getGeneratorSettings() {
        return worldConfig.getGeneratorSettings();
    }

    public boolean isHidden() {
        return worldConfig.isHidden();
    }

    public Try<Void> setHidden(boolean hidden) {
        return worldConfig.setHidden(hidden);
    }

    public boolean isHunger() {
        return worldConfig.isHunger();
    }

    public Try<Void> setHunger(boolean hunger) {
        return worldConfig.setHunger(hunger);
    }

    public boolean isKeepSpawnInMemory() {
        return worldConfig.isKeepSpawnInMemory();
    }

    public Try<Void> setKeepSpawnInMemory(boolean keepSpawnInMemory) {
        return worldConfig.setKeepSpawnInMemory(keepSpawnInMemory);
    }

    @ApiStatus.AvailableSince("5.7")
    public @UnmodifiableView Map<String, String> getAllMeta() {
        return Collections.unmodifiableMap(worldConfig.getMeta());
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull Option<String> getMeta(@Nullable String key) {
        return Option.of(worldConfig.getMeta().get(key));
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull Try<Void> setMeta(@NotNull String key, @Nullable String value) {
        return worldConfig.setMeta(key, value);
    }

    @ApiStatus.AvailableSince("5.7")
    public @NotNull Try<Void> removeMeta(@NotNull String key) {
        return worldConfig.removeMeta(key);
    }

    public int getPlayerLimit() {
        return worldConfig.getPlayerLimit();
    }

    public Try<Void> setPlayerLimit(int playerLimit) {
        return worldConfig.setPlayerLimit(playerLimit);
    }

    public AllowedPortalType getPortalForm() {
        return worldConfig.getPortalForm();
    }

    public Try<Void> setPortalForm(AllowedPortalType portalForm) {
        return worldConfig.setPortalForm(portalForm);
    }

    public boolean isEntryFeeEnabled() {
        return worldConfig.isEntryFeeEnabled();
    }

    public Try<Void> setEntryFeeEnabled(boolean entryFeeEnabled) {
        return worldConfig.setEntryFeeEnabled(entryFeeEnabled);
    }

    public double getPrice() {
        return worldConfig.getEntryFeeAmount();
    }

    public Try<Void> setPrice(double price) {
        return worldConfig.setEntryFeeAmount(price);
    }

    public boolean getPvp() {
        return worldConfig.getPvp();
    }

    public Try<Void> setPvp(boolean pvp) {
        return worldConfig.setPvp(pvp);
    }

    public String getRespawnWorldName() {
        return worldConfig.getRespawnWorld();
    }

    public @Nullable World getRespawnWorld() {
        return Bukkit.getWorld(worldConfig.getRespawnWorld());
    }

    public Try<Void> setRespawnWorld(World respawnWorld) {
        return worldConfig.setRespawnWorld(respawnWorld.getName());
    }

    public Try<Void> setRespawnWorld(MultiverseWorld respawnWorld) {
        return worldConfig.setRespawnWorld(respawnWorld.getName());
    }

    public Try<Void> setRespawnWorld(String respawnWorld) {
        return worldConfig.setRespawnWorld(respawnWorld);
    }

    public double getScale() {
    return worldConfig.getScale();
    }

    public Try<Void> setScale(double scale) {
        return worldConfig.setScale(scale);
    }

    public long getSeed() {
        return worldConfig.getSeed();
    }

    public Location getSpawnLocation() {
        return worldConfig.getSpawnLocation().toBukkitLocation();
    }

    public Try<Void> setSpawnLocation(Location spawnLocation) {
        return worldConfig.setSpawnLocation(spawnLocation instanceof SpawnLocation
                ? (SpawnLocation) spawnLocation.clone()
                : new SpawnLocation(spawnLocation));
    }

    public EntitySpawnConfig getEntitySpawnConfig() {
        return worldConfig.getEntitySpawnConfig();
    }

    public Try<Void> setEntitySpawnConfig(EntitySpawnConfig entitySpawnConfig) {
        return worldConfig.setEntitySpawnConfig(entitySpawnConfig);
    }

    public List<String> getWorldBlacklist() {
        return worldConfig.getWorldBlacklist();
    }

    public Try<Void> setWorldBlacklist(List<String> worldBlacklist) {
        return worldConfig.setWorldBlacklist(worldBlacklist);
    }

    @NotNull WorldConfig getWorldConfig() {
        return worldConfig;
    }

    void setWorldConfig(@NotNull WorldConfig worldConfig) {
        this.worldConfig = worldConfig;
    }

    @Override
    public String toString() {
        return "MultiverseWorld{"
                + "key='" + getKey() + "', "
                + "name='" + getName() + "', "
                + "env='" + getEnvironment() + "', "
                + "gen='" + getGenerator() + "'"
                + '}';
    }
}

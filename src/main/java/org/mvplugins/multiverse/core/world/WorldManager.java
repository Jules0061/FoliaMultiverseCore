package org.mvplugins.multiverse.core.world;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.base.Strings;
import io.vavr.control.Option;
import io.vavr.control.Try;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.event.world.MVWorldClonedEvent;
import org.mvplugins.multiverse.core.event.world.MVWorldCreatedEvent;
import org.mvplugins.multiverse.core.event.world.MVWorldDeleteEvent;
import org.mvplugins.multiverse.core.event.world.MVWorldImportedEvent;
import org.mvplugins.multiverse.core.event.world.MVWorldLoadedEvent;
import org.mvplugins.multiverse.core.event.world.MVWorldRegeneratedEvent;
import org.mvplugins.multiverse.core.event.world.MVWorldRemovedEvent;
import org.mvplugins.multiverse.core.event.world.MVWorldUnloadedEvent;
import org.mvplugins.multiverse.core.exceptions.world.MultiverseWorldException;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.core.permissions.CorePermissions;
import org.mvplugins.multiverse.core.teleportation.BlockSafety;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.utils.MVScheduler;
import org.mvplugins.multiverse.core.utils.ServerProperties;
import org.mvplugins.multiverse.core.utils.compatibility.BukkitCompatibility;
import org.mvplugins.multiverse.core.utils.compatibility.GameRuleCompatibility;
import org.mvplugins.multiverse.core.utils.compatibility.WorldCompatibility;
import org.mvplugins.multiverse.core.utils.compatibility.WorldCreatorCompatibility;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.utils.result.FailureReason;
import org.mvplugins.multiverse.core.utils.FileUtils;
import org.mvplugins.multiverse.core.world.biomeprovider.BiomeProviderFactory;
import org.mvplugins.multiverse.core.world.entity.EntityPurger;
import org.mvplugins.multiverse.core.world.generators.GeneratorProvider;
import org.mvplugins.multiverse.core.world.helpers.DataStore.GameRulesStore;
import org.mvplugins.multiverse.core.world.helpers.DataTransfer;
import org.mvplugins.multiverse.core.world.helpers.DimensionFinder;
import org.mvplugins.multiverse.core.world.helpers.PotentialWorldFinder;
import org.mvplugins.multiverse.core.world.helpers.WorldFolderResolver;
import org.mvplugins.multiverse.core.world.helpers.WorldNameChecker;
import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;
import org.mvplugins.multiverse.core.world.options.CloneWorldOptions;
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.core.world.options.ImportWorldOptions;
import org.mvplugins.multiverse.core.world.options.KeepWorldSettingsOptions;
import org.mvplugins.multiverse.core.world.options.LoadWorldOptions;
import org.mvplugins.multiverse.core.world.options.RegenWorldOptions;
import org.mvplugins.multiverse.core.world.options.RemoveWorldOptions;
import org.mvplugins.multiverse.core.world.options.UnloadWorldOptions;
import org.mvplugins.multiverse.core.world.reasons.CloneFailureReason;
import org.mvplugins.multiverse.core.world.reasons.CreateFailureReason;
import org.mvplugins.multiverse.core.world.reasons.DeleteFailureReason;
import org.mvplugins.multiverse.core.world.reasons.ImportFailureReason;
import org.mvplugins.multiverse.core.world.reasons.LoadFailureReason;
import org.mvplugins.multiverse.core.world.reasons.RegenFailureReason;
import org.mvplugins.multiverse.core.world.reasons.RemoveFailureReason;
import org.mvplugins.multiverse.core.world.reasons.UnloadFailureReason;
import org.mvplugins.multiverse.core.world.reasons.WorldCreatorFailureReason;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;
import static org.mvplugins.multiverse.core.world.helpers.DataStore.WorldBorderStore;
import static org.mvplugins.multiverse.core.world.helpers.DataStore.WorldConfigStore;

@Service
public final class WorldManager {

    private static final List<String> CLONE_IGNORE_FILES = List.of(
            "uid.dat", "session.lock",
            "data/paper/metadata.dat"
    );

    private final WorldStore worldStore;
    private final List<String> unloadTracker;
    private final List<String> loadTracker;
    private final WorldsConfigManager worldsConfigManager;
    private final WorldNameChecker worldNameChecker;
    private final BiomeProviderFactory biomeProviderFactory;
    private final GeneratorProvider generatorProvider;
    private final FileUtils fileUtils;
    private final BlockSafety blockSafety;
    private final LocationManipulation locationManipulation;
    private final PluginManager pluginManager;
    private final CorePermissions corePermissions;
    private final ServerProperties serverProperties;
    private final CoreConfig config;
    private final EntityPurger entityPurger;
    private final MVScheduler scheduler;
    private final Provider<PotentialWorldFinder> potentialWorldFinder;

    @Inject
    WorldManager(
            @NotNull WorldStore worldStore,
            @NotNull WorldsConfigManager worldsConfigManager,
            @NotNull WorldNameChecker worldNameChecker,
            @NotNull BiomeProviderFactory biomeProviderFactory,
            @NotNull GeneratorProvider generatorProvider,
            @NotNull FileUtils fileUtils,
            @NotNull BlockSafety blockSafety,
            @NotNull LocationManipulation locationManipulation,
            @NotNull PluginManager pluginManager,
            @NotNull CorePermissions corePermissions,
            @NotNull ServerProperties serverProperties,
            @NotNull CoreConfig config,
            @NotNull EntityPurger entityPurger,
            @NotNull MVScheduler scheduler,
            @NotNull Provider<PotentialWorldFinder> potentialWorldFinder) {
        this.worldStore = worldStore;
        this.worldsConfigManager = worldsConfigManager;
        this.worldNameChecker = worldNameChecker;
        this.biomeProviderFactory = biomeProviderFactory;
        this.generatorProvider = generatorProvider;
        this.fileUtils = fileUtils;
        this.blockSafety = blockSafety;
        this.locationManipulation = locationManipulation;
        this.pluginManager = pluginManager;
        this.corePermissions = corePermissions;
        this.serverProperties = serverProperties;
        this.config = config;
        this.entityPurger = entityPurger;
        this.scheduler = scheduler;
        this.potentialWorldFinder = potentialWorldFinder;

        this.unloadTracker = new CopyOnWriteArrayList<>();
        this.loadTracker = new CopyOnWriteArrayList<>();
    }

    @ApiStatus.Internal
    public Try<Void> initAllWorlds() {
        return updateWorldsFromConfig().andThenTry(() -> {
            importExistingWorlds();
            autoLoadWorlds();
        }).flatMap(ignore -> saveWorldsConfig());
    }

    private Try<Void> updateWorldsFromConfig() {
        return worldsConfigManager.load().mapTry(result -> {
            loadNewWorldConfigs(result.newWorlds());
            removeWorldsNotInConfigs(result.removedWorlds());
            return null;
        });
    }

    private void loadNewWorldConfigs(Collection<WorldConfig> newWorldConfigs) {
        newWorldConfigs.forEach(worldConfig -> {
            worldStore.getUnloadedWorldRef(worldConfig.getWorldKeyOrName().usableName())
                    .peek(unloadedWorld -> unloadedWorld.setWorldConfig(worldConfig))
                    .onEmpty(() -> newMultiverseWorld(worldConfig));
            worldStore.getLoadedWorld(worldConfig.getWorldKeyOrName().usableName())
                    .peek(loadedWorld -> loadedWorld.setWorldConfig(worldConfig));
        });
    }

    @SuppressWarnings("removal")
    private void removeWorldsNotInConfigs(Collection<WorldKeyOrName> removedWorlds) {
        removedWorlds.forEach(keyOrName -> getWorld(keyOrName.usableName())
                .map(world -> removeWorld(RemoveWorldOptions.world(world)))
                .getOrElse(() -> worldActionResult(RemoveFailureReason.WORLD_NON_EXISTENT, keyOrName))
                .onFailure(failure ->
                        Logging.severe("Failed to unload world %s: %s", keyOrName, failure))
                .onSuccess(success ->
                        Logging.fine("Unloaded world %s as it was removed from config", keyOrName)));
    }

    private void importExistingWorlds() {
        Map<String, World> bukkitWorlds = Bukkit.getWorlds()
                .stream()
                .collect(Collectors.toMap(World::getName, Function.identity()));

        serverProperties.getLevelName().peek(overworldName -> {
            World overworld = bukkitWorlds.remove(overworldName);
            World nether = bukkitWorlds.remove(DimensionFinder.DEFAULT_NETHER_FORMAT.replaceOverworld(overworldName));
            World end = bukkitWorlds.remove(DimensionFinder.DEFAULT_END_FORMAT.replaceOverworld(overworldName));

            if (config.getAutoImportDefaultWorlds()) {
                importExistingBukkitWorld(overworld);
                importExistingBukkitWorld(nether);
                importExistingBukkitWorld(end);
            }
        });

        if (config.getAutoImport3rdPartyWorlds()) {
            bukkitWorlds.values().forEach(this::importExistingBukkitWorld);
        }
    }

    private void importExistingBukkitWorld(World bukkitWorld) {
        Option.of(bukkitWorld)
                .filter(world -> !isWorld(world.getName()))
                .map(world -> importWorld(
                        ImportWorldOptions.worldName(world.getName())
                                .environment(world.getEnvironment())
                                .generator(generatorProvider.getDefaultGeneratorForWorld(world.getName())))
                        .onFailure(failure ->
                                Logging.severe("Failed to import world %s: %s", world.getName(), failure))
                        .onSuccess(newMVWorld ->
                                Logging.fine("Imported existing world %s", newMVWorld.getName())));
    }

    private void autoLoadWorlds() {
        getWorlds().stream()
                .filter(world -> !isLoadedWorld(world) && world.isAutoLoad())
                .forEach(world -> loadWorld(LoadWorldOptions.world(world))
                        .onFailure(failure ->
                                Logging.severe("Failed to autoload world %s: %s", world.getName(), failure))
                        .onSuccess(newMVWorld ->
                                Logging.fine("Autoloaded world %s", newMVWorld.getName())));
    }

    public Attempt<LoadedMultiverseWorld, CreateFailureReason> createWorld(CreateWorldOptions options) {
        return parseCreateWorldOptionsKeyOrName(options).mapAttempt(this::doCreateWorld);
    }

    private Attempt<KeyOrNameWithOptions<CreateWorldOptions>, CreateFailureReason> parseCreateWorldOptionsKeyOrName(
            CreateWorldOptions options) {
        return options.keyOrName()
                .fold(WorldKeyOrName::parse, Attempt::success)
                .transform(CreateFailureReason.INVALID_WORLDNAME)
                .failIf(keyOrName -> !worldNameChecker.isValidWorldName(keyOrName.usableName()),
                        keyOrName -> worldActionResult(CreateFailureReason.INVALID_WORLDNAME, keyOrName))
                .failIf(keyOrName -> keyOrName.isKey() && !WorldCreatorCompatibility.canCreateWorldWithKey(),
                        keyOrName -> worldActionResult(CreateFailureReason.NAMESPACEDKEY_UNSUPPORTED, keyOrName))
                .failIf(worldStore::isLoadedWorld,
                        keyOrName -> worldActionResult(CreateFailureReason.WORLD_EXIST_LOADED, keyOrName))
                .failIf(worldStore::isUnloadedWorld,
                        keyOrName -> worldActionResult(CreateFailureReason.WORLD_EXIST_UNLOADED, keyOrName))
                .failIf(keyOrName -> options.doFolderCheck() && worldNameChecker.hasWorldFolder(keyOrName),
                        keyOrName -> worldActionResult(CreateFailureReason.WORLD_EXIST_FOLDER, keyOrName))
                .map(keyOrName -> new KeyOrNameWithOptions<>(keyOrName, options));
    }

    private Attempt<LoadedMultiverseWorld, CreateFailureReason> doCreateWorld(
            KeyOrNameWithOptions<CreateWorldOptions> keyOrNameWithOptions) {
        WorldKeyOrName keyOrName = keyOrNameWithOptions.keyOrName();
        CreateWorldOptions options = keyOrNameWithOptions.options();
        String generatorString = generatorProvider.parseGeneratorString(keyOrName.usableName(), options.generator());
        WorldCreator worldCreator = WorldCreatorCompatibility.ofKeyOrName(keyOrName)
                .environment(options.environment())
                .generateStructures(options.generateStructures())
                .generatorSettings(options.generatorSettings())
                .seed(options.seed())
                .type(options.worldType());
        WorldCreatorCompatibility.setBonusChest(worldCreator, options.bonusChest());
        options.forcedSpawnPosition().peek(position -> WorldCreatorCompatibility.setForcedSpawnPosition(worldCreator, position));
        return addBiomeProviderToCreator(worldCreator, keyOrName.usableName(), options.biome())
                .mapAttempt(creator -> addGeneratorToCreator(creator, generatorString))
                .mapAttempt(this::createBukkitWorld)
                .transform(CreateFailureReason.WORLD_CREATOR_FAILED)
                .map(bukkitWorld -> newLoadedMultiverseWorld(
                        bukkitWorld,
                        generatorString,
                        options.biome(),
                        options.generatorSettings(),
                        options.useSpawnAdjust()))
                .peek(loadedWorld -> postCreateWorld(loadedWorld, options));
    }

    private void postCreateWorld(LoadedMultiverseWorld loadedWorld, CreateWorldOptions options) {
        options.worldPropertyStrings().forEach((key, value) -> loadedWorld.getStringPropertyHandle()
                .setPropertyString(key, value)
                .onFailure(failure -> Logging.warning("Failed to set property '%s' to '%s' for world %s: %s",
                        key, value, loadedWorld.getName(), failure.getMessage())));
        pluginManager.callEvent(new MVWorldCreatedEvent(loadedWorld));
        saveWorldsConfig();
    }

    public Attempt<LoadedMultiverseWorld, ImportFailureReason> importWorld(ImportWorldOptions options) {
        return options.keyOrName()
                .fold(WorldKeyOrName::parse, Attempt::success)
                .transform(ImportFailureReason.INVALID_WORLDNAME)
                .failIf(keyOrName -> !worldNameChecker.isValidWorldName(keyOrName.usableName()),
                        keyOrName -> worldActionResult(ImportFailureReason.INVALID_WORLDNAME, keyOrName))
                .failIf(keyOrName -> keyOrName.isKey() && !WorldCreatorCompatibility.canCreateWorldWithKey(),
                        keyOrName -> worldActionResult(ImportFailureReason.NAMESPACEDKEY_UNSUPPORTED, keyOrName))
                .failIf(worldStore::isLoadedWorld,
                        keyOrName -> worldActionResult(ImportFailureReason.WORLD_EXIST_LOADED, keyOrName))
                .failIf(worldStore::isUnloadedWorld,
                        keyOrName -> worldActionResult(ImportFailureReason.WORLD_EXIST_UNLOADED, keyOrName))
                .map(keyOrName -> new KeyOrNameWithOptions<>(keyOrName, options))
                .mapAttempt(pair -> BukkitCompatibility.getWorldByNameOrKey(pair.keyOrName())
                        .map(bukkitWorld -> doImportBukkitWorld(pair, bukkitWorld))
                        .getOrElse(() -> validateImportWorldOptions(pair).mapAttempt(this::doImportWorld)));
    }

    private Attempt<KeyOrNameWithOptions<ImportWorldOptions>, ImportFailureReason> validateImportWorldOptions(
            KeyOrNameWithOptions<ImportWorldOptions> keyOrNameWithOptions) {
        WorldKeyOrName keyOrName = keyOrNameWithOptions.keyOrName();
        if (keyOrNameWithOptions.options().doFolderCheck()) {
            WorldNameChecker.FolderStatus folderStatus = worldNameChecker.checkFolder(keyOrName);
            if (!folderStatus.isLoadable()) {
                return worldActionResult(ImportFailureReason.WORLD_FOLDER_INVALID, keyOrName);
            }
            if (folderStatus == WorldNameChecker.FolderStatus.REQUIRES_MIGRATION) {
                Logging.info("World '%s' will be automatically migrated by PaperMC to the new dimension " +
                                "location. If you face any issue with migration, please contact PaperMC support!",
                        keyOrName);
            }
        }
        return worldActionResult(keyOrNameWithOptions);
    }

    private Attempt<LoadedMultiverseWorld, ImportFailureReason> doImportWorld(
            KeyOrNameWithOptions<ImportWorldOptions> keyOrNameWithOptions) {
        WorldKeyOrName keyOrName = keyOrNameWithOptions.keyOrName();
        ImportWorldOptions options = keyOrNameWithOptions.options();
        String generatorString = generatorProvider.parseGeneratorString(keyOrName.usableName(), options.generator());
        WorldCreator worldCreator = WorldCreatorCompatibility.ofKeyOrName(keyOrName)
                .environment(options.environment())
                .generatorSettings(options.generatorSettings());

        return addBiomeProviderToCreator(worldCreator, keyOrName.usableName(), options.biome())
                .mapAttempt(creator -> addGeneratorToCreator(creator, generatorString))
                .mapAttempt(this::createBukkitWorld)
                .transform(ImportFailureReason.WORLD_CREATOR_FAILED)
                .map(bukkitWorld -> newLoadedMultiverseWorld(
                        bukkitWorld,
                        generatorString,
                        options.biome(),
                        options.generatorSettings(),
                        options.useSpawnAdjust()))
                .peek(loadedWorld -> pluginManager.callEvent(new MVWorldImportedEvent(loadedWorld)));
    }

    private Attempt<LoadedMultiverseWorld, ImportFailureReason> doImportBukkitWorld(
            KeyOrNameWithOptions<ImportWorldOptions> keyOrNameWithOptions, World bukkitWorld) {
        WorldKeyOrName keyOrName = keyOrNameWithOptions.keyOrName();
        ImportWorldOptions options = keyOrNameWithOptions.options();
        if (options.environment() != bukkitWorld.getEnvironment()) {
            return Attempt.failure(ImportFailureReason.BUKKIT_ENVIRONMENT_MISMATCH,
                    Replace.WORLD.with(bukkitWorld.getName()),
                    replace("{bukkitEnvironment}").with(bukkitWorld.getEnvironment().name()),
                    replace("{mvEnvironment}").with(options.environment().name()));
        }

        LoadedMultiverseWorld loadedWorld = newLoadedMultiverseWorld(
                bukkitWorld,
                generatorProvider.parseGeneratorString(keyOrName.usableName(), options.generator()),
                options.biome(),
                options.generatorSettings(),
                options.useSpawnAdjust());
        pluginManager.callEvent(new MVWorldImportedEvent(loadedWorld));
        return Attempt.success(loadedWorld);
    }

    private MultiverseWorld newMultiverseWorld(WorldConfig worldConfig) {
        MultiverseWorld mvWorld = new MultiverseWorld(worldConfig, config);
        worldStore.putUnloadedWorld(mvWorld);
        corePermissions.addWorldPermissions(mvWorld);
        return mvWorld;
    }

    private LoadedMultiverseWorld newLoadedMultiverseWorld(
            @NotNull World world,
            @Nullable String generator,
            @Nullable String biome,
            @Nullable String generatorSettings,
            boolean adjustSpawn) {
        WorldConfig worldConfig = worldsConfigManager.addWorldConfig(world.getKey());

        worldConfig.setAdjustSpawn(adjustSpawn);
        worldConfig.setGenerator(generator == null ? "" : generator);
        worldConfig.setBiome(biome == null ? "" : biome);
        worldConfig.setGeneratorSettings(generatorSettings == null ? "" : generatorSettings);

        worldConfig.setLegacyWorldName(world.getName());
        worldConfig.setDifficulty(world.getDifficulty());
        worldConfig.setKeepSpawnInMemory(WorldCompatibility.getKeepSpawnInMemory(world));
        worldConfig.setScale(WorldCompatibility.getCoordinateScale(world));

        worldConfig.save();

        newMultiverseWorld(worldConfig);
        LoadedMultiverseWorld loadedWorld = new LoadedMultiverseWorld(
                world,
                worldConfig,
                config,
                blockSafety,
                locationManipulation,
                entityPurger,
                scheduler
        );
        worldStore.putLoadedWorld(loadedWorld);
        saveWorldsConfig();
        pluginManager.callEvent(new MVWorldLoadedEvent(loadedWorld));
        return loadedWorld;
    }

    @Deprecated(since = "5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    @SuppressWarnings("removal")
    public Attempt<LoadedMultiverseWorld, LoadFailureReason> loadWorld(@NotNull String worldName) {
        return getWorld(worldName)
                .map(world -> loadWorld(LoadWorldOptions.world(world)))
                .getOrElse(() -> worldNameChecker.isValidWorldFolder(worldName)
                        ? worldActionResult(LoadFailureReason.WORLD_EXIST_FOLDER, worldName)
                        : worldActionResult(LoadFailureReason.WORLD_NON_EXISTENT, worldName));
    }

    @Deprecated(since = "5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public Attempt<LoadedMultiverseWorld, LoadFailureReason> loadWorld(@NotNull MultiverseWorld world) {
        return loadWorld(LoadWorldOptions.world(world));
    }

    @ApiStatus.AvailableSince("5.2")
    public Attempt<LoadedMultiverseWorld, LoadFailureReason> loadWorld(@NotNull LoadWorldOptions options) {
        return validateWorldToLoad(options).mapAttempt(this::doLoadWorld);
    }

    private Attempt<LoadWorldOptions, LoadFailureReason> validateWorldToLoad(@NotNull LoadWorldOptions options) {
        MultiverseWorld mvWorld = options.world();
        if (loadTracker.contains(mvWorld.getName())) {
            Logging.fine("World already loading: " + mvWorld.getName());
            return worldActionResult(LoadFailureReason.WORLD_ALREADY_LOADING, mvWorld.getName());
        } else if (isLoadedWorld(mvWorld)) {
            Logging.severe("World already loaded: " + mvWorld.getName());
            return worldActionResult(LoadFailureReason.WORLD_EXIST_LOADED, mvWorld.getName());
        }
        return worldActionResult(options);
    }

    private Attempt<LoadedMultiverseWorld, LoadFailureReason> doLoadWorld(@NotNull LoadWorldOptions options) {
        MultiverseWorld mvWorld = options.world();

        World bukkitWorld = Bukkit.getWorld(mvWorld.getName());
        if (bukkitWorld != null) {
            return doLoadBukkitWorld(bukkitWorld, mvWorld);
        }

        if (options.doFolderCheck()) {
            WorldNameChecker.FolderStatus folderStatus = worldNameChecker.checkFolder(mvWorld.getOfflineWorldFolder());
            if (!folderStatus.isLoadable()) {
                return worldActionResult(LoadFailureReason.WORLD_FOLDER_INVALID, mvWorld.getName());
            }
            if (folderStatus == WorldNameChecker.FolderStatus.REQUIRES_MIGRATION) {
                Logging.info("World '%s' will be automatically migrated by PaperMC to the new dimension " +
                        "location. If you face any issue with migration, please contact PaperMC support!",
                        mvWorld.getName());
            }
        }

        WorldCreator worldCreator = WorldCreatorCompatibility.ofNameAndKey(mvWorld.getKey(), mvWorld.getName())
                .environment(mvWorld.getEnvironment())
                .seed(mvWorld.getSeed());
        return addBiomeProviderToCreator(worldCreator, mvWorld.getName(), mvWorld.getBiome())
                .mapAttempt(creator -> addGeneratorToCreator(creator, mvWorld.getGenerator()))
                .mapAttempt(this::createBukkitWorld)
                .transform(LoadFailureReason.WORLD_CREATOR_FAILED)
                .mapAttempt(newBukkitWorld -> newLoadedMultiverseWorld(mvWorld, newBukkitWorld));
    }

    private @NotNull Attempt<LoadedMultiverseWorld, LoadFailureReason> doLoadBukkitWorld(World bukkitWorld, MultiverseWorld mvWorld) {
        if (bukkitWorld.getEnvironment() != mvWorld.getEnvironment()) {
            return Attempt.failure(LoadFailureReason.BUKKIT_ENVIRONMENT_MISMATCH,
                    Replace.WORLD.with(mvWorld.getName()),
                    replace("{bukkitEnvironment}").with(bukkitWorld.getEnvironment().name()),
                    replace("{mvEnvironment}").with(mvWorld.getEnvironment().name()));
        }
        Logging.finer("World already loaded in bukkit: " + mvWorld.getName());
        return newLoadedMultiverseWorld(mvWorld, bukkitWorld);
    }

    private Attempt<LoadedMultiverseWorld, LoadFailureReason> newLoadedMultiverseWorld(MultiverseWorld mvWorld, World bukkitWorld) {
        WorldConfig worldConfig = mvWorld.getWorldConfig();

        if (worldConfig.getWorldKeyOrName().isName() && mvWorld.getName().equalsIgnoreCase(bukkitWorld.getName())) {
            Logging.info("Migrating world config for '%s' to use namespaced key '%s'...",
                    mvWorld.getName(), bukkitWorld.getKey());
            worldConfig = worldsConfigManager.migrateWorldConfigKey(worldConfig, bukkitWorld.getKey());
            mvWorld.setWorldConfig(worldConfig);
        }

        if (!bukkitWorld.getKey().equals(mvWorld.getKey())) {
            return Attempt.failure(LoadFailureReason.BUKKIT_NAMESPACED_KEY_MISMATCH,
                    Replace.WORLD.with(mvWorld.getName()),
                    replace("{bukkitNamespace}").with(bukkitWorld.getKey()),
                    replace("{mvNamespace}").with(mvWorld.getKey()));
        }

        LoadedMultiverseWorld loadedWorld = new LoadedMultiverseWorld(
                bukkitWorld,
                worldConfig,
                config,
                blockSafety,
                locationManipulation,
                entityPurger,
                scheduler
        );
        worldStore.putLoadedWorld(loadedWorld);
        saveWorldsConfig();
        pluginManager.callEvent(new MVWorldLoadedEvent(loadedWorld));
        return Attempt.success(loadedWorld);
    }

    public Attempt<MultiverseWorld, UnloadFailureReason> unloadWorld(@NotNull UnloadWorldOptions options) {
        LoadedMultiverseWorld world = options.world();
        if (unloadTracker.contains(world.getName())) {
            Logging.fine("World already unloading: " + world.getName());
            return worldActionResult(UnloadFailureReason.WORLD_ALREADY_UNLOADING, world.getName());
        }

        if (!isLoadedWorld(world)) {
            Logging.severe("World is not loaded: " + world.getName());
            return worldActionResult(UnloadFailureReason.WORLD_NON_EXISTENT, world.getName());
        }

        if (!options.unloadBukkitWorld()) {
            return removeLoadedMultiverseWorld(world);
        }

        return unloadBukkitWorld(world.getBukkitWorld().getOrNull(), options.saveBukkitWorld()).fold(
                exception -> worldActionResult(UnloadFailureReason.BUKKIT_UNLOAD_FAILED, world.getName(), exception),
                success -> removeLoadedMultiverseWorld(world));
    }

    private Attempt<MultiverseWorld, UnloadFailureReason> removeLoadedMultiverseWorld(@NotNull LoadedMultiverseWorld mvWorld) {
        MultiverseWorld unloadedWorld = worldStore.getUnloadedWorldRef(mvWorld.getKey().toString()).getOrElseThrow(
                () -> new IllegalStateException("Unloaded ref of world not found: " + mvWorld));
        worldStore.removeLoadedWorld(mvWorld);
        mvWorld.getWorldConfig().setMVWorld(unloadedWorld);
        pluginManager.callEvent(new MVWorldUnloadedEvent(mvWorld));
        return worldActionResult(unloadedWorld);
    }

    @Deprecated(since = "5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    @SuppressWarnings("removal")
    public Attempt<String, RemoveFailureReason> removeWorld(@NotNull String worldName) {
        return getWorld(worldName)
                .map(world -> removeWorld(RemoveWorldOptions.world(world)))
                .getOrElse(() -> worldActionResult(RemoveFailureReason.WORLD_NON_EXISTENT, worldName));
    }

    @Deprecated(since = "5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public Attempt<String, RemoveFailureReason> removeWorld(@NotNull MultiverseWorld world) {
        return removeWorld(RemoveWorldOptions.world(world));
    }

    @Deprecated(since = "5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public Attempt<String, RemoveFailureReason> removeWorld(@NotNull LoadedMultiverseWorld loadedWorld) {
        return removeWorld(RemoveWorldOptions.world(loadedWorld));
    }

    @ApiStatus.AvailableSince("5.2")
    public Attempt<String, RemoveFailureReason> removeWorld(@NotNull RemoveWorldOptions options) {
        MultiverseWorld world = options.world();
        return getLoadedWorld(world).fold(
                () -> removeWorldFromConfig(world),
                loadedWorld -> unloadBeforeRemoveWorld(loadedWorld, options)
        );
    }

    private Attempt<String, RemoveFailureReason> unloadBeforeRemoveWorld(@NotNull LoadedMultiverseWorld loadedWorld,
                                                                         @NotNull RemoveWorldOptions options) {
        UnloadWorldOptions unloadWorldOptions = UnloadWorldOptions.world(loadedWorld)
                .saveBukkitWorld(options.saveBukkitWorld())
                .unloadBukkitWorld(options.unloadBukkitWorld());

        return unloadWorld(unloadWorldOptions)
                .transform(RemoveFailureReason.UNLOAD_FAILED)
                .mapAttempt(this::removeWorldFromConfig);
    }

    private Attempt<String, RemoveFailureReason> removeWorldFromConfig(@NotNull MultiverseWorld world) {
        worldStore.removeWorld(world);

        WorldConfig worldConfig = world.getWorldConfig();
        worldConfig.deferenceMVWorld();
        worldsConfigManager.deleteWorldConfig(worldConfig);
        saveWorldsConfig();

        corePermissions.removeWorldPermissions(world);

        pluginManager.callEvent(new MVWorldRemovedEvent(world));
        return worldActionResult(world.getName());
    }

    public Attempt<String, DeleteFailureReason> deleteWorld(@NotNull DeleteWorldOptions options) {
        return getLoadedWorld(options.world()).fold(
                () -> loadThenDeleteWorld(options),
                world -> doDeleteWorld(world, options));
    }

    private Attempt<String, DeleteFailureReason> loadThenDeleteWorld(@NotNull DeleteWorldOptions options) {
        return loadWorld(LoadWorldOptions.world(options.world()))
                .fold(
                        ignore -> worldActionResult(DeleteFailureReason.LOAD_FAILED, options.world().getName()),
                        loadedWorld -> doDeleteWorld(loadedWorld, options)
                );
    }

    private Attempt<String, DeleteFailureReason> doDeleteWorld(@NotNull LoadedMultiverseWorld world, @NotNull DeleteWorldOptions options) {
        AtomicReference<File> worldFolder = new AtomicReference<>();
        return validateWorldToDelete(world)
                .peek(worldFolder::set)
                .mapAttempt(() -> {
                    MVWorldDeleteEvent event = new MVWorldDeleteEvent(world);
                    pluginManager.callEvent(event);
                    return event.isCancelled()
                            ? Attempt.failure(DeleteFailureReason.EVENT_CANCELLED)
                            : Attempt.success(null);
                })
                .mapAttempt(() -> removeWorld(RemoveWorldOptions
                        .world(world)
                        .unloadBukkitWorld(true)
                        .saveBukkitWorld(false)
                ).transform(DeleteFailureReason.REMOVE_FAILED))
                .mapAttempt(() -> fileUtils.deleteFolder(worldFolder.get(), options.keepFiles()).fold(
                        exception -> worldActionResult(DeleteFailureReason.FAILED_TO_DELETE_FOLDER,
                                world.getName(), exception),
                        success -> worldActionResult(world.getName())));
    }

    private Attempt<File, DeleteFailureReason> validateWorldToDelete(
            @NotNull LoadedMultiverseWorld world) {
        return world.getBukkitWorld().map(World::getWorldFolder)
                .peek(folder -> Logging.finer("World folder for world %s is at: %s", world.getName(), folder.getPath()))
                .filter(worldNameChecker::isValidWorldFolder)
                .map(this::<File, DeleteFailureReason>worldActionResult)
                .getOrElse(() -> {
                    Logging.severe("World folder does not exist for world: " + world.getName());
                    return worldActionResult(DeleteFailureReason.WORLD_FOLDER_NOT_FOUND, world.getName());
                });
    }

    public Attempt<LoadedMultiverseWorld, CloneFailureReason> cloneWorld(@NotNull CloneWorldOptions options) {
        return parseCloneWorldOptionsNewWorld(options)
                .mapAttempt(this::cloneWorldCopyFolder)
                .mapAttempt(keyOrNameWithOptions -> {
                    ImportWorldOptions importWorldOptions = ImportWorldOptions
                            .worldKeyOrName(keyOrNameWithOptions.keyOrName())
                            .biome(options.fromWorld().getBiome())
                            .environment(options.fromWorld().getEnvironment())
                            .generator(options.fromWorld().getGenerator());
                    return importWorld(importWorldOptions).transform(CloneFailureReason.IMPORT_FAILED);
                })
                .onSuccess(newWorld -> {
                    cloneWorldTransferData(options, newWorld);
                    if (options.keepWorldConfig()) {
                        newWorld.setSpawnLocation(options.fromWorld().getSpawnLocation());
                    }
                    saveWorldsConfig();
                    pluginManager.callEvent(new MVWorldClonedEvent(newWorld, options.fromWorld()));
                });
    }

    private Attempt<KeyOrNameWithOptions<CloneWorldOptions>, CloneFailureReason> parseCloneWorldOptionsNewWorld(
            @NotNull CloneWorldOptions options) {
        return options.newWorldKeyOrName()
                .fold(WorldKeyOrName::parse, Attempt::success)
                .transform(CloneFailureReason.INVALID_WORLDNAME)
                .failIf(keyOrName -> !worldNameChecker.isValidWorldName(keyOrName.usableName()),
                        keyOrName -> worldActionResult(CloneFailureReason.INVALID_WORLDNAME, keyOrName))
                .failIf(keyOrName -> keyOrName.isKey() && !WorldCreatorCompatibility.canCreateWorldWithKey(),
                        keyOrName -> worldActionResult(CloneFailureReason.NAMESPACEDKEY_UNSUPPORTED, keyOrName))
                .failIf(worldStore::isLoadedWorld,
                        keyOrName -> worldActionResult(CloneFailureReason.WORLD_EXIST_LOADED, keyOrName))
                .failIf(worldStore::isWorld,
                        keyOrName -> worldActionResult(CloneFailureReason.WORLD_EXIST_UNLOADED, keyOrName))
                .failIf(worldNameChecker::hasWorldFolder,
                        keyOrName -> worldActionResult(CloneFailureReason.WORLD_EXIST_FOLDER, keyOrName))
                .failIf(keyOrName -> !worldNameChecker.isValidWorldFolder(options.fromWorld().getOfflineWorldFolder()),
                        keyOrName -> worldActionResult(CloneFailureReason.FROM_WORLD_FOLDER_INVALID,
                                options.fromWorld().getName()))
                .map(keyOrName -> new KeyOrNameWithOptions<>(keyOrName, options));
    }

    private Attempt<KeyOrNameWithOptions<CloneWorldOptions>, CloneFailureReason> cloneWorldCopyFolder(
            @NotNull KeyOrNameWithOptions<CloneWorldOptions> keyOrNameWithOptions) {
        WorldKeyOrName newWorldkeyOrName = keyOrNameWithOptions.keyOrName();
        CloneWorldOptions options = keyOrNameWithOptions.options();
        if (options.saveBukkitWorld()) {
            options.fromWorld().asLoadedWorld().peek(loadedWorld -> {
                Logging.finer("Saving world before cloning: " + loadedWorld.getName());
                loadedWorld.getBukkitWorld().peek(bukkitWorld -> WorldCompatibility.saveWithFlush(bukkitWorld, true));
            });
        }
        File worldFolder = WorldFolderResolver.resolve(options.fromWorld());
        File newWorldFolder = WorldFolderResolver.resolve(newWorldkeyOrName);
        return fileUtils.copyFolder(worldFolder, newWorldFolder, CLONE_IGNORE_FILES).fold(
                exception -> worldActionResult(CloneFailureReason.COPY_FAILED,
                        options.fromWorld().getName(), exception),
                success -> worldActionResult(keyOrNameWithOptions));
    }

    private void cloneWorldTransferData(@NotNull CloneWorldOptions options, @NotNull LoadedMultiverseWorld newWorld) {
        if (options.keepWorldConfig()) {
            new DataTransfer<MultiverseWorld>()
                    .addDataStore(new WorldConfigStore<>(), options.fromWorld())
                    .pasteAllTo(newWorld);
        }

        newWorld.getBukkitWorld().peek(bukkitWorld -> {
            if (!options.keepWorldBorder()) {
                WorldBorder worldBorder = bukkitWorld.getWorldBorder();
                worldBorder.reset();
            }

            if (!options.keepGameRule()) {
                Arrays.stream(bukkitWorld.getGameRules())
                        .map(gameRuleName -> GameRuleCompatibility.<Object>getByName(gameRuleName))
                        .filter(Objects::nonNull)
                        .forEach(gameRule -> {
                            Object gameRuleDefault = bukkitWorld.getGameRuleDefault(gameRule);
                            if (gameRuleDefault != null) {
                                bukkitWorld.setGameRule(gameRule, gameRuleDefault);
                            }
                        });
            }
        });
    }

    public Attempt<LoadedMultiverseWorld, RegenFailureReason> regenWorld(@NotNull RegenWorldOptions options) {
        LoadedMultiverseWorld world = options.world();
        DataTransfer<LoadedMultiverseWorld> dataTransfer = regenWorldTransferData(options, world);
        boolean shouldKeepSpawnLocation = options.keepWorldConfig() && options.seed() == world.getSeed();
        Location spawnLocation = world.getSpawnLocation();
        CreateWorldOptions createWorldOptions = CreateWorldOptions.worldKeyOrName(world.worldConfig.getWorldKeyOrName())
                .biome(world.getBiome())
                .bonusChest(world.getBukkitWorld().map(WorldCompatibility::hasBonusChest).getOrElse(false))
                .environment(world.getEnvironment())
                .generateStructures(world.canGenerateStructures().getOrElse(true))
                .generator(world.getGenerator())
                .generatorSettings(world.getGeneratorSettings())
                .seed(options.seed())
                .useSpawnAdjust(!shouldKeepSpawnLocation && world.getAdjustSpawn())
                .worldType(world.getWorldType().getOrElse(WorldType.NORMAL))
                .doFolderCheck(options.keepFiles().isEmpty());

        return deleteWorld(DeleteWorldOptions.world(world).keepFiles(options.keepFiles()))
                .transform(RegenFailureReason.DELETE_FAILED)
                .mapAttempt(() -> createWorld(createWorldOptions).transform(RegenFailureReason.CREATE_FAILED))
                .onSuccess(newWorld -> {
                    dataTransfer.pasteAllTo(newWorld);
                    if (shouldKeepSpawnLocation) {
                        newWorld.setSpawnLocation(spawnLocation);
                    }
                    saveWorldsConfig();
                    pluginManager.callEvent(new MVWorldRegeneratedEvent(newWorld));
                });
    }

    private DataTransfer<LoadedMultiverseWorld> regenWorldTransferData(
            @NotNull KeepWorldSettingsOptions options, @NotNull LoadedMultiverseWorld world) {
        DataTransfer<LoadedMultiverseWorld> dataTransfer = new DataTransfer<>();

        if (options.keepWorldConfig()) {
            dataTransfer.addDataStore(new WorldConfigStore<>(), world);
        }
        if (options.keepGameRule()) {
            dataTransfer.addDataStore(new GameRulesStore(), world);
        }
        if (options.keepWorldBorder()) {
            dataTransfer.addDataStore(new WorldBorderStore(), world);
        }

        return dataTransfer;
    }

    private <T, F extends FailureReason> Attempt<T, F> worldActionResult(@NotNull T value) {
        return Attempt.success(value);
    }

    private <T, F extends FailureReason> Attempt.Failure<T, F> worldActionResult(
            @NotNull F failureReason, @NotNull Object worldName) {
        return Attempt.failureRef(failureReason, Replace.WORLD.with(worldName));
    }

    private <T, F extends FailureReason> Attempt.Failure<T, F> worldActionResult(
            @NotNull F failureReason, @NotNull String worldName, @NotNull Throwable error) {
        return Attempt.failureRef(failureReason, Replace.WORLD.with(worldName), Replace.ERROR.with(error));
    }

    private Attempt<WorldCreator, WorldCreatorFailureReason> addBiomeProviderToCreator(
            WorldCreator worldCreator, String worldName, String biomeString) {
        return Try.of(() -> worldCreator.biomeProvider(biomeProviderFactory.parseBiomeProvider(worldName, biomeString)))
                .fold(
                        throwable -> {
                            throwable.printStackTrace();
                            return Attempt.failure(WorldCreatorFailureReason.INVALID_BIOME_PROVIDER,
                                    replace("{biome}").with(biomeString),
                                    Replace.ERROR.with(throwable));
                        },
                        Attempt::success
                );
    }

    private Attempt<WorldCreator, WorldCreatorFailureReason> addGeneratorToCreator(
            WorldCreator worldCreator, String generatorString) {
        if (Strings.isNullOrEmpty(generatorString)) {
            return Attempt.success(worldCreator);
        }
        return Try.of(() -> worldCreator.generator(generatorString))
                .fold(
                        throwable -> {
                            throwable.printStackTrace();
                            return Attempt.failure(WorldCreatorFailureReason.INVALID_CHUNK_GENERATOR,
                                    replace("{generator}").with(generatorString),
                                    Replace.ERROR.with(throwable));
                        },
                        Attempt::success);
    }

    private Attempt<World, WorldCreatorFailureReason> createBukkitWorld(WorldCreator worldCreator) {
        return Try.of(() -> {
            this.loadTracker.add(worldCreator.name());
            World world = worldCreator.createWorld();
            if (world == null) {
                throw new MultiverseWorldException(Message.of(MVCorei18n.EXCEPTION_MULTIVERSEWORLD_CREATENULL));
            }
            Logging.fine("Bukkit created world: " + world.getName());
            return world;
        }).onFailure(exception -> {
            Logging.severe("Failed to create bukkit world: " + worldCreator.name());
            exception.printStackTrace();
        }).andFinally(() -> {
            this.loadTracker.remove(worldCreator.name());
        }).fold(throwable -> Attempt.failure(WorldCreatorFailureReason.BUKKIT_CREATION_FAILED,
                        Replace.WORLD.with(worldCreator.name()),
                        Replace.ERROR.with(throwable)),
                Attempt::success);
    }

    private Try<Void> unloadBukkitWorld(World world, boolean save) {
        return Try.run(() -> {
            if (world == null) {
                return;
            }
            unloadTracker.add(world.getName());
            if (!Bukkit.unloadWorld(world, save)) {
                throwUnloadException(world);
            }
            Logging.fine("Bukkit unloaded world: " + world.getName());
        }).andFinally(() -> unloadTracker.remove(world.getName()));
    }

    private void throwUnloadException(World world) throws MultiverseWorldException {
        var defaultWorldName = getDefaultWorld().map(LoadedMultiverseWorld::getName).getOrElse("");
        if (Objects.equals(world.getName(), defaultWorldName)) {
            throw new MultiverseWorldException(Message.of(MVCorei18n.EXCEPTION_MULTIVERSEWORLD_UNLOADDEFAULTWORLD,
                    Replace.WORLD.with(world.getName())));
        }
        if (!world.getPlayers().isEmpty()) {
            throw new MultiverseWorldException(Message.of(MVCorei18n.EXCEPTION_MULTIVERSEWORLD_UNLOADPLAYERSINWORLD,
                    replace("{count}").with(world.getPlayers().size())));
        }
        throw new MultiverseWorldException(Message.of(MVCorei18n.EXCEPTION_MULTIVERSEWORLD_UNLOADERROR));
    }

    @Deprecated(forRemoval = true, since = "5.7")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public List<String> getPotentialWorlds() {
        return potentialWorldFinder.get().findPotentialWorlds();
    }

    public Option<MultiverseWorld> getWorld(@Nullable World world) {
        return Option.of(world).map(World::getName).flatMap(this::getWorld);
    }

    public Option<MultiverseWorld> getWorld(@Nullable String worldName) {
        return worldStore.getWorld(worldName);
    }

    public Option<MultiverseWorld> getWorldByNameOrAlias(@Nullable String worldNameOrAlias) {
        return getWorld(worldNameOrAlias)
                .orElse(() -> getWorld(worldStore.translateAlias(worldNameOrAlias)));
    }

    @Unmodifiable
    @NotNull
    public Collection<MultiverseWorld> getWorlds() {
        return worldStore.getWorlds();
    }

    public boolean isWorld(@Nullable String worldName) {
        return worldName != null && worldStore.getWorld(worldName).isDefined();
    }

    public Option<MultiverseWorld> getUnloadedWorld(@Nullable String worldName) {
        return worldStore.getUnloadedWorld(worldName);
    }

    public Option<MultiverseWorld> getUnloadedWorldByNameOrAlias(@Nullable String worldNameOrAlias) {
        return getUnloadedWorld(worldNameOrAlias)
                .orElse(() -> getUnloadedWorld(worldStore.translateAlias(worldNameOrAlias)));
    }

    @Unmodifiable
    @NotNull
    public Collection<MultiverseWorld> getUnloadedWorlds() {
        return worldStore.getUnloadedWorlds();
    }

    public boolean isUnloadedWorld(@Nullable String worldName) {
        return !isLoadedWorld(worldName) && isWorld(worldName);
    }

    public Option<LoadedMultiverseWorld> getLoadedWorld(@Nullable World world) {
        return Option.of(world).flatMap(notNullWorld -> getLoadedWorld(notNullWorld.getName()));
    }

    public Option<LoadedMultiverseWorld> getLoadedWorld(@Nullable MultiverseWorld world) {
        return Option.of(world).flatMap(notNullWorld -> getLoadedWorld(notNullWorld.getName()));
    }

    public Option<LoadedMultiverseWorld> getLoadedWorld(@Nullable String worldName) {
        return Option.of(worldName).flatMap(worldStore::getLoadedWorld);
    }

    public Option<LoadedMultiverseWorld> getLoadedWorldByNameOrAlias(@Nullable String worldNameOrAlias) {
        return getLoadedWorld(worldNameOrAlias)
                .orElse(() -> getLoadedWorld(worldStore.translateAlias(worldNameOrAlias)));
    }

    @Unmodifiable
    @NotNull
    public Collection<LoadedMultiverseWorld> getLoadedWorlds() {
        return worldStore.getLoadedWorlds();
    }

    public boolean isLoadedWorld(@Nullable World world) {
        return world != null && isLoadedWorld(world.getName());
    }

    public boolean isLoadedWorld(@Nullable MultiverseWorld world) {
        return world != null && isLoadedWorld(world.getName());
    }

    public boolean isLoadedWorld(@Nullable String worldName) {
        return worldName != null && worldStore.getLoadedWorld(worldName).isDefined();
    }

    public Option<LoadedMultiverseWorld> getDefaultWorld() {
        return serverProperties.getLevelName().flatMap(this::getLoadedWorld)
                .orElse(getLoadedWorld(Bukkit.getWorlds().get(0)));
    }

    public Try<Void> saveWorldsConfig() {
        return worldsConfigManager.save()
                .onFailure(failure -> {
                    Logging.severe("Failed to save worlds config: %s", failure);
                    failure.printStackTrace();
                });
    }

    WorldStore getWorldStore() {
        return worldStore;
    }

    private record KeyOrNameWithOptions<T>(WorldKeyOrName keyOrName, T options) { }
}

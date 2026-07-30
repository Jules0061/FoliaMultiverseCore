package org.mvplugins.multiverse.core;

import java.util.logging.Logger;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.anchor.AnchorManager;
import org.mvplugins.multiverse.core.destination.Destination;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.commands.CoreCommand;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.listeners.CoreListener;
import org.mvplugins.multiverse.core.inject.PluginServiceLocatorFactory;
import org.mvplugins.multiverse.core.module.MultiverseModule;
import org.mvplugins.multiverse.core.utils.MVScheduler;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.entity.SpawnCategoryMapper;
import org.mvplugins.multiverse.core.world.location.NullSpawnLocation;
import org.mvplugins.multiverse.core.world.location.SpawnLocation;
import org.mvplugins.multiverse.core.world.location.UnloadedWorldLocation;

@Service
public class MultiverseCore extends MultiverseModule {

    @Inject
    private Provider<CoreConfig> configProvider;
    @Inject
    private Provider<WorldManager> worldManagerProvider;
    @Inject
    private Provider<AnchorManager> anchorManagerProvider;
    @Inject
    private Provider<DestinationsProvider> destinationsProviderProvider;
    @Inject
    private Provider<BstatsMetricsConfigurator> metricsConfiguratorProvider;
    @Inject
    private Provider<MVEconomist> economistProvider;
    @Inject
    private Provider<MVScheduler> schedulerProvider;

    public MultiverseCore() {
        super();
    }

    @Override
    public void onLoad() {
        Logging.init(this);

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            Logging.severe("Failed to create data folder!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ConfigurationSerialization.registerClass(NullSpawnLocation.class);
        ConfigurationSerialization.registerClass(SpawnLocation.class);
        ConfigurationSerialization.registerClass(UnloadedWorldLocation.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        initializeDependencyInjection(new MultiverseCorePluginBinder(this));

        var config = configProvider.get();
        var loadSuccess = config.load().andThenTry(config::save).isSuccess();
        if (!loadSuccess || !config.isLoaded()) {
            Logging.severe("Your configs were not loaded.");
            Logging.severe("Please check your configs and restart the server.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Logging.setShowingConfig(shouldShowConfig());

        SpawnCategoryMapper.buildSpawnCategoryMap();

        schedulerProvider.get().runGlobalNow(this::initializeWorldsAndServices);
    }

    private void initializeWorldsAndServices() {
        worldManagerProvider.get().initAllWorlds().andThenTry(() -> {
            loadEconomist();
            loadAnchors();
            registerDynamicListeners(CoreListener.class);
            setUpLocales();
            registerCommands(CoreCommand.class);
            registerDestinations();
            setupMetrics();
            loadPlaceholderApiIntegration();
            loadApiService();
            saveAllConfigs();
            logEnableMessage();
        }).onFailure(e -> {
            Logging.severe("Failed to multiverse core! Disabling...");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MultiverseCoreApi.shutdown();
        shutdownDependencyInjection();
        PluginServiceLocatorFactory.get().shutdown();
        Logging.info("- Disabled");
        Logging.shutdown();
    }

    private boolean shouldShowConfig() {
        return !configProvider.get().getSilentStart();
    }

    private void loadEconomist() {
        Try.run(() -> economistProvider.get())
                .onFailure(e -> {
                    Logging.severe("Failed to load economy integration");
                    e.printStackTrace();
                });
    }

    private void loadAnchors() {
        Try.of(() -> anchorManagerProvider.get())
                .flatMap(AnchorManager::loadAnchors)
                .onFailure(e -> {
                    Logging.severe("Failed to load anchors");
                    e.printStackTrace();
                });
    }

    private void registerDestinations() {
        Try.of(() -> destinationsProviderProvider.get())
                .andThenTry(destinationsProvider -> {
                    serviceLocator.getAllServices(Destination.class)
                            .forEach(destinationsProvider::registerDestination);
                })
                .onFailure(e -> {
                    Logging.severe("Failed to register destinations");
                    e.printStackTrace();
                });
    }

    private void setupMetrics() {
        if (TestingMode.isDisabled()) {
            Try.of(() -> metricsConfiguratorProvider.get())
                    .onFailure(e -> {
                        Logging.severe("Failed to setup metrics");
                        e.printStackTrace();
                    });
        } else {
            Logging.info("Metrics are disabled in testing mode.");
        }
    }

    private void loadPlaceholderApiIntegration() {
        if (configProvider.get().isRegisterPapiHook()
                && getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Try.run(() -> serviceLocator.getService(PlaceholderExpansionHook.class))
                    .onFailure(e -> {
                        Logging.severe("Failed to load PlaceholderAPI integration.");
                        e.printStackTrace();
                    });
        }
    }

    private void loadApiService() {
        Try.run(() -> MultiverseCoreApi.init(this))
                .onSuccess(ignore -> Logging.info("API service loaded!"))
                .onFailure(e -> {
                    Logging.severe("Failed to load API service!");
                    e.printStackTrace();
                });
    }

    private Try<Void> saveAllConfigs() {
        return configProvider.get().save()
                .andThenTry(() -> worldManagerProvider.get().saveWorldsConfig())
                .andThenTry(() -> anchorManagerProvider.get().saveAllAnchors())
                .onFailure(e ->
                        Logging.severe("Failed to save all configs, things may not work as expected. %s",
                                e.getLocalizedMessage()));
    }

    private void logEnableMessage() {
        Logging.config("\u001B[32mVersion %s (API v%s) Enabled - By %s\u001B[0m",
                this.getDescription().getVersion(), getVersionAsNumber(), StringFormatter.joinAnd(getDescription().getAuthors()));

        if (configProvider.get().isShowingDonateMessage()) {
            Logging.config("\u001B[32mLoving Multiverse-Core? Please consider supporting the project with a " +
                    "small donation: https://github.com/sponsors/Multiverse\u001B[0m");
        }
    }

    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public MultiverseCoreApi getApi() {
        return MultiverseCoreApi.get();
    }

    @Override
    public double getTargetCoreVersion() {
        return getVersionAsNumber();
    }

    @NotNull
    @Override
    public Logger getLogger() {
        return Logging.getLogger();
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        CoreConfig coreConfig = this.configProvider.get();
        var config = coreConfig.getConfig();
        if (config != null && coreConfig.isLoaded()) {
            return config;
        }

        var loadSuccess = coreConfig.load().isSuccess();
        if (!loadSuccess || !coreConfig.isLoaded()) {
            throw new RuntimeException("Failed to load configs");
        }
        return coreConfig.getConfig();
    }

    @Override
    public void reloadConfig() {
        this.configProvider.get().load();
    }

    @Override
    public void saveConfig() {
        this.configProvider.get().save();
    }
}

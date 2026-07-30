package org.mvplugins.multiverse.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.anchor.AnchorManager;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.core.teleportation.BlockSafety;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.biomeprovider.BiomeProviderFactory;
import org.mvplugins.multiverse.core.world.generators.GeneratorProvider;

public final class MultiverseCoreApi {

    private static MultiverseCoreApi instance;
    private static final List<Consumer<MultiverseCoreApi>> WHEN_LOADED_CALLBACKS = new ArrayList<>();

    static void init(@NotNull MultiverseCore multiverseCore) {
        if (instance != null) {
            throw new IllegalStateException("MultiverseCoreApi has already been initialized!");
        }
        instance = new MultiverseCoreApi(multiverseCore.getServiceLocator());
        Bukkit.getServicesManager().register(MultiverseCoreApi.class, instance, multiverseCore, ServicePriority.Normal);

        List<Consumer<MultiverseCoreApi>> callbacks = List.copyOf(WHEN_LOADED_CALLBACKS);
        WHEN_LOADED_CALLBACKS.clear();
        MultiverseCoreApi loadedApi = instance;
        callbacks.forEach(callback -> runLoadCallback(callback, loadedApi));
    }

    private static void runLoadCallback(
            @NotNull Consumer<MultiverseCoreApi> callback,
            @NotNull MultiverseCoreApi loadedApi) {
        Try.run(() -> callback.accept(loadedApi))
                .onFailure(exception -> Logging.warning(
                        "A Multiverse-Core API load callback failed: %s", exception.getMessage()));
    }

    static void shutdown() {
        if (instance == null) {
            return;
        }
        Bukkit.getServicesManager().unregister(instance);
        instance = null;
    }

    @ApiStatus.AvailableSince("5.1")
    public static void whenLoaded(@NotNull Consumer<MultiverseCoreApi> consumer) {
        if (instance != null) {
            consumer.accept(instance);
        } else {
            WHEN_LOADED_CALLBACKS.add(consumer);
        }
    }

    @ApiStatus.AvailableSince("5.1")
    public static boolean isLoaded() {
        return instance != null;
    }

    public static @NotNull MultiverseCoreApi get() {
        if (instance == null) {
            throw new IllegalStateException("MultiverseCoreApi has not been initialized!");
        }
        return instance;
    }

    private final PluginServiceLocator serviceLocator;

    private MultiverseCoreApi(@NotNull PluginServiceLocator serviceProvider) {
        this.serviceLocator = serviceProvider;
    }

    public @NotNull AnchorManager getAnchorManager() {
        return Objects.requireNonNull(serviceLocator.getActiveService(AnchorManager.class));
    }

    public @NotNull BiomeProviderFactory getBiomeProviderFactory() {
        return Objects.requireNonNull(serviceLocator.getActiveService(BiomeProviderFactory.class));
    }

    public @NotNull BlockSafety getBlockSafety() {
        return Objects.requireNonNull(serviceLocator.getActiveService(BlockSafety.class));
    }

    public @NotNull CoreConfig getCoreConfig() {
        return Objects.requireNonNull(serviceLocator.getActiveService(CoreConfig.class));
    }

    public @NotNull DestinationsProvider getDestinationsProvider() {
        return Objects.requireNonNull(serviceLocator.getActiveService(DestinationsProvider.class));
    }

    public @NotNull GeneratorProvider getGeneratorProvider() {
        return Objects.requireNonNull(serviceLocator.getActiveService(GeneratorProvider.class));
    }

    public @NotNull LocationManipulation getLocationManipulation() {
        return Objects.requireNonNull(serviceLocator.getActiveService(LocationManipulation.class));
    }

    public @NotNull MVEconomist getMVEconomist() {
        return Objects.requireNonNull(serviceLocator.getActiveService(MVEconomist.class));
    }

    public @NotNull AsyncSafetyTeleporter getSafetyTeleporter() {
        return Objects.requireNonNull(serviceLocator.getActiveService(AsyncSafetyTeleporter.class));
    }

    public @NotNull WorldManager getWorldManager() {
        return Objects.requireNonNull(serviceLocator.getActiveService(WorldManager.class));
    }

    public @NotNull PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }
}

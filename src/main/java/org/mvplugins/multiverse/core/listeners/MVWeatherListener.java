package org.mvplugins.multiverse.core.listeners;

import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.world.WorldManager;

@Service
final class MVWeatherListener implements CoreListener {

    private final WorldManager worldManager;

    @Inject
    MVWeatherListener(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventMethod
    void weatherChange(WeatherChangeEvent event) {
        if (event.isCancelled() || !event.toWeatherState()) {
            return;
        }
        worldManager.getLoadedWorld(event.getWorld())
                .peek(world -> {
                    if (!world.isAllowWeather()) {
                        Logging.fine("Cancelling weather for %s as getAllowWeather is false", world.getName());
                        event.setCancelled(true);
                    }
                });
    }

    @EventMethod
    void thunderChange(ThunderChangeEvent event) {
        if (event.isCancelled() || !event.toThunderState()) {
            return;
        }
        worldManager.getLoadedWorld(event.getWorld())
                .peek(world -> {
                    if (!world.isAllowWeather()) {
                        Logging.fine("Cancelling thunder for %s as getAllowWeather is false", world.getName());
                        event.setCancelled(true);
                    }
                });
    }
}

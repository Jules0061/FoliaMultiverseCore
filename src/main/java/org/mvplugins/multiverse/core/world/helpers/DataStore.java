package org.mvplugins.multiverse.core.world.helpers;

import java.util.HashMap;
import java.util.Map;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.utils.compatibility.WorldBorderCompatibility;
import org.mvplugins.multiverse.core.utils.compatibility.GameRuleCompatibility;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

@Service
public interface DataStore<T> {
    DataStore<T> copyFrom(T object);

    DataStore<T> pasteTo(T object);

    class GameRulesStore implements DataStore<LoadedMultiverseWorld> {
        private Map<GameRule<?>, Object> gameRuleMap;

        @Override
        public GameRulesStore copyFrom(LoadedMultiverseWorld world) {
            this.gameRuleMap = new HashMap<>();
            world.getBukkitWorld().peek(bukkitWorld -> {
                for (String gameRule : bukkitWorld.getGameRules()) {
                    GameRule<?> gameRuleEnum = GameRuleCompatibility.getByName(gameRule);
                    if (gameRuleEnum == null) {
                        continue;
                    }
                    gameRuleMap.put(gameRuleEnum, bukkitWorld.getGameRuleValue(gameRuleEnum));
                }
            });
            Logging.finer("Copied " + gameRuleMap.size() + " game rules from world " + world.getName());
            return this;
        }

        @Override
        public GameRulesStore pasteTo(LoadedMultiverseWorld world) {
            if (gameRuleMap == null) {
                return this;
            }
            world.getBukkitWorld().peek(bukkitWorld -> {
                for (String gameRule : bukkitWorld.getGameRules()) {
                    GameRule<?> gameRuleEnum = GameRuleCompatibility.getByName(gameRule);
                    if (gameRuleEnum == null) {
                        continue;
                    }
                    setGameRuleValue(bukkitWorld, gameRuleEnum, gameRuleMap.get(gameRuleEnum)).onFailure(e -> {
                        Logging.warning("Failed to set game rule " + GameRuleCompatibility.getName(gameRuleEnum)
                                + " to " + gameRuleMap.get(gameRuleEnum));
                        e.printStackTrace();
                    });
                }
            });
            return this;
        }

        private <T> Try<Void> setGameRuleValue(World world, GameRule<T> gameRule, Object value) {
            return Try.run(() -> world.setGameRule(gameRule, (T) value));
        }
    }

    class WorldConfigStore<T extends MultiverseWorld> implements DataStore<T> {
        private Map<String, Object> configMap;

        @Override
        public WorldConfigStore<T> copyFrom(T world) {
            this.configMap = new HashMap<>();
            StringPropertyHandle worldPropertyHandler = world.getStringPropertyHandle();
            worldPropertyHandler.getAllPropertyNames().forEach(name -> worldPropertyHandler.getProperty(name)
                    .peek(value -> configMap.put(name, value)).onFailure(e ->
                            Logging.warning("Failed to get property " + name + " from world "
                                    + world.getName() + ": " + e.getMessage())));
            return this;
        }

        @Override
        public WorldConfigStore<T> pasteTo(T world) {
            if (configMap == null) {
                return this;
            }
            StringPropertyHandle worldPropertyHandler = world.getStringPropertyHandle();
            configMap.forEach((name, value) -> worldPropertyHandler.setProperty(name, value).onFailure(e ->
                    Logging.warning("Failed to set property %s to %s for world %s: %s",
                            name, value, world.getName(), e.getMessage())));
            return this;
        }
    }

    class WorldBorderStore implements DataStore<LoadedMultiverseWorld> {
        private double borderCenterX;
        private double borderCenterZ;
        private double borderDamageAmount;
        private double borderDamageBuffer;
        private double borderSize;
        private int borderTimeRemaining;

        @Override
        public WorldBorderStore copyFrom(LoadedMultiverseWorld world) {
            world.getBukkitWorld()
                    .map(World::getWorldBorder)
                    .peek(worldBorder -> {
                        borderCenterX = worldBorder.getCenter().getX();
                        borderCenterZ = worldBorder.getCenter().getZ();
                        borderDamageAmount = worldBorder.getDamageAmount();
                        borderDamageBuffer = worldBorder.getDamageBuffer();
                        borderSize = worldBorder.getSize();
                        borderTimeRemaining = WorldBorderCompatibility.getWarningTimeTicks(worldBorder);
                    });
            return this;
        }

        @Override
        public WorldBorderStore pasteTo(LoadedMultiverseWorld world) {
            world.getBukkitWorld()
                    .map(World::getWorldBorder)
                    .peek(worldBorder -> {
                        worldBorder.setCenter(borderCenterX, borderCenterZ);
                        worldBorder.setDamageAmount(borderDamageAmount);
                        worldBorder.setDamageBuffer(borderDamageBuffer);
                        worldBorder.setSize(borderSize);
                        WorldBorderCompatibility.setWarningTimeTicks(worldBorder, borderTimeRemaining);
                    });
            return this;
        }
    }
}

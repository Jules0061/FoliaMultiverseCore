package org.mvplugins.multiverse.core.listeners;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.dynamiclistener.EventRunnable;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventClass;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.entity.EntitySpawnConfig;

@Service
final class MVEntityListener implements CoreListener {
    private final WorldManager worldManager;

    @Inject
    MVEntityListener(@NotNull WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventMethod
    void foodLevelChange(FoodLevelChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        worldManager.getLoadedWorld(player.getWorld())
                .peek(world -> {
                    if (!world.isHunger() && event.getFoodLevel() < player.getFoodLevel()) {
                        event.setCancelled(true);
                    }
                });
    }

    @EventMethod
    void entityRegainHealth(EntityRegainHealthEvent event) {
        if (event.isCancelled() || event.getRegainReason() != RegainReason.REGEN) {
            return;
        }

        worldManager.getLoadedWorld(event.getEntity().getWorld())
                .peek(world -> {
                    if (!world.getAutoHeal()) {
                        event.setCancelled(true);
                    }
                });
    }

    @EventClass("com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent")
    EventRunnable<?> preCreatureSpawn() {
        return new EventRunnable<PreCreatureSpawnEvent>() {
            @Override
            public void onEvent(PreCreatureSpawnEvent event) {
                if (event.getReason() == SpawnReason.CUSTOM
                        || event.getReason() == SpawnReason.COMMAND
                        || event.getReason() == SpawnReason.BREEDING
                        || event.getReason() == SpawnReason.SPAWNER_EGG) {
                    return;
                }

                worldManager.getLoadedWorld(event.getSpawnLocation().getWorld())
                        .peek(world -> {
                            if (!world.getEntitySpawnConfig().shouldAllowSpawn(event.getType())) {
                                Logging.finest("Cancelling Pre Creature Spawn Event for: " + event.getType());
                                event.setCancelled(true);
                                event.setShouldAbortSpawn(true);
                            }
                        });
            }
        };
    }

    @EventMethod
    void creatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getSpawnReason() == SpawnReason.CUSTOM
                || event.getSpawnReason() == SpawnReason.COMMAND
                || event.getSpawnReason() == SpawnReason.BREEDING
                || event.getSpawnReason() == SpawnReason.SPAWNER_EGG) {
            return;
        }

        worldManager.getLoadedWorld(event.getEntity().getWorld())
                .peek(world -> {
                    if (!world.getEntitySpawnConfig().shouldAllowSpawn(event.getEntityType())) {
                        Logging.finest("Cancelling Creature Spawn Event for: " + event.getEntity());
                        event.setCancelled(true);
                    }
                });
    }

    @EventMethod
    void entitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            return;
        }

        worldManager.getLoadedWorld(event.getEntity().getWorld())
                .peek(world -> {
                    if (!world.getEntitySpawnConfig().shouldAllowSpawn(event.getEntityType())) {
                        Logging.finest("Cancelling Entity Spawn Event for: " + event.getEntity());
                        event.setCancelled(true);
                    }
                });
    }

    @EventMethod
    void chunkLoad(ChunkLoadEvent event) {
        LoadedMultiverseWorld world = worldManager.getLoadedWorld(event.getWorld()).getOrNull();
        if (world == null) {
            return;
        }
        Chunk chunk = event.getChunk();
        Entity[] entities = chunk.getEntities();
        if (entities.length == 0) {
            return;
        }
        EntitySpawnConfig spawnConfig = world.getEntitySpawnConfig();
        int count = 0;
        for (Entity entity : entities) {
            if (entity instanceof Player || spawnConfig.shouldAllowSpawn(entity)) {
                continue;
            }
            entity.remove();
            count++;
        }
        if (count > 0) {
            Logging.finest("Removed %d entities from chunk %d, %d in world %s due to spawn category settings.",
                    count, chunk.getX(), chunk.getZ(), event.getWorld().getName());
        }
    }
}

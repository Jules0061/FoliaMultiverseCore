package org.mvplugins.multiverse.core.world.entity;

import jakarta.inject.Inject;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.utils.MVScheduler;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@Service
public final class EntityPurger {

    private final MVScheduler scheduler;

    @Inject
    EntityPurger(@NotNull MVScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public CompletableFuture<Integer> purgeEntities(LoadedMultiverseWorld world) {
        EntitySpawnConfig spawnConfig = world.getEntitySpawnConfig();
        return purgeEntitiesWithCondition(world, entity -> !spawnConfig.shouldAllowSpawn(entity));
    }

    public CompletableFuture<Integer> purgeEntities(LoadedMultiverseWorld world, SpawnCategory spawnCategory) {
        return purgeEntitiesWithCondition(world, entity -> entity.getSpawnCategory().equals(spawnCategory));
    }

    public CompletableFuture<Integer> purgeEntities(LoadedMultiverseWorld world, SpawnCategory... spawnCategories) {
        Set<SpawnCategory> spawnCategoriesSet = Set.of(spawnCategories);
        return purgeEntitiesWithCondition(world, entity -> spawnCategoriesSet.contains(entity.getSpawnCategory()));
    }

    public CompletableFuture<Integer> purgeAllEntities(LoadedMultiverseWorld world) {
        return purgeEntitiesWithCondition(world, entity -> true);
    }

    private CompletableFuture<Integer> purgeEntitiesWithCondition(
            LoadedMultiverseWorld world, Predicate<Entity> condition) {
        World bukkitWorld = world.getBukkitWorld().getOrNull();
        if (bukkitWorld == null) {
            return CompletableFuture.completedFuture(0);
        }
        if (!MVScheduler.isRegionisedServer()) {
            return CompletableFuture.completedFuture(removeMatching(bukkitWorld.getEntities(), condition));
        }
        CompletableFuture<Integer> result = new CompletableFuture<>();
        scheduler.runGlobalNow(() -> purgeByChunk(bukkitWorld, condition, result));
        return result;
    }

    private void purgeByChunk(World bukkitWorld, Predicate<Entity> condition, CompletableFuture<Integer> result) {
        Chunk[] chunks = bukkitWorld.getLoadedChunks();
        if (chunks.length == 0) {
            result.complete(0);
            return;
        }
        AtomicInteger removed = new AtomicInteger();
        AtomicInteger pending = new AtomicInteger(chunks.length);
        for (Chunk chunk : chunks) {
            scheduler.runAtChunk(bukkitWorld, chunk.getX(), chunk.getZ(), () -> {
                if (chunk.isLoaded()) {
                    removed.addAndGet(removeMatching(chunk.getEntities(), condition));
                }
                if (pending.decrementAndGet() == 0) {
                    result.complete(removed.get());
                }
            });
        }
    }

    private int removeMatching(List<Entity> entities, Predicate<Entity> condition) {
        int removed = 0;
        for (int i = 0, size = entities.size(); i < size; i++) {
            if (removeIfMatching(entities.get(i), condition)) {
                removed++;
            }
        }
        return removed;
    }

    private int removeMatching(Entity[] entities, Predicate<Entity> condition) {
        int removed = 0;
        for (Entity entity : entities) {
            if (removeIfMatching(entity, condition)) {
                removed++;
            }
        }
        return removed;
    }

    private boolean removeIfMatching(Entity entity, Predicate<Entity> condition) {
        if (entity instanceof Player || !condition.test(entity)) {
            return false;
        }
        entity.remove();
        return true;
    }
}

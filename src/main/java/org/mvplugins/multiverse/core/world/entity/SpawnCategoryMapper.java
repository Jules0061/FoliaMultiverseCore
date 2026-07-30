package org.mvplugins.multiverse.core.world.entity;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.SpawnCategory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.utils.ReflectHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public final class SpawnCategoryMapper {

    private static final @NotNull Map<EntityType, SpawnCategory> entityTypeToSpawnCategoryMap;
    private static final @NotNull Map<SpawnCategory, List<EntityType>> spawnCategoryMap;

    static {
        entityTypeToSpawnCategoryMap = new HashMap<>();
        spawnCategoryMap = new HashMap<>();
    }

    public static void buildSpawnCategoryMap() {
        Class<?> entityTypeClass = ReflectHelper.tryGetClass("net.minecraft.world.entity.EntityType").getOrNull();
        if (entityTypeClass == null) {
            Logging.warning("Failed to find EntityType class. SpawnCategoryMapper will not work.");
            return;
        }
        Class<?> entityTypesListClass = ReflectHelper.tryGetClass("net.minecraft.world.entity.EntityTypes").getOrNull();
        if (entityTypesListClass == null) {
            Logging.finer("Failed to find EntityTypes class. falling back to EntityType.");
            entityTypesListClass = entityTypeClass;
        }
        Method getCategoryMethod = ReflectHelper.tryGetMethod(entityTypeClass, "getCategory").getOrNull();
        if (getCategoryMethod == null) {
            Logging.warning("Failed to find getCategory method. SpawnCategoryMapper will not work.");
            return;
        }
        Class<?> craftSpawnCategoryClass = ReflectHelper.tryGetClass("org.bukkit.craftbukkit.util.CraftSpawnCategory").getOrNull();
        if (craftSpawnCategoryClass == null) {
            Logging.warning("Failed to find CraftSpawnCategory class. SpawnCategoryMapper will not work.");
            return;
        }
        Method toBukkitMethod = Arrays.stream(craftSpawnCategoryClass.getMethods())
                .filter(method -> method.getName().equals("toBukkit"))
                .findFirst()
                .orElse(null);
        if (toBukkitMethod == null) {
            Logging.warning("Failed to find toBukkit method. SpawnCategoryMapper will not work.");
            return;
        }

        Logging.finer("Mapping entities to its spawn category...");

        Arrays.stream(entityTypesListClass.getFields()).forEach(entityTypeField -> Try.of(() -> EntityType.valueOf(entityTypeField.getName()))
                .peek(entityType -> ReflectHelper.tryGetStaticFieldValue(entityTypeField, entityTypeClass)
                        .flatMap(nmsEntityType -> ReflectHelper.tryInvokeMethod(nmsEntityType, getCategoryMethod))
                        .flatMap(nsmMobCategory -> ReflectHelper.tryInvokeStaticMethod(toBukkitMethod, nsmMobCategory))
                        .filter(bukkitSpawnCategory -> bukkitSpawnCategory instanceof SpawnCategory)
                        .map(bukkitSpawnCategory -> (SpawnCategory) bukkitSpawnCategory)
                        .peek(bukkitSpawnCategory -> entityTypeToSpawnCategoryMap.put(entityType, bukkitSpawnCategory))
                        .peek(bukkitSpawnCategory -> spawnCategoryMap
                                .computeIfAbsent(bukkitSpawnCategory, ignore -> new ArrayList<>())
                                .add(entityType))
                        .onFailure(throwable -> Logging.warning("Failed to map entity type "
                                + entityTypeField.getName() + " to its spawn category. " + throwable.getLocalizedMessage())))
                .onFailure(throwable -> Logging.fine("Failed to map entity type "
                        + entityTypeField.getName() + " to its spawn category. " + throwable.getLocalizedMessage())));

        if (entityTypeToSpawnCategoryMap.isEmpty()) {
            Logging.warning("entityTypeToSpawnCategoryMap map is empty after building... " +
                    "SpawnCategoryMapper will not work.");
        }
        if (spawnCategoryMap.isEmpty()) {
            Logging.warning("spawnCategoryMap map is empty after building... " +
                    "SpawnCategoryMapper will not work.");
        }

        Logging.finer("Mapped " + entityTypeToSpawnCategoryMap.size() + " entities to its spawn category.");
    }

    static @NotNull Option<SpawnCategory> getSpawnCategory(@Nullable EntityType entityType) {
        return Option.of(entityTypeToSpawnCategoryMap.get(entityType));
    }

    static @NotNull List<EntityType> getEntityTypes(@Nullable SpawnCategory spawnCategory) {
        return spawnCategoryMap.getOrDefault(spawnCategory, Collections.emptyList());
    }

    private SpawnCategoryMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

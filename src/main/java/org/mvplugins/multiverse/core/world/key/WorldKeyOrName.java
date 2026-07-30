package org.mvplugins.multiverse.core.world.key;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.locale.message.LocalizableMessage;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement;
import org.mvplugins.multiverse.core.utils.ServerProperties;
import org.mvplugins.multiverse.core.utils.compatibility.UnsafeValuesCompatibility;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.helpers.DimensionFinder;

import java.util.Locale;
import java.util.Objects;

@ApiStatus.AvailableSince("5.7")
public sealed abstract class WorldKeyOrName implements Comparable<WorldKeyOrName>, LocalizableMessage permits WorldKeyOrName.Key, WorldKeyOrName.Name {

    private static final String DEFAULT_OVERWORLD_KEY = "overworld";
    private static final String DEFAULT_NETHER_KEY = "the_nether";
    private static final String DEFAULT_END_KEY = "the_end";

    @ApiStatus.AvailableSince("5.7")
    public static Attempt<WorldKeyOrName, WorldKeyParseFailReason> parse(@Nullable String nameOrKey) {
        if (nameOrKey == null || nameOrKey.isEmpty()) {
            return Attempt.failure(WorldKeyParseFailReason.EMPTY);
        }
        return nameOrKey.contains(":") ? parseKey(nameOrKey) : parseName(nameOrKey);
    }

    @ApiStatus.AvailableSince("5.7")
    public static Attempt<WorldKeyOrName, WorldKeyParseFailReason> parseName(@NotNull String name) {
        return Try.of(() -> NamespacedKey.minecraft(mapWorldNameToMinecraftKey(name)))
                .map(usableKey -> Attempt.<WorldKeyOrName, WorldKeyParseFailReason>success(new Name(name, usableKey)))
                .recover(throwable -> Attempt.failure(WorldKeyParseFailReason.INVALID_WORLD_NAME,
                        MessageReplacement.Replace.WORLD.with(name)))
                .getOrElse(() -> Attempt.failure(WorldKeyParseFailReason.INVALID_WORLD_NAME,
                        MessageReplacement.Replace.WORLD.with(name)));
    }

    private static String mapWorldNameToMinecraftKey(@NotNull String nameOrKey) {
        String defaultLevelName = getMostAccurateLevelName();
        String lowerCaseName = nameOrKey.toLowerCase(Locale.ROOT);
        if (defaultLevelName.equalsIgnoreCase(lowerCaseName)) {
            lowerCaseName = DEFAULT_OVERWORLD_KEY;
        } else if (DimensionFinder.DEFAULT_NETHER_FORMAT.replaceOverworld(defaultLevelName).equalsIgnoreCase(lowerCaseName)) {
            lowerCaseName = DEFAULT_NETHER_KEY;
        } else if (DimensionFinder.DEFAULT_END_FORMAT.replaceOverworld(defaultLevelName).equalsIgnoreCase(lowerCaseName)) {
            lowerCaseName = DEFAULT_END_KEY;
        }
        return lowerCaseName;
    }

    @ApiStatus.AvailableSince("5.7")
    public static Attempt<WorldKeyOrName, WorldKeyParseFailReason> parseKey(@NotNull String nameOrKey) {
        return Option.of(NamespacedKey.fromString(nameOrKey))
                .filter(Objects::nonNull)
                .map(key -> Attempt.<WorldKeyOrName, WorldKeyParseFailReason>success(new Key(key, usableNameFromKey(key))))
                .getOrElse(() -> Attempt.failure(WorldKeyParseFailReason.INVALID_NAMESPACED_KEY,
                        MessageReplacement.Replace.NAMESPACE.with(nameOrKey)));
    }

    @ApiStatus.AvailableSince("5.7")
    public static WorldKeyOrName parseKey(@NotNull NamespacedKey key) {
        return new Key(key,  usableNameFromKey(key));
    }

    private static String usableNameFromKey(@NotNull NamespacedKey key) {
        return key.getNamespace().equals(NamespacedKey.MINECRAFT)
                ? mapMinecraftKeyToWorldName(key)
                : mapCustomKeyToWorldName(key);
    }

    private static String mapMinecraftKeyToWorldName(@NotNull NamespacedKey key) {
        String defaultLevelName = getMostAccurateLevelName();
        return switch (key.getKey()) {
            case DEFAULT_OVERWORLD_KEY -> defaultLevelName;
            case DEFAULT_NETHER_KEY -> DimensionFinder.DEFAULT_NETHER_FORMAT.replaceOverworld(defaultLevelName);
            case DEFAULT_END_KEY -> DimensionFinder.DEFAULT_END_FORMAT.replaceOverworld(defaultLevelName);
            default -> key.getKey();
        };
    }

    private static String mapCustomKeyToWorldName(@NotNull NamespacedKey key) {
        return key.getNamespace() + "_" + key.getKey();
    }

    private static String getMostAccurateLevelName() {
        return UnsafeValuesCompatibility.getMainLevelName()
                .orElse(ServerProperties::getStaticLevelName)
                .getOrElse("world");
    }

    @ApiStatus.AvailableSince("5.7")
    public abstract boolean isName();

    @ApiStatus.AvailableSince("5.7")
    public abstract boolean isKey();

    @ApiStatus.AvailableSince("5.7")
    public abstract @NotNull Option<NamespacedKey> getKey();

    @ApiStatus.AvailableSince("5.7")
    public abstract @NotNull Option<String> getName();

    @ApiStatus.AvailableSince("5.7")
    public abstract @NotNull Either<NamespacedKey, String> asEither();

    @ApiStatus.AvailableSince("5.7")
    public abstract @NotNull NamespacedKey usableKey();

    @ApiStatus.AvailableSince("5.7")
    public abstract @NotNull String usableName();

    @ApiStatus.AvailableSince("5.7")
    public abstract @NotNull String serialise();

    @Override
    public int compareTo(WorldKeyOrName o) {
        return serialise().compareTo(o.serialise());
    }

    @Override
    public @Nullable Message getLocalizableMessage() {
        return Message.of(serialise());
    }

    public static final class Key extends WorldKeyOrName {

        private final NamespacedKey key;
        private final String usableName;

        private Key(@NotNull NamespacedKey key, @NotNull String usableName) {
            this.key = key;
            this.usableName = usableName;
        }

        @Override
        public boolean isName() {
            return false;
        }

        @Override
        public boolean isKey() {
            return true;
        }

        @Override
        public @NotNull Option<NamespacedKey> getKey() {
            return Option.of(key);
        }

        @Override
        public @NotNull Option<String> getName() {
            return Option.none();
        }

        @Override
        public @NotNull Either<NamespacedKey, String> asEither() {
            return Either.left(key);
        }

        @Override
        public @NotNull NamespacedKey usableKey() {
            return key;
        }

        @Override
        public @NotNull String usableName() {
            return usableName;
        }

        @Override
        public @NotNull String serialise() {
            return key.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Key key1 = (Key) o;
            return Objects.equals(key, key1.key);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }

        @Override
        public String toString() {
            return "Key{" +
                    "key=" + key +
                    ", usableName='" + usableName + '\'' +
                    '}';
        }
    }

    public static final class Name extends WorldKeyOrName {

        private final String name;
        private final NamespacedKey usableKey;

        private Name(String name, NamespacedKey usableKey) {
            this.name = name;
            this.usableKey = usableKey;
        }

        @Override
        public boolean isName() {
            return true;
        }

        @Override
        public boolean isKey() {
            return false;
        }

        @Override
        public @NotNull Option<NamespacedKey> getKey() {
            return Option.none();
        }

        @Override
        public @NotNull Option<String> getName() {
            return Option.of(name);
        }

        @Override
        public @NotNull Either<NamespacedKey, String> asEither() {
            return Either.right(name);
        }

        @Override
        public @NotNull NamespacedKey usableKey() {
            return usableKey;
        }

        @Override
        public @NotNull String usableName() {
            return name;
        }

        @Override
        public @NotNull String serialise() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Name name1 = (Name) o;
            return Objects.equals(name, name1.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "Name{" +
                    "name='" + name + '\'' +
                    ", usableKey=" + usableKey +
                    '}';
        }
    }
}

package org.mvplugins.multiverse.core.config.node;

import io.vavr.control.Try;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ApiStatus.AvailableSince("5.7")
public interface MapValueNode<K, V> extends ValueNode<Map<K, V>> {

    @ApiStatus.AvailableSince("5.7")
    @NotNull Try<Void> validateKey(@Nullable K key);

    @ApiStatus.AvailableSince("5.7")
    @NotNull Try<Void> validateValue(@Nullable V value);

    @ApiStatus.AvailableSince("5.7")
    @NotNull Try<Void> validateEntry(@Nullable K key, @Nullable V value);

    @ApiStatus.AvailableSince("5.7")
    @NotNull Map.Entry<K, V> deserializeEntry(@Nullable Object key, @Nullable Object value);

    @ApiStatus.AvailableSince("5.7")
    @NotNull Map.Entry<Object, Object> serializeEntry(@Nullable K key, @Nullable V value);
}

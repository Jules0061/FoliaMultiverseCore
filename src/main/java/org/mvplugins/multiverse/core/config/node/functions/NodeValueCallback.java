package org.mvplugins.multiverse.core.config.node.functions;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("5.4")
@FunctionalInterface
public interface NodeValueCallback<T> {
    @ApiStatus.AvailableSince("5.4")
    void run(T value);

    @ApiStatus.AvailableSince("5.4")
    default NodeValueCallback<T> then(NodeValueCallback<T> after) {
        return (T value) -> {
            this.run(value);
            after.run(value);
        };
    }
}

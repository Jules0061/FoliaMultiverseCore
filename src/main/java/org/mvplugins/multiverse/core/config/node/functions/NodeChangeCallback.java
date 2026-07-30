package org.mvplugins.multiverse.core.config.node.functions;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("5.4")
@FunctionalInterface
public interface NodeChangeCallback<T> {
    @ApiStatus.AvailableSince("5.4")
    void run(T oldValue, T newValue);

    @ApiStatus.AvailableSince("5.4")
    default void run(CommandSender sender, T oldValue, T newValue) {
        run(oldValue, newValue);
    }

    @ApiStatus.AvailableSince("5.4")
    default NodeChangeCallback<T> then(NodeChangeCallback<T> after) {
        return new SenderNodeChangeCallback<T>() {
            @Override
            public void run(CommandSender sender, T oldValue, T newValue) {
                NodeChangeCallback.this.run(sender, oldValue, newValue);
                after.run(sender, oldValue, newValue);
            }

            @Override
            public void run(T oldValue, T newValue) {
                NodeChangeCallback.this.run(oldValue, newValue);
                after.run(oldValue, newValue);
            }
        };
    }
}

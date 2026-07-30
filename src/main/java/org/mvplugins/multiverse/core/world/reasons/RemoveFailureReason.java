package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;
import org.mvplugins.multiverse.core.world.WorldManager;

public enum RemoveFailureReason implements FailureReason {
    @Deprecated(forRemoval = true, since = "5.2")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    WORLD_NON_EXISTENT(MVCorei18n.REMOVEWORLD_WORLDNONEXISTENT),

    UNLOAD_FAILED(MVCorei18n.GENERIC_FAILURE);

    private final MessageKeyProvider message;

    RemoveFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

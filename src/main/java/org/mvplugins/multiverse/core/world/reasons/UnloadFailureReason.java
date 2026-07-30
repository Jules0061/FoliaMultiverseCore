package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum UnloadFailureReason implements FailureReason {
    WORLD_ALREADY_UNLOADING(MVCorei18n.UNLOADWORLD_WORLDALREADYUNLOADING),

    WORLD_NON_EXISTENT(MVCorei18n.UNLOADWORLD_WORLDNONEXISTENT),

    WORLD_UNLOADED(MVCorei18n.UNLOADWORLD_WORLDUNLOADED),

    BUKKIT_UNLOAD_FAILED(MVCorei18n.UNLOADWORLD_BUKKITUNLOADFAILED);

    private final MessageKeyProvider message;

    UnloadFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

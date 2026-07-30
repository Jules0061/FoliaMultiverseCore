package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum CloneFailureReason implements FailureReason {
    INVALID_WORLDNAME(MVCorei18n.CLONEWORLD_INVALIDWORLDNAME),

    @ApiStatus.AvailableSince("5.7")
    NAMESPACEDKEY_UNSUPPORTED(MVCorei18n.WORLDKEYPARSE_NAMESPACEDKEYUNSUPPORTED),

    WORLD_EXIST_FOLDER(MVCorei18n.CLONEWORLD_WORLDEXISTFOLDER),

    WORLD_EXIST_UNLOADED(MVCorei18n.CLONEWORLD_WORLDEXISTUNLOADED),

    WORLD_EXIST_LOADED(MVCorei18n.CLONEWORLD_WORLDEXISTLOADED),

    @ApiStatus.AvailableSince("5.6")
    FROM_WORLD_FOLDER_INVALID(MVCorei18n.CLONEWORLD_FROMWORLDFOLDERINVALID),

    COPY_FAILED(MVCorei18n.CLONEWORLD_COPYFAILED),

    IMPORT_FAILED(MVCorei18n.GENERIC_FAILURE);

    private final MessageKeyProvider message;

    CloneFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

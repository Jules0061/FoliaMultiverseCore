package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum CreateFailureReason implements FailureReason {
    INVALID_WORLDNAME(MVCorei18n.CREATEWORLD_INVALIDWORLDNAME),

    @ApiStatus.AvailableSince("5.7")
    NAMESPACEDKEY_UNSUPPORTED(MVCorei18n.WORLDKEYPARSE_NAMESPACEDKEYUNSUPPORTED),

    WORLD_EXIST_FOLDER(MVCorei18n.CREATEWORLD_WORLDEXISTFOLDER),

    WORLD_EXIST_UNLOADED(MVCorei18n.CREATEWORLD_WORLDEXISTUNLOADED),

    WORLD_EXIST_LOADED(MVCorei18n.CREATEWORLD_WORLDEXISTLOADED),

    WORLD_CREATOR_FAILED(MVCorei18n.GENERIC_FAILURE),
    ;

    private final MessageKeyProvider message;

    CreateFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum ImportFailureReason implements FailureReason {
    INVALID_WORLDNAME(MVCorei18n.IMPORTWORLD_INVALIDWORLDNAME),

    @ApiStatus.AvailableSince("5.7")
    NAMESPACEDKEY_UNSUPPORTED(MVCorei18n.WORLDKEYPARSE_NAMESPACEDKEYUNSUPPORTED),

    WORLD_FOLDER_INVALID(MVCorei18n.IMPORTWORLD_WORLDFOLDERINVALID),

    WORLD_EXIST_UNLOADED(MVCorei18n.IMPORTWORLD_WORLDEXISTUNLOADED),

    WORLD_EXIST_LOADED(MVCorei18n.IMPORTWORLD_WORLDEXISTLOADED),

    @ApiStatus.AvailableSince("5.2")
    BUKKIT_ENVIRONMENT_MISMATCH(MVCorei18n.IMPORTWORLD_BUKKITENVIRONMENTMISMATCH),

    WORLD_CREATOR_FAILED(MVCorei18n.GENERIC_FAILURE);

    private final MessageKeyProvider message;

    ImportFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

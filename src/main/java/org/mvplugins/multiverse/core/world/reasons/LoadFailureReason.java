package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum LoadFailureReason implements FailureReason {
    WORLD_ALREADY_LOADING(MVCorei18n.LOADWORLD_WORLDALREADYLOADING),

    WORLD_NON_EXISTENT(MVCorei18n.LOADWORLD_WORLDNONEXISTENT),

    WORLD_EXIST_FOLDER(MVCorei18n.LOADWORLD_WORLDEXISTFOLDER),

    @ApiStatus.AvailableSince("5.2")
    WORLD_FOLDER_INVALID(MVCorei18n.IMPORTWORLD_WORLDFOLDERINVALID),

    WORLD_EXIST_LOADED(MVCorei18n.LOADWORLD_WORLDEXISTLOADED),

    @ApiStatus.AvailableSince("5.2")
    BUKKIT_ENVIRONMENT_MISMATCH(MVCorei18n.LOADWORLD_BUKKITENVIRONMENTMISMATCH),

    @ApiStatus.AvailableSince("5.7")
    BUKKIT_NAMESPACED_KEY_MISMATCH(MVCorei18n.LOADWORLD_BUKKITNAMESPACEDKEYMISMATCH),

    WORLD_CREATOR_FAILED(MVCorei18n.GENERIC_FAILURE),
    ;

    private final MessageKeyProvider message;

    LoadFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

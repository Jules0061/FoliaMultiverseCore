package org.mvplugins.multiverse.core.world.key;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

@ApiStatus.AvailableSince("5.7")
public enum WorldKeyParseFailReason implements FailureReason {
    @ApiStatus.AvailableSince("5.7")
    EMPTY(MVCorei18n.WORLDKEYPARSE_EMPTY),

    @ApiStatus.AvailableSince("5.7")
    INVALID_WORLD_NAME(MVCorei18n.WORLDKEYPARSE_INVALIDWORLDNAME),

    @ApiStatus.AvailableSince("5.7")
    INVALID_NAMESPACED_KEY(MVCorei18n.WORLDKEYPARSE_INVALIDNAMESPACEDKEY),
    ;

    private final MessageKeyProvider message;

    WorldKeyParseFailReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

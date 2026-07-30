package org.mvplugins.multiverse.core.teleportation;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import org.mvplugins.multiverse.core.event.MVTeleportDestinationEvent;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum TeleportFailureReason implements FailureReason {
    NULL_DESTINATION(MVCorei18n.TELEPORTFAILUREREASON_NULL_DESTINATION),

    NULL_LOCATION(MVCorei18n.TELEPORTFAILUREREASON_NULL_LOCATION),

    NULL_WORLD(MVCorei18n.TELEPORTFAILUREREASON_NULL_WORLD),

    UNSAFE_LOCATION(MVCorei18n.TELEPORTFAILUREREASON_UNSAFE_LOCATION),

    TELEPORT_FAILED(MVCorei18n.TELEPORTFAILUREREASON_TELEPORT_FAILED),

    TELEPORT_FAILED_EXCEPTION(MVCorei18n.TELEPORTFAILUREREASON_TELEPORT_FAILED_EXCEPTION),

    EVENT_CANCELLED(MVCorei18n.TELEPORTFAILUREREASON_EVENT_CANCELLED),
    ;

    private final MessageKeyProvider messageKey;

    TeleportFailureReason(MessageKeyProvider message) {
        this.messageKey = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return messageKey.getMessageKey();
    }
}

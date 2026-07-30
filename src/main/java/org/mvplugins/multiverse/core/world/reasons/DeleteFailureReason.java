package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.mvplugins.multiverse.core.event.world.MVWorldDeleteEvent;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum DeleteFailureReason implements FailureReason {
    WORLD_NON_EXISTENT(MVCorei18n.DELETEWORLD_WORLDNONEXISTENT),

    LOAD_FAILED(MVCorei18n.DELETEWORLD_LOADFAILED),

    WORLD_FOLDER_NOT_FOUND(MVCorei18n.DELETEWORLD_WORLDFOLDERNOTFOUND),

    REMOVE_FAILED(MVCorei18n.GENERIC_FAILURE),

    FAILED_TO_DELETE_FOLDER(MVCorei18n.DELETEWORLD_FAILEDTODELETEFOLDER),

    EVENT_CANCELLED(MVCorei18n.GENERIC_FAILURE);

    private final MessageKeyProvider message;

    DeleteFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

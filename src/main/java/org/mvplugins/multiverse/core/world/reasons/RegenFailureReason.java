package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum RegenFailureReason implements FailureReason {
    DELETE_FAILED(MVCorei18n.GENERIC_FAILURE),

    CREATE_FAILED(MVCorei18n.GENERIC_FAILURE);

    private final MessageKeyProvider message;

    RegenFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

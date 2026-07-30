package org.mvplugins.multiverse.core.utils.result;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.locale.MVCorei18n;

public interface SuccessReason extends MessageKeyProvider {
    @ApiStatus.AvailableSince("5.2")
    SuccessReason GENERIC = new SuccessReason() { };

    default MessageKey getMessageKey() {
        return MVCorei18n.GENERIC_SUCCESS.getMessageKey();
    }
}

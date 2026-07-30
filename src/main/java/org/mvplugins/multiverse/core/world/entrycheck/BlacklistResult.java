package org.mvplugins.multiverse.core.world.entrycheck;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;
import org.mvplugins.multiverse.core.utils.result.SuccessReason;

public final class BlacklistResult {
    public enum Success implements SuccessReason {
        UNKNOWN_FROM_WORLD,
        BYPASSED_BLACKLISTED,
        NOT_BLACKLISTED
    }

    public enum Failure implements FailureReason {
        BLACKLISTED(MVCorei18n.ENTRYCHECK_BLACKLISTED);

        private final MessageKeyProvider message;

        Failure(MessageKeyProvider message) {
            this.message = message;
        }

        @Override
        public MessageKey getMessageKey() {
            return message.getMessageKey();
        }
    }
}

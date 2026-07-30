package org.mvplugins.multiverse.core.world.reasons;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.FailureReason;

public enum WorldCreatorFailureReason implements FailureReason {

    INVALID_BIOME_PROVIDER(MVCorei18n.WORLDCREATOR_INVALIDBIOMEPROVIDER),

    INVALID_CHUNK_GENERATOR(MVCorei18n.WORLDCREATOR_INVALIDCHUNKGENERATOR),

    BUKKIT_CREATION_FAILED(MVCorei18n.WORLDCREATOR_BUKKITCREATIONFAILED),
    ;

    private final MessageKeyProvider message;

    WorldCreatorFailureReason(MessageKeyProvider message) {
        this.message = message;
    }

    @Override
    public MessageKey getMessageKey() {
        return message.getMessageKey();
    }
}

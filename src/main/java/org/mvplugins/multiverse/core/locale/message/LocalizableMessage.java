package org.mvplugins.multiverse.core.locale.message;

import co.aikar.commands.CommandIssuer;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.locale.PluginLocales;

public interface LocalizableMessage {
    @Nullable Message getLocalizableMessage();
}

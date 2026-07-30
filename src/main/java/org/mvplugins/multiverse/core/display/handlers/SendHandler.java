package org.mvplugins.multiverse.core.display.handlers;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;

@FunctionalInterface
public interface SendHandler {
    void send(@NotNull MVCommandIssuer issuer, @NotNull List<String> content);
}

package org.mvplugins.multiverse.core.display.handlers;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;

public final class DefaultSendHandler implements SendHandler {

    private static DefaultSendHandler instance;

    public static DefaultSendHandler getInstance() {
        if (instance == null) {
            instance = new DefaultSendHandler();
        }
        return instance;
    }

    private DefaultSendHandler() {
    }

    @Override
    public void send(@NotNull MVCommandIssuer issuer, @NotNull List<String> content) {
        content.forEach(issuer::sendMessage);
    }
}

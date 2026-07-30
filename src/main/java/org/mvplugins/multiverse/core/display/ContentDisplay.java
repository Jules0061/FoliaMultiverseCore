package org.mvplugins.multiverse.core.display;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.display.handlers.DefaultSendHandler;
import org.mvplugins.multiverse.core.display.handlers.SendHandler;
import org.mvplugins.multiverse.core.display.parsers.ContentProvider;

public class ContentDisplay {

    @NotNull
    public static ContentDisplay create() {
        return new ContentDisplay();
    }

    private final List<ContentProvider> contentParsers = new ArrayList<>();
    private SendHandler sendHandler = DefaultSendHandler.getInstance();

    ContentDisplay() {
    }

    @NotNull
    public ContentDisplay addContent(@NotNull ContentProvider parser) {
        contentParsers.add(parser);
        return this;
    }

    @NotNull
    public ContentDisplay withSendHandler(@NotNull SendHandler handler) {
        sendHandler = handler;
        return this;
    }

    public void send(@NotNull MVCommandIssuer issuer) {
        Objects.requireNonNull(sendHandler, "No send handler set for content display");
        List<String> parsedContent = new ArrayList<>();
        contentParsers.forEach(parser -> parsedContent.addAll(parser.parse(issuer)));
        sendHandler.send(issuer, parsedContent);
    }
}

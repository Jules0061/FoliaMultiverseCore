package org.mvplugins.multiverse.core.display.handlers;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.display.filters.ContentFilter;
import org.mvplugins.multiverse.core.display.filters.DefaultContentFilter;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;

public abstract class BaseSendHandler<T extends BaseSendHandler<?>> implements SendHandler {

    protected Message header = null;

    protected ContentFilter filter = DefaultContentFilter.get();

    protected Message noContentMessage = Message.of(MVCorei18n.CONTENTDISPLAY_NOCONTENT);

    @Override
    public void send(@NotNull MVCommandIssuer issuer, @NotNull List<String> content) {
        sendHeader(issuer);
        List<String> filteredContent = filterContent(content);
        if (filteredContent.isEmpty() && noContentMessage != null) {
            issuer.sendMessage(noContentMessage);
            return;
        }
        sendContent(issuer, filteredContent);
    }

    protected void sendHeader(MVCommandIssuer issuer) {
        if (header != null) {
            issuer.sendMessage(header);
        }
    }

    protected List<String> filterContent(@NotNull List<String> content) {
        if (filter.needToFilter()) {
            return content.stream().filter(filter::checkMatch).collect(Collectors.toList());
        }
        return content;
    }

    protected abstract void sendContent(@NotNull MVCommandIssuer issuer, @NotNull List<String> content);

    public T withHeader(@NotNull String header, @NotNull Object... replacements) {
        return withHeader(Message.of(String.format(header, replacements)));
    }

    public T withHeader(@NotNull Message header) {
        this.header = header;
        return getT();
    }

    public T withFilter(@NotNull ContentFilter filter) {
        this.filter = filter;
        return getT();
    }

    public T noContentMessage(@Nullable String message) {
        return noContentMessage(message == null ? null : Message.of(message));
    }

    public T noContentMessage(@Nullable Message message) {
        this.noContentMessage = message;
        return getT();
    }

    @SuppressWarnings("unchecked")
    private @NotNull T getT() {
        return (T) this;
    }

    public Message getHeader() {
        return header;
    }

    public ContentFilter getFilter() {
        return filter;
    }

    public Message getNoContentMessage() {
        return noContentMessage;
    }
}

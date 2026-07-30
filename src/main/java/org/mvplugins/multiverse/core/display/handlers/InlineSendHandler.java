package org.mvplugins.multiverse.core.display.handlers;

import java.util.List;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.MVCorei18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

public class InlineSendHandler extends BaseSendHandler<InlineSendHandler> {

    public static InlineSendHandler create() {
        return new InlineSendHandler();
    }

    private String delimiter = ChatColor.WHITE + ", ";
    private String prefix = null;

    InlineSendHandler() {
    }

    @Override
    public void sendContent(@NotNull MVCommandIssuer issuer, @NotNull List<String> content) {
        if (filter.needToFilter()) {
            issuer.sendMessage(MVCorei18n.CONTENTDISPLAY_FILTER, replace("{filter}").with(filter));
        }
        String message = String.join(delimiter, content);
        if (prefix != null) {
            message = prefix + message;
        }
        issuer.sendMessage(message);
    }

    public InlineSendHandler withDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public InlineSendHandler withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getPrefix() {
        return prefix;
    }
}

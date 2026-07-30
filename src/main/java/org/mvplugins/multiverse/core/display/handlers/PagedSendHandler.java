package org.mvplugins.multiverse.core.display.handlers;

import java.util.List;

import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

public class PagedSendHandler extends BaseSendHandler<PagedSendHandler> {

    public static PagedSendHandler create() {
        return new PagedSendHandler();
    }

    private boolean paginate = true;
    private boolean paginateInConsole = false;
    private boolean padEnd = true;
    private int linesPerPage = 8;
    private int targetPage = 1;

    PagedSendHandler() {
    }

    @Override
    public void sendContent(@NotNull MVCommandIssuer issuer, @NotNull List<String> content) {
        if (!paginate || (issuer.getIssuer() instanceof ConsoleCommandSender && !paginateInConsole)) {
            sendNormal(issuer, content);
            return;
        }
        sendPaged(issuer, content);
    }

    private void sendNormal(@NotNull MVCommandIssuer issuer, @NotNull List<String> content) {
        if (filter.needToFilter()) {
            issuer.sendMessage(MVCorei18n.CONTENTDISPLAY_FILTER, replace("{filter}").with(filter));
        }
        content.forEach(issuer::sendMessage);
    }

    private void sendPaged(@NotNull MVCommandIssuer issuer, @NotNull List<String> content) {
        int totalPages = (content.size() + linesPerPage - 1) / linesPerPage;
        if (targetPage < 1 || targetPage > totalPages) {
            issuer.sendMessage(MVCorei18n.CONTENTDISPLAY_INVALIDPAGE, replace("{total}").with(totalPages));
            return;
        }

        if (filter.needToFilter()) {
            issuer.sendMessage(MVCorei18n.CONTENTDISPLAY_PAGEFILTER,
                    replace("{page}").with(Message.of(MVCorei18n.CONTENTDISPLAY_PAGE,
                            replace("{current}").with(targetPage),
                            replace("{total}").with(totalPages))),
                    replace("{filter}").with(Message.of(MVCorei18n.CONTENTDISPLAY_FILTER,
                            replace("{filter}").with(filter))));
        } else {
            issuer.sendMessage(MVCorei18n.CONTENTDISPLAY_PAGE,
                    replace("{current}").with(targetPage),
                    replace("{total}").with(totalPages));
        }

        int startIndex = (targetPage - 1) * linesPerPage;
        int pageEndIndex = startIndex + linesPerPage;
        int endIndex = Math.min(pageEndIndex, content.size());
        List<String> pageContent = content.subList(startIndex, endIndex);
        if (padEnd) {
            for (int i = 0; i < (pageEndIndex - endIndex); i++) {
                pageContent.add("");
            }
        }
        pageContent.forEach(issuer::sendMessage);
    }

    public PagedSendHandler doPagination(boolean paginate) {
        this.paginate = paginate;
        return this;
    }

    public PagedSendHandler doPaginationInConsole(boolean paginateInConsole) {
        this.paginateInConsole = paginateInConsole;
        return this;
    }

    public PagedSendHandler doEndPadding(boolean padEnd) {
        this.padEnd = padEnd;
        return this;
    }

    public PagedSendHandler withLinesPerPage(int linesPerPage) {
        this.linesPerPage = linesPerPage;
        return this;
    }

    public PagedSendHandler withTargetPage(int targetPage) {
        this.targetPage = targetPage;
        return this;
    }

    public boolean isPaginate() {
        return paginate;
    }

    public boolean isPaginateInConsole() {
        return paginateInConsole;
    }

    public boolean isPadEnd() {
        return padEnd;
    }

    public int getLinesPerPage() {
        return linesPerPage;
    }

    public int getTargetPage() {
        return targetPage;
    }
}

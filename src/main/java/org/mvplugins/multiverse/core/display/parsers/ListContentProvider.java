package org.mvplugins.multiverse.core.display.parsers;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;

public class ListContentProvider<T> implements ContentProvider {

    public static <T> ListContentProvider<T> forContent(List<T> list) {
        return new ListContentProvider<>(list);
    }

    private final List<T> list;

    ListContentProvider(List<T> list) {
        this.list = list;
    }

    @Override
    public Collection<String> parse(@NotNull MVCommandIssuer issuer) {
        return list.stream()
                .map(object -> object instanceof Message message ? message.formatted(issuer) : String.valueOf(object))
                .toList();
    }

    public List<T> getList() {
        return list;
    }
}

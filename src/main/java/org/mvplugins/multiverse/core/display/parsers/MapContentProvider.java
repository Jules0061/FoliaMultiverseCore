package org.mvplugins.multiverse.core.display.parsers;

import java.util.Collection;
import java.util.Map;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;

public class MapContentProvider<K, V> implements ContentProvider {

    public static <K, V> MapContentProvider<K, V> forContent(Map<K, V> map) {
        return new MapContentProvider<>(map);
    }

    private final Map<K, V> map;

    private String format = "%s%s%s%s%s";
    private ChatColor keyColor = ChatColor.WHITE;
    private ChatColor valueColor = ChatColor.WHITE;
    private String separator = ": ";

    MapContentProvider(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public Collection<String> parse(@NotNull MVCommandIssuer issuer) {
        return map.entrySet().stream()
                .map(e -> String.format(format,
                        keyColor, formatValue(issuer, e.getKey()), separator, valueColor, formatValue(issuer, e.getValue())))
                .toList();
    }

    private String formatValue(MVCommandIssuer issuer, Object value) {
        String stringValue;
        if (value instanceof Message message) {
            stringValue = message.formatted(issuer);
        } else if (value instanceof String string) {
            stringValue = string.isEmpty() ? Message.of(MVCorei18n.CONTENTDISPLAY_EMPTY).formatted(issuer) : string;
        } else {
            stringValue = value == null ? null : String.valueOf(value);
        }
        return stringValue == null ? Message.of(MVCorei18n.CONTENTDISPLAY_NULL).formatted(issuer) : stringValue;
    }

    public MapContentProvider<K, V> withFormat(String format) {
        this.format = format;
        return this;
    }

    public MapContentProvider<K, V> withKeyColor(ChatColor keyColor) {
        this.keyColor = keyColor;
        return this;
    }

    public MapContentProvider<K, V> withValueColor(ChatColor valueColor) {
        this.valueColor = valueColor;
        return this;
    }

    public MapContentProvider<K, V> withSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public Map<K, V> getMap() {
        return map;
    }

    public String getFormat() {
        return format;
    }

    public ChatColor getKeyColor() {
        return keyColor;
    }

    public ChatColor getValueColor() {
        return valueColor;
    }

    public String getSeparator() {
        return separator;
    }
}

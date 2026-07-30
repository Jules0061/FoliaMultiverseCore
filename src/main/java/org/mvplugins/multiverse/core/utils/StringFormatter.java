package org.mvplugins.multiverse.core.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class StringFormatter {

    private StringFormatter() {
    }

    public static @NotNull String joinAnd(@Nullable List<String> list) {
        return join(list, ", ", " and ");
    }

    public static @NotNull String join(@Nullable Collection<?> list, @NotNull String separator) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(separator));
    }

    public static @NotNull String join(@Nullable List<String> list, @NotNull String separator, @NotNull String lastSeparator) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder authors = new StringBuilder();
        authors.append(list.get(0));

        for (int i = 1; i < list.size(); i++) {
            if (i == list.size() - 1) {
                authors.append(lastSeparator).append(list.get(i));
            } else {
                authors.append(separator).append(list.get(i));
            }
        }

        return authors.toString();
    }

    @Deprecated(forRemoval = true, since = "5.5")
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static Collection<String> addonToCommaSeperated(@Nullable String input, @NotNull Collection<String> addons) {
        return addOnToCommaSeparated(input, addons);
    }

    @ApiStatus.AvailableSince("5.5")
    public static Collection<String> addOnToCommaSeparated(@Nullable String input, @NotNull Collection<String> addons) {
        if (Strings.isNullOrEmpty(input)) {
            return addons;
        }
        int lastComma = input.lastIndexOf(',');
        String previousInputs = input.substring(0, lastComma + 1);
        Set<String> inputSet = Sets.newHashSet(REPatterns.COMMA.split(input));
        return addons.stream()
                .filter(suggestion -> !inputSet.contains(suggestion))
                .map(suggestion -> previousInputs + suggestion)
                .toList();
    }

    public static @NotNull Collection<String> parseQuotesInArgs(@NotNull String[] args) {
        List<String> result = new ArrayList<>(args.length);
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int quoteStartIndex = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (!inQuotes && arg.startsWith("\"") && !arg.endsWith("\"")) {
                inQuotes = true;
                quoteStartIndex = i;
                current.append(arg.substring(1));
            } else if (inQuotes && arg.endsWith("\"")) {
                current.append(" ").append(arg, 0, arg.length() - 1);
                result.add(current.toString());
                current.setLength(0);
                inQuotes = false;
                quoteStartIndex = -1;
            } else if (inQuotes) {
                current.append(" ").append(arg);
            } else if (arg.startsWith("\"") && arg.endsWith("\"") && arg.length() > 1) {
                result.add(arg.substring(1, arg.length() - 1));
            } else {
                result.add(arg);
            }
        }

        if (inQuotes) {
            result.addAll(Arrays.asList(args).subList(quoteStartIndex, args.length));
        }

        return result;
    }

    @Contract("null -> null")
    public static @Nullable String quoteMultiWordString(@Nullable String input) {
        return input != null && input.contains(" ") ? "\"" + input + "\"" : input;
    }

    @ApiStatus.AvailableSince("5.5")
    public static @Unmodifiable Map<String, String> parseCSVMap(@Nullable String input) {
        if (Strings.isNullOrEmpty(input)) {
            return Map.of();
        }
        return REPatterns.COMMA.splitAsStream(input)
                .map(s -> REPatterns.EQUALS.split(s, 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toUnmodifiableMap(parts -> parts[0], parts -> parts[1]));
    }
}

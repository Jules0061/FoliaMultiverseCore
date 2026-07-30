package org.mvplugins.multiverse.core.command.flags;

import java.util.List;
import java.util.function.Function;

import co.aikar.commands.InvalidCommandArgument;
import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.command.flag.CommandValueFlag;
import org.mvplugins.multiverse.core.display.filters.ContentFilter;
import org.mvplugins.multiverse.core.display.filters.RegexContentFilter;

public final class FilterCommandFlag extends CommandValueFlag<ContentFilter> {

    private static final Function<String, ContentFilter> VALUE_PARSER = value -> {
        try {
            return RegexContentFilter.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandArgument("Invalid filter: " + value);
        }
    };

    @NotNull
    public static FilterCommandFlag create() {
        return new FilterCommandFlag();
    }

    private FilterCommandFlag() {
        super("--filter", List.of("-f"), ContentFilter.class, false, null, VALUE_PARSER, null);
    }
}

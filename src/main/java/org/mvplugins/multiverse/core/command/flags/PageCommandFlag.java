package org.mvplugins.multiverse.core.command.flags;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import org.mvplugins.multiverse.core.command.flag.CommandValueFlag;

public final class PageCommandFlag extends CommandValueFlag<Integer> {

    private static final Function<String, Integer> VALUE_PARSER = value -> {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid page number: " + value);
        }
    };

    @NotNull
    public static PageCommandFlag create() {
        return new PageCommandFlag();
    }

    private PageCommandFlag() {
        super("--page", List.of("-p"), Integer.class, false, null, VALUE_PARSER, null);
    }
}

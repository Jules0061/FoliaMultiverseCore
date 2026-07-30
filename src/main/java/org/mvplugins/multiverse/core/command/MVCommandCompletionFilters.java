package org.mvplugins.multiverse.core.command;

import co.aikar.commands.CommandCompletionContext;
import co.aikar.commands.CommandCompletionFilter;
import co.aikar.commands.apachecommonslang.ApacheCommonsLangUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

@ApiStatus.AvailableSince("5.7")
public final class MVCommandCompletionFilters {

    @ApiStatus.AvailableSince("5.7")
    public static final CommandCompletionFilter NAMESPACED_KEY = (context, completion) -> {
        String[] split = completion.split(":", 2);
        if (split.length < 2) {
            return ApacheCommonsLangUtil.startsWithIgnoreCase(completion, context.getInput());
        }
        String lowerCase = context.getInput().toLowerCase(Locale.ROOT);
        return ApacheCommonsLangUtil.startsWithIgnoreCase(completion, context.getInput())
                || split[0].toLowerCase(Locale.ROOT).startsWith(lowerCase)
                || split[1].toLowerCase(Locale.ROOT).contains(lowerCase);
    };

    @ApiStatus.AvailableSince("5.7")
    public static <C extends CommandCompletionContext> CommandCompletionFilter<C> namespacedKey() {
        return NAMESPACED_KEY;
    }

    private MVCommandCompletionFilters() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

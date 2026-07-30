package org.mvplugins.multiverse.core.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.BukkitRootCommand;
import co.aikar.commands.CommandIssuer;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.utils.MVScheduler;
import org.mvplugins.multiverse.core.utils.StringFormatter;

import java.util.List;

public class MVRootCommand extends BukkitRootCommand {

    private final MVScheduler scheduler;

    protected MVRootCommand(BukkitCommandManager manager, String name, MVScheduler scheduler) {
        super(manager, name);
        this.scheduler = scheduler;
    }

    @Override
    public @Nullable BaseCommand execute(CommandIssuer sender, String commandLabel, String[] args) {
        String[] quoteFormatedArgs = StringFormatter.parseQuotesInArgs(args).toArray(String[]::new);
        if (MVScheduler.isRegionisedServer() && !scheduler.isGlobalThread()) {
            scheduler.runGlobal(() -> super.execute(sender, commandLabel, quoteFormatedArgs));
            return null;
        }
        return super.execute(sender, commandLabel, quoteFormatedArgs);
    }

    @Override
    public List<String> getTabCompletions(CommandIssuer sender, String alias, String[] args, boolean commandsOnly, boolean isAsync) {
        String[] quoteFormatedArgs = StringFormatter.parseQuotesInArgs(args).toArray(String[]::new);
        return super.getTabCompletions(sender, alias, quoteFormatedArgs, commandsOnly, isAsync)
                .stream()
                .map(StringFormatter::quoteMultiWordString)
                .toList();
    }
}

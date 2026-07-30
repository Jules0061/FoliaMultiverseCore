package org.mvplugins.multiverse.core.command.flag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.aikar.commands.InvalidCommandArgument;
import com.dumptruckman.minecraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

@Service
public class CommandFlagsManager {
    private final Map<String, CommandFlagGroup> flagGroupMap;

    public CommandFlagsManager() {
        flagGroupMap = new HashMap<>();
    }

    public void registerFlagGroup(@NotNull CommandFlagGroup flagGroup) {
        flagGroupMap.put(flagGroup.getName(), flagGroup);
    }

    public @Nullable CommandFlagGroup getFlagGroup(@Nullable String groupName) {
        return this.flagGroupMap.get(groupName);
    }

    public @NotNull Collection<String> suggest(@Nullable String groupName, @NotNull String[] flags) {
        CommandFlagGroup flagGroup = this.getFlagGroup(groupName);
        if (flagGroup == null) {
            Logging.warning("Unknown flag group: " + groupName);
            return Collections.emptyList();
        }

        Collection<String> suggestions = new ArrayList<>();
        CommandFlag currentFlag = (flags.length <= 1) ? null : flagGroup.getFlagByKey(flags[flags.length - 2]);

        if (currentFlag instanceof CommandValueFlag<?> valueFlag) {
            if (valueFlag.getCompletion() != null) {
                suggestions.addAll(valueFlag.getCompletion().apply(flags[flags.length - 1]));
            }
            if (valueFlag.isOptional()) {
                suggestions.addAll(flagGroup.getRemainingKeys(flags));
            }
        } else {
            suggestions.addAll(flagGroup.getRemainingKeys(flags));
        }

        return suggestions;
    }

    public @NotNull ParsedCommandFlags parse(@Nullable String groupName, @NotNull String[] flags) {
        CommandFlagGroup flagGroup = this.getFlagGroup(groupName);
        if (flagGroup == null) {
            return ParsedCommandFlags.EMPTY;
        }

        return new CommandFlagsParser(this.getFlagGroup(groupName), flags).parse();
    }
}

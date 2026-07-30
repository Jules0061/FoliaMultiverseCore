package org.mvplugins.multiverse.core.command.flag;

import co.aikar.commands.InvalidCommandArgument;

public class CommandFlagsParser {
    private final CommandFlagGroup flagGroup;
    private final String[] flags;

    private ParsedCommandFlags parsedFlags;
    private boolean nextArgMayBeKey;
    private boolean nextArgMayBeValue;
    private CommandFlag currentFlag;

    public CommandFlagsParser(CommandFlagGroup flagGroup, String[] flags) {
        this.flagGroup = flagGroup;
        this.flags = flags;
    }

    public ParsedCommandFlags parse() {
        parsedFlags = new ParsedCommandFlags();

        this.nextArgMayBeKey = true;
        this.nextArgMayBeValue = false;

        for (String flag : flags) {
            if (this.nextArgMayBeKey) {
                if (parseKey(flag)) continue;
            }
            if (this.nextArgMayBeValue) {
                if (parseValue(flag)) continue;
            }
            throw new InvalidCommandArgument(flag + " is not a valid flag.");
        }

        if (!this.nextArgMayBeKey && this.nextArgMayBeValue) {
            throw new InvalidCommandArgument(currentFlag.getKey() + " requires a value!");
        }

        return parsedFlags;
    }

    private boolean parseKey(String flag) {
        CommandFlag potentialFlag = flagGroup.getFlagByKey(flag);
        if (potentialFlag == null) {
            return false;
        }

        this.currentFlag = potentialFlag;

        if (this.currentFlag instanceof CommandValueFlag<?> valueFlag) {
            if (valueFlag.isOptional()) {
                parsedFlags.addFlagResult(valueFlag.getKey(), valueFlag.getDefaultValue());
                this.nextArgMayBeKey = true;
                this.nextArgMayBeValue = true;
                return true;
            }

            this.nextArgMayBeKey = false;
            this.nextArgMayBeValue = true;
            return true;
        }

        parsedFlags.addFlagResult(this.currentFlag.getKey(), null);
        this.nextArgMayBeKey = true;
        this.nextArgMayBeValue = false;

        return true;
    }

    private boolean parseValue(String flag) {
        if (this.currentFlag == null) {
            throw new InvalidCommandArgument("Some flag logic error occurred at '" + flag + "'!");
        }
        if (flagGroup.hasKey(flag)) {
            throw new InvalidCommandArgument(currentFlag.getKey() + " requires a value!");
        }

        Object flagValue;
        CommandValueFlag<?> valueFlag = (CommandValueFlag<?>) this.currentFlag;
        flagValue = valueFlag.getContext() != null ? valueFlag.getContext().apply(flag) : flag;
        parsedFlags.addFlagResult(valueFlag.getKey(), flagValue);

        this.nextArgMayBeKey = true;
        this.nextArgMayBeValue = false;
        this.currentFlag = null;
        return true;
    }
}

package org.mvplugins.multiverse.core.world.options;

import org.jetbrains.annotations.NotNull;

public sealed interface KeepWorldSettingsOptions permits CloneWorldOptions, RegenWorldOptions {

    @NotNull KeepWorldSettingsOptions keepGameRule(boolean keepGameRuleInput);

    boolean keepGameRule();

    @NotNull KeepWorldSettingsOptions keepWorldConfig(boolean keepWorldConfigInput);

    boolean keepWorldConfig();

    @NotNull KeepWorldSettingsOptions keepWorldBorder(boolean keepWorldBorderInput);

    boolean keepWorldBorder();
}

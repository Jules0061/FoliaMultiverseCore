package org.mvplugins.multiverse.core.config.node;

import org.jetbrains.annotations.NotNull;

public interface CommentedNode extends Node {

    @NotNull String[] getComments();
}

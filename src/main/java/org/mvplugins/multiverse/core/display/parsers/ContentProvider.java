package org.mvplugins.multiverse.core.display.parsers;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;

@FunctionalInterface
public interface ContentProvider {
    Collection<String> parse(@NotNull MVCommandIssuer issuer);
}

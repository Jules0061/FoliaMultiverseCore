package org.mvplugins.multiverse.core.world;

import org.mvplugins.multiverse.core.world.key.WorldKeyOrName;

import java.util.List;

record NewAndRemovedWorlds(List<WorldConfig> newWorlds, List<WorldKeyOrName> removedWorlds) {
}

package org.mvplugins.multiverse.core.utils;

import de.themoep.idconverter.IdMappings;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public final class MaterialConverter {

    @Nullable
    public static Material stringToMaterial(@Nullable String value) {
        IdMappings.Mapping mapping = IdMappings.getById(value != null ? value : "");
        if (mapping != null) {
            return Material.matchMaterial(mapping.getFlatteningType());
        } else {
            return Material.matchMaterial(value != null ? value : "");
        }
    }
}

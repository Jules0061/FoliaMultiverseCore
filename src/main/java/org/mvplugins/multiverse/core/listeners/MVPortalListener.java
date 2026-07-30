package org.mvplugins.multiverse.core.listeners;

import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.IgnoreIfCancelled;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.world.WorldManager;

import static org.bukkit.PortalType.CUSTOM;

@Service
final class MVPortalListener implements CoreListener {

    private final CoreConfig config;
    private final WorldManager worldManager;
    private final MVCommandManager commandManager;

    @Inject
    MVPortalListener(@NotNull CoreConfig config,
                     @NotNull WorldManager worldManager,
                     @NotNull MVCommandManager commandManager) {
        this.config = config;
        this.worldManager = worldManager;
        this.commandManager = commandManager;
    }

    @EventMethod
    @IgnoreIfCancelled
    void portalCreate(PortalCreateEvent event) {
        Logging.fine("Attempting to create portal at '%s' with reason: %s",
                event.getWorld().getName(), event.getReason());

        this.worldManager.getLoadedWorld(event.getWorld()).peek(world -> {
            PortalType targetType = getPortalType(event);
            if (targetType == PortalType.CUSTOM) {
                return;
            }
            if (!world.getPortalForm().isPortalAllowed(targetType)) {
                Logging.fine("Cancelling creation of %s portal because portalForm disallows.", targetType);
                event.setCancelled(true);
                commandManager.getCommandIssuer(event.getEntity()).sendError(MVCorei18n.PORTALFORM_DISABLED_NETHER);
            }
        }).onEmpty(() ->
                Logging.fine("World '%s' is not managed by Multiverse! Ignoring at PortalCreateEvent.",
                        event.getWorld().getName()));
    }

    private PortalType getPortalType(PortalCreateEvent event) {
        return switch (event.getReason()) {
            case FIRE -> {
                for (BlockState block : event.getBlocks()) {
                    if (block.getType() == Material.NETHER_PORTAL) {
                        yield PortalType.NETHER;
                    }
                }
                yield CUSTOM;
            }
            case NETHER_PAIR -> PortalType.NETHER;
            case END_PLATFORM -> PortalType.ENDER;
            default -> {
                Logging.fine("Portal created is not NETHER or ENDER type. Ignoring...");
                yield CUSTOM;
            }
        };
    }

    @EventMethod
    @IgnoreIfCancelled
    void playerInteract(PlayerInteractEvent event) {
        if (isCreateEndPortalInteraction(event)) {
            return;
        }

        this.worldManager.getLoadedWorld(event.getPlayer().getWorld()).peek(world -> {
            if (!world.getPortalForm().isPortalAllowed(PortalType.ENDER)) {
                Logging.fine("Cancelling creation of ENDER portal because portalForm disallows.");
                event.setCancelled(true);
                commandManager.getCommandIssuer(event.getPlayer()).sendError(MVCorei18n.PORTALFORM_DISABLED_END);
            }
        }).onEmpty(() ->
                Logging.fine("World '%s' is not managed by Multiverse! Ignoring at PlayerInteractEvent.",
                        event.getPlayer().getWorld().getName()));
    }

    private boolean isCreateEndPortalInteraction(PlayerInteractEvent event) {
        return event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.END_PORTAL_FRAME
                || event.getItem() == null
                || event.getItem().getType() != Material.ENDER_EYE;
    }

    @EventMethod
    void entityPortal(EntityPortalEvent event) {
        if (event.isCancelled() || event.getTo() == null) {
            return;
        }
        if (config.isUsingCustomPortalSearch()) {
            event.setSearchRadius(config.getCustomPortalSearchRadius());
        }
    }
}

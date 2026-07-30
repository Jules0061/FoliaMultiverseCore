package org.mvplugins.multiverse.core.teleportation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Option;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jvnet.hk2.annotations.Service;

@Service
public final class TeleportQueue {

    private final Map<String, String> teleportQueueMap;

    TeleportQueue() {
        teleportQueueMap = new ConcurrentHashMap<>();
    }

    public void addToQueue(CommandSender teleporter, Player teleportee) {
        addToQueue(teleporter.getName(), teleportee.getName());
    }

    public void addToQueue(String teleporter, String teleportee) {
        Logging.finest("Adding mapping '%s' => '%s' to teleport queue", teleporter, teleportee);
        teleportQueueMap.put(teleportee, teleporter);
    }

    public Option<String> popFromQueue(String playerName) {
        return Option.of(teleportQueueMap.remove(playerName));
    }
}

package org.mvplugins.multiverse.core.command.queue;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.dumptruckman.minecraft.util.Logging;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.vavr.control.Option;
import jakarta.inject.Inject;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.MVScheduler;
import org.mvplugins.multiverse.core.utils.result.Attempt;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.*;

@Service
public class CommandQueueManager {

    private static final String CONSOLE_NAME = "@console";
    private static final String COMMAND_BLOCK_NAME = "@commandblock";
    private static final long TICKS_PER_SECOND = 20;

    private final MVScheduler scheduler;
    private final CoreConfig config;
    private final Map<String, CommandQueuePayload> queuedCommandMap;

    @Inject
    CommandQueueManager(@NotNull MVScheduler scheduler, @NotNull CoreConfig config) {
        this.scheduler = scheduler;
        this.config = config;
        this.queuedCommandMap = new ConcurrentHashMap<>();
    }

    public void addToQueue(CommandQueuePayload payload) {
        String senderName = parseSenderName(payload.issuer());
        if (canRunImmediately(senderName)) {
            payload.action().run();
            return;
        }

        this.removeFromQueue(senderName);

        Logging.finer("Add new command to queue for sender %s.", senderName);
        this.queuedCommandMap.put(senderName, payload);
        payload.expireTask(runExpireLater(senderName, config.getConfirmTimeout()));

        payload.issuer().sendInfo(payload.prompt());
        var confirmCommand = "/mv confirm";
        if (config.getUseConfirmOtp()) {
            confirmCommand += " " + payload.otp();
        }
        payload.issuer().sendMessage(MVCorei18n.QUEUECOMMAND_PROMPT,
                replace("{command}").with(confirmCommand),
                replace("{timeout}").with(config.getConfirmTimeout()));
    }

    private boolean canRunImmediately(@NotNull String senderName) {
        return switch (config.getConfirmMode()) {
            case ENABLE -> false;
            case PLAYER_ONLY -> senderName.equals(CONSOLE_NAME) || senderName.equals(COMMAND_BLOCK_NAME);
            case DISABLE_COMMAND_BLOCKS -> senderName.equals(COMMAND_BLOCK_NAME);
            case DISABLE_CONSOLE -> senderName.equals(CONSOLE_NAME);
            case DISABLE -> true;
        };
    }

    @Nullable
    private ScheduledTask runExpireLater(@NotNull String senderName, int validDuration) {
        return scheduler.runGlobalLater(
                expireRunnable(senderName),
                validDuration * TICKS_PER_SECOND);
    }

    @NotNull
    private Runnable expireRunnable(@NotNull String senderName) {
        return () -> {
            CommandQueuePayload payload = this.queuedCommandMap.remove(senderName);
            if (payload == null) {
                return;
            }
            payload.issuer().sendMessage(MVCorei18n.QUEUECOMMAND_EXPIRED);
        };
    }

    public Attempt<Void, RunQueuedFailedReason> runQueuedCommand(@NotNull MVCommandIssuer issuer, String otpInput) {
        String senderName = parseSenderName(issuer);
        return Option.of(this.queuedCommandMap.get(senderName)).fold(
                () -> Attempt.failure(RunQueuedFailedReason.NO_COMMAND_IN_QUEUE),
                payload -> runPayload(senderName, otpInput, payload));
    }

    private Attempt<Void, RunQueuedFailedReason> runPayload(String senderName, String otpInput, CommandQueuePayload payload) {
        if (config.getUseConfirmOtp() && !Objects.equals(payload.otp(), otpInput)) {
            return Attempt.failure(RunQueuedFailedReason.INVALID_OTP, replace("{otp}").with(otpInput));
        }
        this.removeFromQueue(senderName);
        Logging.finer("Running queued command...");
        payload.action().run();
        return Attempt.success(null);
    }

    public void removeFromQueue(@NotNull String senderName) {
        CommandQueuePayload payload = this.queuedCommandMap.remove(senderName);
        if (payload == null) {
            Logging.finer("No queue command to remove for sender %s.", senderName);
            return;
        }
        Option.of(payload.expireTask()).peek(ScheduledTask::cancel);
        Logging.finer("Removed queue command for sender %s.", senderName);
    }

    private String parseSenderName(MVCommandIssuer issuer) {
        CommandSender sender = issuer.getIssuer();
        if (isCommandBlock(sender)) {
            return COMMAND_BLOCK_NAME;
        } else if (sender instanceof ConsoleCommandSender) {
            return CONSOLE_NAME;
        }
        return sender.getName();
    }

    private boolean isCommandBlock(@NotNull CommandSender sender) {
        return sender instanceof BlockCommandSender
                && ((BlockCommandSender) sender).getBlock().getBlockData() instanceof CommandBlock;
    }
}

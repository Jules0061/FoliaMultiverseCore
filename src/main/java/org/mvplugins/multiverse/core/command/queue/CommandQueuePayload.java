package org.mvplugins.multiverse.core.command.queue;

import co.aikar.commands.ACFUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;

public class CommandQueuePayload {

    public static CommandQueuePayload issuer(@NotNull MVCommandIssuer issuer) {
        return new CommandQueuePayload(issuer);
    }

    private final MVCommandIssuer issuer;
    private String otp;
    private Runnable action = () -> {};
    private Message prompt = Message.of(MVCorei18n.QUEUECOMMAND_DEFAULTPROMPT);
    private ScheduledTask expireTask;

    protected CommandQueuePayload(@NotNull MVCommandIssuer issuer) {
        this.otp = String.valueOf(ACFUtil.rand(100, 999));
        this.issuer = issuer;
    }

    @NotNull
    public MVCommandIssuer issuer() {
        return issuer;
    }

    public CommandQueuePayload action(@NotNull Runnable action) {
        this.action = action;
        return this;
    }

    @NotNull
    public Runnable action() {
        return action;
    }

    public CommandQueuePayload otp(String otp) {
        this.otp = otp;
        return this;
    }

    public String otp() {
        return otp;
    }

    public CommandQueuePayload prompt(Message prompt) {
        this.prompt = prompt;
        return this;
    }

    public Message prompt() {
        return prompt;
    }

    void expireTask(@Nullable ScheduledTask expireTask) {
        this.expireTask = expireTask;
    }

    @Nullable
    ScheduledTask expireTask() {
        return expireTask;
    }
}

package org.mvplugins.multiverse.core.utils.result;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AsyncAttemptsAggregate<T, F extends FailureReason> {

    public static <T, F extends FailureReason> AsyncAttemptsAggregate<T, F> allOf(List<AsyncAttempt<T, F>> attempts) {
        return new AsyncAttemptsAggregate<>(attempts);
    }

    public static <T, F extends FailureReason> AsyncAttemptsAggregate<T, F> allOf(AsyncAttempt<T, F>... attempts) {
        return allOf(List.of(attempts));
    }

    @ApiStatus.AvailableSince("5.1")
    public static <T, F extends FailureReason> AsyncAttemptsAggregate<T, F> allOfAggregate(List<AsyncAttemptsAggregate<T, F>> attempts) {
        return new AsyncAttemptsAggregate<>(attempts.stream()
                .flatMap(a -> a.attempts.stream())
                .toList());
    }

    @ApiStatus.AvailableSince("5.1")
    public static <T, F extends FailureReason> AsyncAttemptsAggregate<T, F> allOfAggregate(AsyncAttemptsAggregate<T, F>... attempts) {
        return allOfAggregate(List.of(attempts));
    }

    public static <T, F extends FailureReason> AsyncAttemptsAggregate<T, F> emptySuccess() {
        return new AsyncAttemptsAggregate<>(
                Collections.emptyList(),
                CompletableFuture.completedFuture(AttemptsAggregate.emptySuccess())
        );
    }

    private final List<AsyncAttempt<T, F>> attempts;
    private final CompletableFuture<AttemptsAggregate<T, F>> future;

    private AsyncAttemptsAggregate(List<AsyncAttempt<T, F>> attempts) {
        this.attempts = attempts;
        this.future = CompletableFuture.allOf(attempts.stream().map(AsyncAttempt::getFuture).toArray(CompletableFuture[]::new))
                .thenApply(v -> AttemptsAggregate.allOf(attempts.stream()
                        .map(AsyncAttempt::getFuture)
                        .map(CompletableFuture::join).toList()));
    }

    private AsyncAttemptsAggregate(List<AsyncAttempt<T, F>> attempts, CompletableFuture<AttemptsAggregate<T, F>> future) {
        this.attempts = attempts;
        this.future = future;
    }

    @ApiStatus.AvailableSince("5.1")
    public List<AsyncAttempt<T, F>> getAttempts() {
        return attempts.stream().toList();
    }

    public AsyncAttemptsAggregate<T, F> onSuccess(Runnable runnable) {
        return newFuture(future.thenApply(aggregate -> aggregate.onSuccess(runnable)));
    }

    public AsyncAttemptsAggregate<T, F> onFailure(Runnable runnable) {
        return newFuture(future.thenApply(aggregate -> aggregate.onFailure(runnable)));
    }

    public AsyncAttemptsAggregate<T, F> onSuccess(Consumer<List<Attempt<T, F>>> successConsumer) {
        return newFuture(future.thenApply(aggregate -> aggregate.onSuccess(successConsumer)));
    }

    public AsyncAttemptsAggregate<T, F> onFailure(Consumer<List<Attempt<T, F>>> failureConsumer) {
        return newFuture(future.thenApply(aggregate -> aggregate.onFailure(failureConsumer)));
    }

    public AsyncAttemptsAggregate<T, F> onSuccessCount(Consumer<Integer> successConsumer) {
        return newFuture(future.thenApply(aggregate -> aggregate.onSuccessCount(successConsumer)));
    }

    public AsyncAttemptsAggregate<T, F> onFailureCount(Consumer<Map<F, Long>> failureConsumer) {
        return newFuture(future.thenApply(aggregate -> aggregate.onFailureCount(failureConsumer)));
    }

    @ApiStatus.AvailableSince("5.1")
    public AsyncAttemptsAggregate<T, F> thenRun(Runnable runnable) {
        return newFuture(future.thenApply(aggregate -> {
            runnable.run();
            return aggregate;
        }));
    }

    private AsyncAttemptsAggregate<T, F> newFuture(CompletableFuture<AttemptsAggregate<T, F>> future) {
        return new AsyncAttemptsAggregate<>(attempts, future);
    }
}

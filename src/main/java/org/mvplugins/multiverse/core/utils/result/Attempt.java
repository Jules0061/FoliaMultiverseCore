package org.mvplugins.multiverse.core.utils.result;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.exceptions.MultiverseException;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement;

public sealed interface Attempt<T, F extends FailureReason> permits Attempt.Success, Attempt.Failure {

    @ApiStatus.AvailableSince("5.7")
    static <T, F extends FailureReason> Attempt.Success<T, F> successRef(T value) {
        return new Success<>(value);
    }

    @ApiStatus.AvailableSince("5.7")
    static <T, F extends FailureReason> Attempt.Failure<T, F> failureRef(
            F failureReason, MessageReplacement... messageReplacements) {
        return new Failure<>(failureReason, Message.of(failureReason, "Failed!", messageReplacements));
    }

    static <T, F extends FailureReason> Attempt<T, F> success(T value) {
        return successRef(value);
    }

    static <T, F extends FailureReason> Attempt<T, F> failure(
            F failureReason, MessageReplacement... messageReplacements) {
        return failureRef(failureReason, messageReplacements);
    }

    static <T, F extends FailureReason> Attempt<T, F> failure(F failureReason, Message message) {
        return new Failure<>(failureReason, message);
    }

    T get();

    T getOrNull();

    T getOrElse(T defaultValue);

    <X extends Throwable> T getOrThrow(Function<Failure<T, F>, X> exceptionSupplier) throws X;

    F getFailureReason();

    Message getFailureMessage();

    boolean isSuccess();

    boolean isFailure();

    @ApiStatus.AvailableSince("5.1")
    Try<T> toTry();

    @ApiStatus.AvailableSince("5.1")
    Try<T> toTry(Function<Failure<T, F>, Throwable> throwableFunction);

    default Attempt<T, F> thenRun(Runnable runnable) {
        runnable.run();
        return this;
    }

    Attempt<T, F> thenAccept(Consumer<Either<T, F>> consumer);

     Attempt<T, F> peek(Consumer<T> consumer);

    <U> Attempt<U, F> map(Function<? super T, ? extends U> mapper);

    <U> Attempt<U, F> map(Supplier<? extends U> mapper);

    <U> Attempt<U, F> mapAttempt(Function<? super T, Attempt<U, F>> mapper);

    <U> Attempt<U, F> mapAttempt(Supplier<Attempt<U, F>> mapper);

    @ApiStatus.AvailableSince("5.7")
    Attempt<T, F> failIf(Predicate<? super T> predicate, Supplier<Attempt.Failure<T, F>> failureAttempt);

    @ApiStatus.AvailableSince("5.7")
    Attempt<T, F> failIf(Predicate<? super T> predicate, Function<? super T, Attempt.Failure<T, F>> failureAttempt);

    <UF extends FailureReason> Attempt<T, UF> transform(UF failureReason);

    @ApiStatus.AvailableSince("5.1")
    <U> U transform(Function<T, U> successMapper, Function<F, U> failureMapper);

    <N> N fold(Function<Failure<T, F>, N> failureMapper, Function<T, N> successMapper);

    Attempt<T, F> onSuccess(Runnable runnable);

    Attempt<T, F> onSuccess(Consumer<T> consumer);

    Attempt<T, F> onFailure(Runnable runnable);

    Attempt<T, F> onFailure(Consumer<Failure<T, F>> consumer);

    Attempt<T, F> onFailureReason(Consumer<F> consumer);

    final class Success<T, F extends FailureReason> implements Attempt<T, F> {
        private final T value;

        Success(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T getOrNull() {
            return value;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public <X extends Throwable> T getOrThrow(Function<Failure<T, F>, X> exceptionSupplier) throws X  {
            return value;
        }

        @Override
        public F getFailureReason() {
            throw new UnsupportedOperationException("No failure reason as attempt is a success");
        }

        @Override
        public Message getFailureMessage() {
            throw new UnsupportedOperationException("No failure message as attempt is a success");
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public Try<T> toTry() {
            return Try.success(value);
        }

        @Override
        public Try<T> toTry(Function<Failure<T, F>, Throwable> throwableFunction) {
            return Try.success(value);
        }

        @Override
        public Attempt<T, F> thenAccept(Consumer<Either<T, F>> consumer) {
            consumer.accept(Either.left(value));
            return this;
        }

        @Override
        public Attempt<T, F> peek(Consumer<T> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public <U> Attempt<U, F> map(Function<? super T, ? extends U> mapper) {
            return new Success<>(mapper.apply(value));
        }

        @Override
        public <U> Attempt<U, F> map(Supplier<? extends U> mapper) {
            return new Success<>(mapper.get());
        }

        @Override
        public <U> Attempt<U, F> mapAttempt(Function<? super T, Attempt<U, F>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public <U> Attempt<U, F> mapAttempt(Supplier<Attempt<U, F>> mapper) {
            return mapper.get();
        }

        @Override
        public Attempt<T, F> failIf(Predicate<? super T> predicate, Supplier<Attempt.Failure<T, F>> failureAttempt) {
            return predicate.test(value) ? failureAttempt.get() : this;
        }

        @Override
        public Attempt<T, F> failIf(Predicate<? super T> predicate, Function<? super T, Failure<T, F>> failureAttempt) {
            return predicate.test(value) ? failureAttempt.apply(value) : this;
        }

        @Override
        public <UF extends FailureReason> Attempt<T, UF> transform(UF failureReason) {
            return changeFailureType();
        }

        @Override
        public <U> U transform(Function<T, U> successMapper, Function<F, U> failureMapper) {
            return successMapper.apply(value);
        }

        @Override
        public <N> N fold(Function<Failure<T, F>, N> failureMapper, Function<T, N> successMapper) {
            return successMapper.apply(value);
        }

        @Override
        public Attempt<T, F> onSuccess(Runnable runnable) {
            runnable.run();
            return this;
        }

        @Override
        public Attempt<T, F> onSuccess(Consumer<T> consumer) {
            consumer.accept(value);
            return this;
        }

        @Override
        public Attempt<T, F> onFailure(Runnable runnable) {
            return this;
        }

        @Override
        public Attempt<T, F> onFailure(Consumer<Failure<T, F>> consumer) {
            return this;
        }

        @Override
        public Attempt<T, F> onFailureReason(Consumer<F> consumer) {
            return this;
        }

        private <UF extends FailureReason> Attempt<T, UF> changeFailureType() {
            @SuppressWarnings("unchecked")
            Attempt<T, UF> mappedSuccess = (Attempt<T, UF>) this;
            return mappedSuccess;
        }

        @Override
        public String toString() {
            return "Success{"
                    + "value=" + value
                    + '}';
        }
    }

    final class Failure<T, F extends FailureReason> implements Attempt<T, F> {
        private final F failureReason;
        private final Message message;
        private final Failure<?, ?> causeBy;

        Failure(F failureReason, Message message) {
            this(failureReason, message, null);
        }

        Failure(Failure<?, F> failure) {
            this(failure.failureReason, failure.message, failure.causeBy);
        }

        Failure(F failureReason, Message message, Failure<?, ?> causeBy) {
            this.failureReason = failureReason;
            this.message = message;
            this.causeBy = causeBy;
        }

        @Override
        public T get() {
            throw new UnsupportedOperationException("No value as attempt is a failure");
        }

        @Override
        public T getOrNull() {
            return null;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public <X extends Throwable> T getOrThrow(Function<Failure<T, F>, X> exceptionSupplier) throws X {
            throw exceptionSupplier.apply(this);
        }

        @Override
        public F getFailureReason() {
            return failureReason;
        }

        @Override
        public Message getFailureMessage() {
            return message;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public Try<T> toTry() {
            return Try.failure(new MultiverseException(message));
        }

        @Override
        public Try<T> toTry(Function<Failure<T, F>, Throwable> throwableFunction) {
            return Try.failure(throwableFunction.apply(this));
        }

        @Override
        public Attempt<T, F> thenAccept(Consumer<Either<T, F>> consumer) {
            consumer.accept(Either.right(failureReason));
            return this;
        }

        @Override
        public Attempt<T, F> peek(Consumer<T> consumer) {
            return this;
        }

        @Override
        public <U> Attempt<U, F> map(Function<? super T, ? extends U> mapper) {
            return changeValueType();
        }

        @Override
        public <U> Attempt<U, F> map(Supplier<? extends U> mapper) {
            return changeValueType();
        }

        @Override
        public <U> Attempt<U, F> mapAttempt(Function<? super T, Attempt<U, F>> mapper) {
            return changeValueType();
        }

        @Override
        public <U> Attempt<U, F> mapAttempt(Supplier<Attempt<U, F>> mapper) {
            return changeValueType();
        }

        @Override
        public Attempt<T, F> failIf(Predicate<? super T> predicate, Supplier<Attempt.Failure<T, F>> failureAttempt) {
            return this;
        }

        @Override
        public Attempt<T, F> failIf(Predicate<? super T> predicate, Function<? super T, Failure<T, F>> failureAttempt) {
            return this;
        }

        @Override
        public <UF extends FailureReason> Attempt<T, UF> transform(UF failureReason) {
            return new Failure<>(failureReason, getFailureMessage(), this);
        }

        @Override
        public <U> U transform(Function<T, U> successMapper, Function<F, U> failureMapper) {
            return failureMapper.apply(failureReason);
        }

        @Override
        public <N> N fold(Function<Failure<T, F>, N> failureMapper, Function<T, N> successMapper) {
            return failureMapper.apply(this);
        }

        @Override
        public Attempt<T, F> onSuccess(Runnable runnable) {
            return this;
        }

        @Override
        public Attempt<T, F> onSuccess(Consumer<T> consumer) {
            return this;
        }

        @Override
        public Attempt<T, F> onFailure(Runnable runnable) {
            runnable.run();
            return this;
        }

        @Override
        public Attempt<T, F> onFailure(Consumer<Failure<T, F>> consumer) {
            consumer.accept(this);
            return this;
        }

        @Override
        public Attempt<T, F> onFailureReason(Consumer<F> consumer) {
            consumer.accept(failureReason);
            return this;
        }

        private <U> Attempt<U, F> changeValueType() {
            @SuppressWarnings("unchecked")
            Attempt<U, F> mappedFailure = (Attempt<U, F>) this;
            return mappedFailure;
        }

        @Override
        public String toString() {
            return "Failure{"
                    + "reason=" + failureReason
                    + (causeBy != null ? ", causeBy=" + causeBy : "")
                    + '}';
        }
    }
}

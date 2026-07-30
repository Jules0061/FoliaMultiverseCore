package org.mvplugins.multiverse.core.utils.position;

import io.vavr.control.Try;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.exceptions.utils.position.PositionParseException;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@ApiStatus.AvailableSince("5.3")
public sealed interface PositionNumber permits PositionNumber.Relative, PositionNumber.Absolute {

    @ApiStatus.AvailableSince("5.3")
    static PositionNumber ofAbsolute(double value) {
        return new Absolute(value);
    }

    @ApiStatus.AvailableSince("5.3")
    static PositionNumber ofRelative(double value) {
        return new Relative(value);
    }

    @ApiStatus.AvailableSince("5.3")
    static PositionNumber fromString(String string) throws PositionParseException {
        if (string.startsWith("~")) {
            if (string.length() == 1) {
                return new Relative(0);
            }
            return new Relative(tryParseDouble(string.substring(1)));
        }
        return new Absolute(tryParseDouble(string));
    }

    private static double tryParseDouble(String str) throws PositionParseException {
        return Try.of(() -> Double.parseDouble(str))
                .getOrElseThrow(throwable -> new PositionParseException(
                        Message.of(MVCorei18n.EXCEPTION_POSITIONPARSE_INVALIDNUMBER,
                                replace("{number}").with(str))));
    }

    @ApiStatus.AvailableSince("5.3")
    double getValue(double base);

    @ApiStatus.AvailableSince("5.3")
    boolean isRelative();

    @ApiStatus.AvailableSince("5.3")
    boolean isAbsolute();

    @ApiStatus.AvailableSince("5.3")
    double getRawValue();

    final class Relative implements PositionNumber {

        private final double value;

        Relative(double value) {
            this.value = value;
        }

        @Override
        public double getValue(double base) {
            return base + value;
        }

        @Override
        public boolean isRelative() {
            return true;
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }

        @Override
        public double getRawValue() {
            return value;
        }

        @Override
        public String toString() {
            return "~" + value;
        }
    }

     final class Absolute implements PositionNumber {

        private final double value;

        Absolute(double value) {
        this.value = value;
        }

        @Override
        public double getValue(double base) {
        return value;
        }

        @Override
        public boolean isRelative() {
            return false;
        }

        @Override
        public boolean isAbsolute() {
            return true;
        }

        @Override
        public double getRawValue() {
        return value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }
    }
}

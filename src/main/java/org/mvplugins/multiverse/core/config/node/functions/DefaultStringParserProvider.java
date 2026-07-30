package org.mvplugins.multiverse.core.config.node.functions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import co.aikar.commands.ACFUtil;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.mvplugins.multiverse.core.exceptions.MultiverseException;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

public final class DefaultStringParserProvider {

    private static final Map<Class<?>, NodeStringParser<?>> PARSERS = new HashMap<>();

    public static void addDefaultStringParser(Class<?> clazz, NodeStringParser<?> parser) {
        PARSERS.put(clazz, parser);
    }

    public static <T> NodeStringParser<T> getDefaultStringParser(Class<T> clazz) {
        if (clazz.isEnum()) {
            return (NodeStringParser<T>) ENUM_STRING_PARSER;
        }
        return (NodeStringParser<T>) PARSERS.get(clazz);
    }

    private static final NodeStringParser<Enum> ENUM_STRING_PARSER = (input, type) -> Try.of(
            () -> Enum.valueOf(type, input.toUpperCase(Locale.ENGLISH)));

    private static final NodeStringParser<String> STRING_STRING_PARSER = (input, type) -> Try.of(
            () -> input);

    private static final NodeStringParser<Boolean> BOOLEAN_STRING_PARSER = (input, type) -> Try.of(
            () -> switch (String.valueOf(input).toLowerCase(Locale.ENGLISH)) {
                case "t", "true", "on", "y", "yes", "1", "allow" -> true;
                case "f", "false", "off", "n", "no", "0", "deny" -> false;
                default -> throw new MultiverseException(Message.of(MVCorei18n.CONFIG_STRING_PARSER_INVALIDBOOLEAN,
                        replace("{input}").with(input)));
            });

    private static final NodeStringParser<Integer> INTEGER_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.parseInt(input))
            .flatMap(number -> Option.of(number).toTry(() -> new MultiverseException(Message.of(
                    MVCorei18n.CONFIG_STRING_PARSER_INVALIDINTEGER,
                    replace("{input}").with(input)))));

    private static final NodeStringParser<Double> DOUBLE_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.parseDouble(input))
            .flatMap(number -> Option.of(number).toTry(() -> new MultiverseException(Message.of(
                    MVCorei18n.CONFIG_STRING_PARSER_INVALIDDOUBLE,
                    replace("{input}").with(input)))));

    private static final NodeStringParser<Float> FLOAT_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.parseFloat(input))
            .flatMap(number -> Option.of(number).toTry(() -> new MultiverseException(Message.of(
                    MVCorei18n.CONFIG_STRING_PARSER_INVALIDFLOAT,
                    replace("{input}").with(input)))));

    private static final NodeStringParser<Long> LONG_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.parseLong(input))
            .flatMap(number -> Option.of(number).toTry(() -> new MultiverseException(Message.of(
                    MVCorei18n.CONFIG_STRING_PARSER_INVALIDLONG,
                    replace("{input}").with(input)))));

    static {
        addDefaultStringParser(String.class, STRING_STRING_PARSER);
        addDefaultStringParser(Boolean.class, BOOLEAN_STRING_PARSER);
        addDefaultStringParser(Integer.class, INTEGER_STRING_PARSER);
        addDefaultStringParser(Double.class, DOUBLE_STRING_PARSER);
        addDefaultStringParser(Float.class, FLOAT_STRING_PARSER);
        addDefaultStringParser(Long.class, LONG_STRING_PARSER);
    }

    private DefaultStringParserProvider() {
    }
}

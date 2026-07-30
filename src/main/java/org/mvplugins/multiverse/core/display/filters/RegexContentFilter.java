package org.mvplugins.multiverse.core.display.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.utils.text.ChatTextFormatter;

public class RegexContentFilter implements ContentFilter {

    private static final Pattern REGEX_SPECIAL_CHARS = Pattern.compile("[.+*?\\[^\\]$(){}=!<>|:-\\\\]");

    @NotNull
    public static RegexContentFilter fromString(@Nullable String filterString) {
        if (filterString == null) {
            return new RegexContentFilter(null);
        }
        if (filterString.startsWith("r=")) {
            return new RegexContentFilter(filterString.substring(2));
        }
        String cleanedFilter = REGEX_SPECIAL_CHARS.matcher(filterString.toLowerCase()).replaceAll("\\\\$0");
        return new RegexContentFilter(cleanedFilter);
    }

    private final String regexString;
    private Pattern regexPattern;

    RegexContentFilter(@Nullable String regexString) {
        this.regexString = regexString;
        convertToPattern();
    }

    private void convertToPattern() {
        if (Strings.isNullOrEmpty(regexString)) {
            return;
        }
        try {
            regexPattern = Pattern.compile(regexString);
        } catch (PatternSyntaxException ignored) {
            regexPattern = null;
            Logging.fine("Error parsing regex: %s", regexString);
        }
    }

    @Override
    public boolean checkMatch(String value) {
        if (!hasValidRegex()) {
            return false;
        }
        String text = ChatTextFormatter.removeColor(String.valueOf(value));
        if (text == null) {
            return false;
        }
        try {
            return regexPattern.matcher(text.toLowerCase()).find();
        } catch (PatternSyntaxException ignored) {
            Logging.warning("Error parsing regex '%s' for input '%s'", regexString, text);
            return false;
        }
    }

    @Override
    public boolean needToFilter() {
        return hasValidRegex();
    }

    public boolean hasValidRegex() {
        return regexPattern != null;
    }

    public String getRegexString() {
        return regexString;
    }

    public Pattern getRegexPattern() {
        return regexPattern;
    }

    @Override
    public String toString() {
        return regexString;
    }
}

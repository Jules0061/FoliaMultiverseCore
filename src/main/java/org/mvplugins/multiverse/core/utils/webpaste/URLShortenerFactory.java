package org.mvplugins.multiverse.core.utils.webpaste;

public class URLShortenerFactory {
    private URLShortenerFactory() { }

    public static URLShortener getService(URLShortenerType type) {
        if (type == URLShortenerType.BITLY) {
            try {
                return new BitlyURLShortener();
            } catch (UnsupportedOperationException ignored) {}
        }

        return null;
    }
}

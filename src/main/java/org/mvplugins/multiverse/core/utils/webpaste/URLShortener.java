package org.mvplugins.multiverse.core.utils.webpaste;

public abstract sealed class URLShortener extends HttpAPIClient permits BitlyURLShortener {
    URLShortener(String url) {
        super(url);
    }

    URLShortener(String url, String accessToken) {
        super(url, accessToken);
    }

    public abstract String shorten(String longUrl);
}

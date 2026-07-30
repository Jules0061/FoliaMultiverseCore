package org.mvplugins.multiverse.core.utils.webpaste;

import java.util.Map;

public abstract sealed class PasteService extends HttpAPIClient
        permits GitHubPasteService,
                McloGsPasteService,
                PastebinPasteService,
                PasteGGPasteService,
                PastesDevPasteService {
    PasteService(String url) {
        super(url);
    }

    PasteService(String url, String accessToken) {
        super(url, accessToken);
    }

    public abstract String postData(String data) throws PasteFailedException;

    public abstract String postData(Map<String, String> data) throws PasteFailedException;

    public abstract boolean supportsMultiFile();
}

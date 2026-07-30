package org.mvplugins.multiverse.core.utils.webpaste;

public class PasteServiceFactory {
    private PasteServiceFactory() { }

    public static PasteService getService(PasteServiceType type, boolean isPrivate) {
        return switch (type) {
            case PASTEGG -> new PasteGGPasteService(isPrivate);
            case PASTEBIN -> new PastebinPasteService(isPrivate);
            case PASTESDEV -> new PastesDevPasteService();
            case GITHUB -> new GitHubPasteService(isPrivate);
            case MCLOGS -> new McloGsPasteService();
        };
    }
}

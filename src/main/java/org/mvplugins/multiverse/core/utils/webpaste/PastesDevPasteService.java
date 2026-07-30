package org.mvplugins.multiverse.core.utils.webpaste;

import java.io.IOException;
import java.util.Map;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

final class PastesDevPasteService extends PasteService {
    private static final String PASTESDEV_POST_REQUEST = "https://api.pastes.dev/post";

    PastesDevPasteService() {
        super(PASTESDEV_POST_REQUEST);
    }

    @Override
    String encodeData(String data) {
        return data;
    }

    @Override
    String encodeData(Map<String, String> data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String postData(String data) throws PasteFailedException {
        try {
            String stringJSON = this.exec(encodeData(data), ContentType.JSON);
            return DUMPS_VIEWER_URL +
                    ((JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(stringJSON)).get("key");
        } catch (IOException | ParseException e) {
            throw new PasteFailedException(e);
        }
    }

    @Override
    public String postData(Map<String, String> data) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsMultiFile() {
        return false;
    }
}

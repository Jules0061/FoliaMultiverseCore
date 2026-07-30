package org.mvplugins.multiverse.core.utils.webpaste;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

abstract sealed class HttpAPIClient permits PasteService, URLShortener {

    static final String DUMPS_VIEWER_URL = "https://mvdumps.c0ding.party/";

    private final String url;
    private final String accessToken;

    enum ContentType {
        JSON,
        PLAINTEXT,
        PLAINTEXT_YAML,
        URLENCODED
    }

    HttpAPIClient(String url) {
        this(url, null);
    }

    HttpAPIClient(String url, String accessToken) {
        this.url = url;
        this.accessToken = accessToken;
    }

    private String getContentHeader(ContentType type) {
        return switch (type) {
            case JSON -> "application/json; charset=utf-8";
            case PLAINTEXT -> "text/plain; charset=utf-8";
            case PLAINTEXT_YAML -> "text/yaml";
            case URLENCODED -> "application/x-www-form-urlencoded; charset=utf-8";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    abstract String encodeData(String data);

    abstract String encodeData(Map<String, String> data);

    final String exec(String payload, ContentType type) throws IOException {
        BufferedReader bufferedReader = null;
        OutputStreamWriter streamWriter = null;

        try {
            HttpsURLConnection connection = getHttpsURLConnection(type);

            streamWriter = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8.newEncoder());
            streamWriter.write(payload);
            streamWriter.flush();

            String line;
            StringBuilder responseString = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            while ((line = bufferedReader.readLine()) != null) {
                responseString.append(line);
            }

            return responseString.toString();
        } finally {
            if (streamWriter != null) {
                try {
                    streamWriter.close();
                } catch (IOException ignore) { }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignore) { }
            }
        }
    }

    private @NotNull HttpsURLConnection getHttpsURLConnection(ContentType type) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(this.url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        connection.addRequestProperty("Accept", "*/*");
        connection.addRequestProperty("User-Agent", "multiverse/dumps");
        connection.addRequestProperty("Content-Type", getContentHeader(type));
        if (this.accessToken != null) {
            connection.addRequestProperty("Authorization", this.accessToken);
        }
        return connection;
    }
}

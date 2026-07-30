package org.mvplugins.multiverse.core.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MVDumpsDebugInfoEvent extends Event {

    private final StringBuilder debugInfoBuilder;
    private final Map<String, String> detailedDebugInfo;

    public MVDumpsDebugInfoEvent() {
        debugInfoBuilder = new StringBuilder();
        detailedDebugInfo = new LinkedHashMap<>();
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public String getDebugInfo() {
        return this.debugInfoBuilder.toString();
    }

    public Map<String, String> getDetailedDebugInfo() {
        return Collections.unmodifiableMap(this.detailedDebugInfo);
    }

    public void appendDebugInfo(String moreVersionInfo) {
        this.debugInfoBuilder.append(moreVersionInfo);
    }

    private String readFile(final String filename) {
        StringBuilder result;

        try {
            FileReader reader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            result = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            Logging.severe("Unable to find %s. Here's the traceback: %s", filename, e.getMessage());
            e.printStackTrace();
            result = new StringBuilder(String.format("ERROR: Could not load: %s", filename));
        } catch (IOException e) {
            Logging.severe("Something bad happend when reading %s. Here's the traceback: %s", filename, e.getMessage());
            e.printStackTrace();
            result = new StringBuilder(String.format("ERROR: Could not load: %s", filename));
        }

        return result.toString();
    }

    public void putDetailedDebugInfo(String fileName, String contents) {
        this.detailedDebugInfo.put(fileName, contents);
    }

    public void putDetailedDebugInfo(String filename, File file) {
        this.putDetailedDebugInfo(filename, readFile(file.getAbsolutePath()));
    }
}

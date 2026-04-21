package com.ic3dwtf.pingnametag.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PingNametagConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("nametag-ping.json");

    private static PingNametagConfig config = new PingNametagConfig();

    private PingNametagConfigManager() {
    }

    public static PingNametagConfig get() {
        return config;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save(config);
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            PingNametagConfig loaded = GSON.fromJson(reader, PingNametagConfig.class);
            config = loaded == null ? new PingNametagConfig() : loaded;
        } catch (IOException | JsonParseException ignored) {
            config = new PingNametagConfig();
        }

        config.sanitize();
        save(config);
    }

    public static void save(PingNametagConfig toSave) {
        PingNametagConfig next = toSave == null ? new PingNametagConfig() : toSave;
        next.sanitize();

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(next, writer);
            }
            config = next;
        } catch (IOException ignored) {
            config = next;
        }
    }
}

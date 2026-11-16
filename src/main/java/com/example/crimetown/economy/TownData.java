package com.example.crimetown.economy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * World-global state of the town, persisted as {@code <world>/crimetown.json}.
 * Plain JSON on purpose: it avoids depending on the SavedData API, which changes between versions.
 */
public class TownData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TownData instance;
    private static Path path;

    // exchange rates, expressed in gold ingots per one unit
    public double diamondValue = 8;
    public double netheriteValue = 64;
    public double prevDiamond = 8;
    public double prevNetherite = 64;
    public long lastDay = -1;
    public long lastConcertDay = -1;

    // town placement
    public boolean built;
    public int cx, cy, cz;
    public List<int[]> slots = new ArrayList<>();

    // ownership: business id -> owner UUID
    public Map<String, String> owners = new HashMap<>();
    public Map<String, Integer> buyCount = new HashMap<>();
    // owner UUID -> unclaimed netherite income
    public Map<String, Integer> pending = new HashMap<>();
    public String boss;
    public List<String> crownGiven = new ArrayList<>();

    public static TownData get() {
        return instance;
    }

    public static void load(MinecraftServer server) {
        path = server.getWorldPath(LevelResource.ROOT).resolve("crimetown.json");
        instance = null;
        try {
            if (Files.exists(path)) {
                instance = GSON.fromJson(Files.readString(path), TownData.class);
            }
        } catch (Exception e) {
            System.err.println("[CrimeTown] failed to read crimetown.json: " + e);
        }
        if (instance == null) instance = new TownData();
    }

    public static void unload() {
        if (instance != null) instance.save();
        instance = null;
    }

    public void save() {
        if (path == null) return;
        try {
            Files.writeString(path, GSON.toJson(this));
        } catch (Exception e) {
            System.err.println("[CrimeTown] failed to write crimetown.json: " + e);
        }
    }

    public boolean inTown(double x, double z) {
        return built && Math.abs(x - cx) <= 45 && Math.abs(z - cz) <= 45;
    }

    public boolean isSlot(int x, int y, int z) {
        for (int[] s : slots) {
            if (s[0] == x && s[1] == y && s[2] == z) return true;
        }
        return false;
    }
}

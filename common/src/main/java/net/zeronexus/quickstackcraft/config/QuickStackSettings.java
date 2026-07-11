package net.zeronexus.quickstackcraft.config;

import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Loader-neutral server configuration used by storage discovery and transfer requests.
 */
public final class QuickStackSettings {

    public enum StorageDetection {
        RECOGNIZED_STORAGE,
        ANY_ITEM_INVENTORY
    }

    private static final int DEFAULT_SEARCH_RADIUS = 8;
    private static final int DEFAULT_CAPACITY_THRESHOLD = 9;
    private static final String DEFAULT_OUTLINE_RGB = "FFD700";

    public static volatile StorageDetection storageDetection = StorageDetection.RECOGNIZED_STORAGE;
    public static volatile boolean capacityFallbackEnabled;
    public static volatile int capacityFallbackThreshold = DEFAULT_CAPACITY_THRESHOLD;
    public static volatile int searchRadius = DEFAULT_SEARCH_RADIUS;
    public static volatile Set<ResourceLocation> configuredBlacklist = builtInHazards();
    public static volatile Set<ResourceLocation> configuredWhitelist = Set.of();
    public static volatile String outlineRgb = DEFAULT_OUTLINE_RGB;
    public static volatile double outlineOpacity = 0.8D;
    public static volatile int outlineLifetimeMs = 3000;

    private static volatile Path settingsPath;

    public record Snapshot(
            StorageDetection storageDetection,
            boolean capacityFallbackEnabled,
            int capacityFallbackThreshold,
            int searchRadius,
            String outlineRgb,
            double outlineOpacity,
            int outlineLifetimeMs) {
    }

    private QuickStackSettings() {}

    public static void load(Path path) {
        settingsPath = path;
        resetDefaults();
        if (Files.notExists(path)) {
            persist();
            return;
        }

        Properties values = new Properties();
        try (Reader input = Files.newBufferedReader(path)) {
            values.load(input);
        } catch (IOException ignored) {
            return;
        }

        storageDetection = readDetection(values.getProperty("storageDetection"));
        capacityFallbackEnabled = readBoolean(values, "capacityFallback", false);
        capacityFallbackThreshold = boundedInt(
                values.getProperty("capacityThreshold"), DEFAULT_CAPACITY_THRESHOLD, 1, 256);
        searchRadius = boundedInt(values.getProperty("searchRadius"), DEFAULT_SEARCH_RADIUS, 1, 64);
        outlineRgb = validRgb(values.getProperty("outlineColor"), DEFAULT_OUTLINE_RGB);
        outlineOpacity = boundedDouble(values.getProperty("outlineOpacity"), 0.8D, 0.0D, 1.0D);
        outlineLifetimeMs = boundedInt(values.getProperty("outlineLifetimeMs"), 3000, 100, 60000);

        LinkedHashSet<ResourceLocation> blocked = new LinkedHashSet<>(builtInHazards());
        blocked.addAll(readIdentifiers(values.getProperty("storageBlacklist")));
        configuredBlacklist = Set.copyOf(blocked);
        configuredWhitelist = Set.copyOf(readIdentifiers(values.getProperty("storageWhitelist")));
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                storageDetection,
                capacityFallbackEnabled,
                capacityFallbackThreshold,
                searchRadius,
                outlineRgb,
                outlineOpacity,
                outlineLifetimeMs);
    }

    public static void apply(Snapshot requested) {
        Snapshot accepted = sanitize(requested);
        storageDetection = accepted.storageDetection();
        capacityFallbackEnabled = accepted.capacityFallbackEnabled();
        capacityFallbackThreshold = accepted.capacityFallbackThreshold();
        searchRadius = accepted.searchRadius();
        outlineRgb = accepted.outlineRgb();
        outlineOpacity = accepted.outlineOpacity();
        outlineLifetimeMs = accepted.outlineLifetimeMs();
        persist();
    }

    public static Snapshot sanitize(Snapshot requested) {
        if (requested == null) {
            return snapshot();
        }
        StorageDetection detection = requested.storageDetection() == null
                ? StorageDetection.RECOGNIZED_STORAGE
                : requested.storageDetection();
        return new Snapshot(
                detection,
                requested.capacityFallbackEnabled(),
                clamp(requested.capacityFallbackThreshold(), 1, 256),
                clamp(requested.searchRadius(), 1, 64),
                validRgb(requested.outlineRgb(), DEFAULT_OUTLINE_RGB),
                clamp(requested.outlineOpacity(), 0.0D, 1.0D),
                clamp(requested.outlineLifetimeMs(), 100, 60000));
    }

    public static void save() {
        persist();
    }

    static void resetDefaults() {
        storageDetection = StorageDetection.RECOGNIZED_STORAGE;
        capacityFallbackEnabled = false;
        capacityFallbackThreshold = DEFAULT_CAPACITY_THRESHOLD;
        searchRadius = DEFAULT_SEARCH_RADIUS;
        configuredBlacklist = builtInHazards();
        configuredWhitelist = Set.of();
        outlineRgb = DEFAULT_OUTLINE_RGB;
        outlineOpacity = 0.8D;
        outlineLifetimeMs = 3000;
    }

    private static void persist() {
        Path path = settingsPath;
        if (path == null) {
            return;
        }
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Properties values = new Properties();
            values.setProperty("storageDetection", storageDetection.name());
            values.setProperty("capacityFallback", Boolean.toString(capacityFallbackEnabled));
            values.setProperty("capacityThreshold", Integer.toString(capacityFallbackThreshold));
            values.setProperty("searchRadius", Integer.toString(searchRadius));
            values.setProperty("outlineColor", outlineRgb);
            values.setProperty("outlineOpacity", Double.toString(outlineOpacity));
            values.setProperty("outlineLifetimeMs", Integer.toString(outlineLifetimeMs));
            values.setProperty("storageBlacklist", joinIdentifiers(configuredBlacklist));
            values.setProperty("storageWhitelist", joinIdentifiers(configuredWhitelist));
            try (Writer output = Files.newBufferedWriter(path)) {
                values.store(output, "QuickStack & Craft server settings");
            }
        } catch (IOException ignored) {
        }
    }

    private static StorageDetection readDetection(String raw) {
        if (raw == null) {
            return StorageDetection.RECOGNIZED_STORAGE;
        }
        try {
            return StorageDetection.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return StorageDetection.RECOGNIZED_STORAGE;
        }
    }

    private static boolean readBoolean(Properties values, String key, boolean fallback) {
        String raw = values.getProperty(key);
        return raw == null ? fallback : Boolean.parseBoolean(raw.trim());
    }

    private static int boundedInt(String raw, int fallback, int minimum, int maximum) {
        try {
            return clamp(raw == null ? fallback : Integer.parseInt(raw.trim()), minimum, maximum);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double boundedDouble(String raw, double fallback, double minimum, double maximum) {
        try {
            return clamp(raw == null ? fallback : Double.parseDouble(raw.trim()), minimum, maximum);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String validRgb(String raw, String fallback) {
        if (raw == null) {
            return fallback;
        }
        String candidate = raw.trim().replaceFirst("^#", "");
        return candidate.matches("[0-9a-fA-F]{6}") ? candidate.toUpperCase() : fallback;
    }

    private static Set<ResourceLocation> readIdentifiers(String raw) {
        LinkedHashSet<ResourceLocation> identifiers = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) {
            return identifiers;
        }
        Arrays.stream(raw.split("[,;\\s]+"))
                .map(String::trim)
                .map(ResourceLocation::tryParse)
                .filter(java.util.Objects::nonNull)
                .forEach(identifiers::add);
        return identifiers;
    }

    private static Set<ResourceLocation> builtInHazards() {
        return Set.of(
                id("minecraft", "furnace"),
                id("minecraft", "blast_furnace"),
                id("minecraft", "smoker"),
                id("minecraft", "brewing_stand"),
                id("minecraft", "crafter"),
                id("trashcans", "item_trash_can"),
                id("trashcans", "ultimate_trash_can"));
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static String joinIdentifiers(Set<ResourceLocation> identifiers) {
        return identifiers.stream().map(ResourceLocation::toString).sorted()
                .collect(java.util.stream.Collectors.joining(","));
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

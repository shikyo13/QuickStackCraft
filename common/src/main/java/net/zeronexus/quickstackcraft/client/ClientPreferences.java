package net.zeronexus.quickstackcraft.client;

import dev.architectury.platform.Platform;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ClientPreferences {

    public static final String DEFAULT_RGB = "FFD700";
    public static final double DEFAULT_OPACITY = 0.8D;
    public static final int DEFAULT_LIFETIME_MS = 3000;

    private static String outlineRgb = DEFAULT_RGB;
    private static double outlineOpacity = DEFAULT_OPACITY;
    private static int outlineLifetimeMs = DEFAULT_LIFETIME_MS;
    private static boolean initialized;

    private ClientPreferences() {}

    public static synchronized void load() {
        if (initialized) {
            return;
        }
        initialized = true;

        Path path = preferencesPath();
        if (Files.notExists(path)) {
            outlineRgb = sanitizeRgb(QuickStackSettings.outlineRgb, DEFAULT_RGB);
            outlineOpacity = clamp(QuickStackSettings.outlineOpacity, 0.0D, 1.0D);
            outlineLifetimeMs = clamp(QuickStackSettings.outlineLifetimeMs, 100, 60000);
            writeFile();
            return;
        }

        Properties values = new Properties();
        try (Reader input = Files.newBufferedReader(path)) {
            values.load(input);
            outlineRgb = sanitizeRgb(values.getProperty("outlineColor"), DEFAULT_RGB);
            outlineOpacity = clamp(number(values.getProperty("outlineOpacity"), DEFAULT_OPACITY), 0.0D, 1.0D);
            outlineLifetimeMs = clamp(integer(values.getProperty("outlineLifetimeMs"), DEFAULT_LIFETIME_MS), 100, 60000);
        } catch (IOException ignored) {
        }
    }

    public static synchronized void apply(Snapshot requested) {
        outlineRgb = sanitizeRgb(requested.rgb(), DEFAULT_RGB);
        outlineOpacity = clamp(requested.opacity(), 0.0D, 1.0D);
        outlineLifetimeMs = clamp(requested.lifetimeMs(), 100, 60000);
        writeFile();
    }

    public static Snapshot snapshot() {
        load();
        return new Snapshot(outlineRgb, outlineOpacity, outlineLifetimeMs);
    }

    public static String outlineRgb() {
        load();
        return outlineRgb;
    }

    public static double outlineOpacity() {
        load();
        return outlineOpacity;
    }

    public static int outlineLifetimeMs() {
        load();
        return outlineLifetimeMs;
    }

    public static String sanitizeRgb(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String candidate = value.trim().replaceFirst("^#", "");
        return candidate.matches("[0-9a-fA-F]{6}") ? candidate.toUpperCase() : fallback;
    }

    private static void writeFile() {
        Path path = preferencesPath();
        try {
            Files.createDirectories(path.getParent());
            Properties values = new Properties();
            values.setProperty("outlineColor", outlineRgb);
            values.setProperty("outlineOpacity", Double.toString(outlineOpacity));
            values.setProperty("outlineLifetimeMs", Integer.toString(outlineLifetimeMs));
            try (Writer output = Files.newBufferedWriter(path)) {
                values.store(output, "QuickStack & Craft visual preferences");
            }
        } catch (IOException ignored) {
        }
    }

    private static Path preferencesPath() {
        return Platform.getConfigFolder().resolve("quickstackcraft-client.properties");
    }

    private static int integer(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double number(String value, double fallback) {
        try {
            return value == null ? fallback : Double.parseDouble(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public record Snapshot(String rgb, double opacity, int lifetimeMs) {
    }
}

package net.zeronexus.quickstackcraft.logic;

import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Server-owned block-type whitelist and blacklist edited through the in-game UI, keybind, or commands.
 */
public final class StorageBlockLists {

    private static final Set<ResourceLocation> whitelisted = new CopyOnWriteArraySet<>();
    private static final Set<ResourceLocation> blacklisted = new CopyOnWriteArraySet<>();
    private static volatile Path storageFile;

    private StorageBlockLists() {}

    public static void load(Path file) {
        storageFile = file;
        whitelisted.clear();
        blacklisted.clear();

        Path source = Files.exists(file)
                ? file
                : file.resolveSibling("quickstackcraft-targets.properties");
        if (!Files.exists(source)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(source)) {
            properties.load(reader);
        } catch (IOException ignored) {
            return;
        }

        whitelisted.addAll(parseIds(properties.getProperty("whitelist")));
        blacklisted.addAll(parseIds(properties.getProperty("blacklist")));
        blacklisted.removeAll(whitelisted);
        if (!source.equals(file)) {
            save();
        }
    }

    public static boolean isWhitelisted(ResourceLocation id) {
        return whitelisted.contains(id);
    }

    public static boolean isBlacklisted(ResourceLocation id) {
        return blacklisted.contains(id);
    }

    public static StorageListState state(ResourceLocation id) {
        if (whitelisted.contains(id)) {
            return StorageListState.WHITELISTED;
        }
        if (blacklisted.contains(id)) {
            return StorageListState.BLACKLISTED;
        }
        return StorageListState.DEFAULT;
    }

    public static StorageListState cycle(ResourceLocation id) {
        StorageListState next = state(id).next();
        whitelisted.remove(id);
        blacklisted.remove(id);
        if (next == StorageListState.WHITELISTED) {
            whitelisted.add(id);
        } else if (next == StorageListState.BLACKLISTED) {
            blacklisted.add(id);
        }
        save();
        return next;
    }

    public static boolean whitelist(ResourceLocation id) {
        boolean changed = blacklisted.remove(id);
        changed |= whitelisted.add(id);
        if (changed) {
            save();
        }
        return changed;
    }

    public static boolean removeFromWhitelist(ResourceLocation id) {
        boolean changed = whitelisted.remove(id);
        if (changed) {
            save();
        }
        return changed;
    }

    public static boolean blacklist(ResourceLocation id) {
        boolean changed = whitelisted.remove(id);
        changed |= blacklisted.add(id);
        if (changed) {
            save();
        }
        return changed;
    }

    public static boolean removeFromBlacklist(ResourceLocation id) {
        boolean changed = blacklisted.remove(id);
        if (changed) {
            save();
        }
        return changed;
    }

    public static void clearWhitelist() {
        if (!whitelisted.isEmpty()) {
            whitelisted.clear();
            save();
        }
    }

    public static void clearBlacklist() {
        if (!blacklisted.isEmpty()) {
            blacklisted.clear();
            save();
        }
    }

    public static void replace(Set<ResourceLocation> whitelist, Set<ResourceLocation> blacklist) {
        whitelisted.clear();
        blacklisted.clear();
        whitelisted.addAll(whitelist);
        blacklisted.addAll(blacklist);
        blacklisted.removeAll(whitelisted);
        save();
    }

    public static Set<ResourceLocation> whitelist() {
        return Set.copyOf(whitelisted);
    }

    public static Set<ResourceLocation> blacklist() {
        return Set.copyOf(blacklisted);
    }

    static void resetForTests() {
        whitelisted.clear();
        blacklisted.clear();
        storageFile = null;
    }

    private static void save() {
        Path file = storageFile;
        if (file == null) {
            return;
        }

        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Properties properties = new Properties();
            properties.setProperty("whitelist", joinIds(whitelisted));
            properties.setProperty("blacklist", joinIds(blacklisted));
            try (Writer writer = Files.newBufferedWriter(file)) {
                properties.store(writer, "QuickStack & Craft storage block whitelist and blacklist");
            }
        } catch (IOException ignored) {
        }
    }

    private static Set<ResourceLocation> parseIds(String raw) {
        Set<ResourceLocation> ids = new HashSet<>();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        for (String token : raw.split("[,;\\s]+")) {
            ResourceLocation id = ResourceLocation.tryParse(token.trim());
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static String joinIds(Set<ResourceLocation> ids) {
        return String.join(",", ids.stream().map(ResourceLocation::toString).sorted().toList());
    }
}

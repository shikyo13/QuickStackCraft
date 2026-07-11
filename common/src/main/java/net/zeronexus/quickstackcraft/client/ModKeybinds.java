package net.zeronexus.quickstackcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.List;

/**
 * Keybind definitions shared across platforms.
 * Registration is platform-specific (Fabric: KeyBindingHelper, NeoForge: RegisterKeyMappingsEvent).
 */
public final class ModKeybinds {

    public static final String CATEGORY = "key.categories.quickstackcraft";

    public static final KeyMapping QUICK_STACK = unbound("quick_stack");
    public static final KeyMapping DUMP_ALL = unbound("dump_all");
    public static final KeyMapping PREVIEW_STORAGE = unbound("preview_storage");
    public static final KeyMapping CYCLE_STORAGE_LIST = unbound("cycle_storage_list");
    public static final KeyMapping CONFIG = unbound("config");

    private static final List<KeyMapping> ALL = List.of(
            QUICK_STACK, DUMP_ALL, PREVIEW_STORAGE, CYCLE_STORAGE_LIST, CONFIG);

    private ModKeybinds() {}

    public static List<KeyMapping> all() {
        return ALL;
    }

    private static KeyMapping unbound(String action) {
        return new KeyMapping(
                "key.quickstackcraft." + action,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY);
    }
}

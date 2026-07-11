package net.zeronexus.quickstackcraft.compat;

/**
 * Soft-integration matching rules for optional mods.
 *
 * <p>These helpers intentionally use class-name strings instead of compile-time references so
 * QuickStack & Craft can support optional UI/storage mods without depending on their jars.
 */
public final class ClassNameRules {

    private static final String SOPHISTICATED_CORE_STORAGE_SLOT =
            "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageInventorySlot";

    private ClassNameRules() {}

    public static boolean isSophisticatedStorageSlot(String className) {
        return SOPHISTICATED_CORE_STORAGE_SLOT.equals(className);
    }

    public static boolean isSophisticatedStorageSlot(Class<?> slotClass) {
        for (Class<?> current = slotClass; current != null; current = current.getSuperclass()) {
            if (isSophisticatedStorageSlot(current.getName())) {
                return true;
            }
        }
        return false;
    }
}

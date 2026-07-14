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
    private static final String TRAVELERS_BACKPACK_STORAGE_SLOT =
            "com.tiviacz.travelersbackpack.inventory.menu.slot.BackpackSlotItemHandler";
    private static final String INMIS_BACKPACK_MENU =
            "draylar.inmis.ui.BackpackScreenHandler";

    private ClassNameRules() {}

    public static boolean isSophisticatedStorageSlot(String className) {
        return SOPHISTICATED_CORE_STORAGE_SLOT.equals(className);
    }

    public static boolean isSophisticatedStorageSlot(Class<?> slotClass) {
        return hasTypeInHierarchy(slotClass, SOPHISTICATED_CORE_STORAGE_SLOT);
    }

    public static boolean isTravelersBackpackStorageSlot(Class<?> slotClass) {
        return hasTypeInHierarchy(slotClass, TRAVELERS_BACKPACK_STORAGE_SLOT);
    }

    public static boolean isInmisBackpackMenu(Class<?> menuClass) {
        return hasTypeInHierarchy(menuClass, INMIS_BACKPACK_MENU);
    }

    private static boolean hasTypeInHierarchy(Class<?> type, String expectedName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (expectedName.equals(current.getName())) {
                return true;
            }
        }
        return false;
    }
}

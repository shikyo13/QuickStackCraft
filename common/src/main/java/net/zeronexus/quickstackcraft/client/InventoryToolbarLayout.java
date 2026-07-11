package net.zeronexus.quickstackcraft.client;

public final class InventoryToolbarLayout {

    public static final int OFFSET_X = 126;
    public static final int OFFSET_Y = 62;
    public static final int BUTTON_SIZE = 14;
    public static final int BUTTON_GAP = 1;
    public static final String QUICK_STACK_LABEL = "Q";
    public static final String DUMP_LABEL = "D";

    private InventoryToolbarLayout() {}

    public static int buttonX(int inventoryLeft, int index) {
        return inventoryLeft + OFFSET_X + index * (BUTTON_SIZE + BUTTON_GAP);
    }

    public static int buttonY(int inventoryTop) {
        return inventoryTop + OFFSET_Y;
    }

    public static double scaledButtonCenterX(int guiX, float scale, int index) {
        return guiX + (OFFSET_X + index * (BUTTON_SIZE + BUTTON_GAP) + BUTTON_SIZE / 2.0D) * scale;
    }

    public static double scaledButtonCenterY(int guiY, float scale) {
        return guiY + (OFFSET_Y + BUTTON_SIZE / 2.0D) * scale;
    }
}

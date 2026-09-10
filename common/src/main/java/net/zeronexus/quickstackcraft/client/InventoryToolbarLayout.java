package net.zeronexus.quickstackcraft.client;

public final class InventoryToolbarLayout {

    public static final int OFFSET_X = 124;
    public static final int OFFSET_Y = 62;
    public static final int BUTTON_SIZE = 12;
    public static final int BUTTON_GAP = 1;
    public static final String QUICK_STACK_LABEL = "Q";
    public static final String RESTOCK_LABEL = "R";
    public static final String DUMP_LABEL = "D";

    private InventoryToolbarLayout() {}

    public static int buttonX(int inventoryLeft, int index) {
        return inventoryLeft + OFFSET_X + index * (BUTTON_SIZE + BUTTON_GAP);
    }

    public static int buttonY(int inventoryTop) {
        return inventoryTop + OFFSET_Y;
    }

    public static Position position(int anchorX, int anchorY, int count, int size, int gap,
                                    int screenWidth, int screenHeight, ToolbarPreferences preferences) {
        int toolbarWidth = count * size + Math.max(0, count - 1) * gap;
        int x = Math.clamp(anchorX + preferences.offsetX(), 0, Math.max(0, screenWidth - toolbarWidth));
        int y = Math.clamp(anchorY + preferences.offsetY(), 0, Math.max(0, screenHeight - size));
        return new Position(x, y);
    }

    public record Position(int x, int y) {}

    public static double scaledButtonCenterX(int guiX, float scale, int index) {
        return guiX + (OFFSET_X + index * (BUTTON_SIZE + BUTTON_GAP) + BUTTON_SIZE / 2.0D) * scale;
    }

    public static double scaledButtonCenterY(int guiY, float scale) {
        return guiY + (OFFSET_Y + BUTTON_SIZE / 2.0D) * scale;
    }
}

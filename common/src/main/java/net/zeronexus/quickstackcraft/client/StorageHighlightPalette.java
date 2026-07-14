package net.zeronexus.quickstackcraft.client;

import net.zeronexus.quickstackcraft.logic.StorageListState;

public final class StorageHighlightPalette {

    private static final int WHITELISTED = 0xFF4FDB6B;
    private static final int BLACKLISTED = 0xFFF0454D;
    private static final int DEFAULT = 0xFF40C7EB;
    private static final int SOURCE = 0xFF45D4E8;

    private StorageHighlightPalette() {}

    public static int destinationArgb() {
        return destinationArgb(
                ClientPreferences.outlineRgb(), ClientPreferences.outlineOpacity());
    }

    static int destinationArgb(String colorHex, double alphaValue) {
        int rgb;
        try {
            rgb = Integer.parseInt(
                    ClientPreferences.sanitizeRgb(colorHex, ClientPreferences.DEFAULT_RGB), 16);
        } catch (NumberFormatException ignored) {
            rgb = Integer.parseInt(ClientPreferences.DEFAULT_RGB, 16);
        }
        int alpha = (int) Math.round(Math.max(0.0D,
                Math.min(1.0D, alphaValue)) * 255.0D);
        return alpha << 24 | rgb;
    }

    public static int listStateArgb(StorageListState state) {
        return switch (state) {
            case WHITELISTED -> WHITELISTED;
            case BLACKLISTED -> BLACKLISTED;
            case DEFAULT -> DEFAULT;
        };
    }

    public static float[] destinationRgb() {
        return rgb(destinationArgb());
    }

    public static int sourceArgb() {
        return SOURCE;
    }

    public static float[] sourceRgb() {
        return rgb(SOURCE);
    }

    public static float[] listStateRgb(StorageListState state) {
        return rgb(listStateArgb(state));
    }

    private static float[] rgb(int argb) {
        return new float[] {
                ((argb >> 16) & 0xFF) / 255.0F,
                ((argb >> 8) & 0xFF) / 255.0F,
                (argb & 0xFF) / 255.0F
        };
    }
}

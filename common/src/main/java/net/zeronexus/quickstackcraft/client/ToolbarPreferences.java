package net.zeronexus.quickstackcraft.client;

import java.util.Properties;

public record ToolbarPreferences(boolean visible, int offsetX, int offsetY) {

    public static final int MAX_OFFSET = 4096;
    public static final ToolbarPreferences DEFAULT = new ToolbarPreferences(true, 0, 0);

    public ToolbarPreferences {
        offsetX = Math.clamp(offsetX, -MAX_OFFSET, MAX_OFFSET);
        offsetY = Math.clamp(offsetY, -MAX_OFFSET, MAX_OFFSET);
    }

    static ToolbarPreferences read(Properties values, String prefix) {
        return new ToolbarPreferences(
                !"false".equalsIgnoreCase(values.getProperty(prefix + ".visible", "true").trim()),
                integer(values.getProperty(prefix + ".offsetX")),
                integer(values.getProperty(prefix + ".offsetY")));
    }

    void write(Properties values, String prefix) {
        values.setProperty(prefix + ".visible", Boolean.toString(visible));
        values.setProperty(prefix + ".offsetX", Integer.toString(offsetX));
        values.setProperty(prefix + ".offsetY", Integer.toString(offsetY));
    }

    private static int integer(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}

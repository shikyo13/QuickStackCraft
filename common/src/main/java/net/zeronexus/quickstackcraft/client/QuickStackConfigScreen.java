package net.zeronexus.quickstackcraft.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.zeronexus.quickstackcraft.client.tutorial.TutorialScreen;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;
import net.zeronexus.quickstackcraft.config.StorageTargetResolver;
import net.zeronexus.quickstackcraft.logic.ContainerScanner;
import net.zeronexus.quickstackcraft.network.ConfigRequestC2SPacket;
import net.zeronexus.quickstackcraft.network.ConfigSaveC2SPacket;
import net.zeronexus.quickstackcraft.network.ConfigSyncS2CPacket;
import net.zeronexus.quickstackcraft.network.StorageListActionC2SPacket;
import net.zeronexus.quickstackcraft.util.BlockSelection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class QuickStackConfigScreen extends AbstractContainerScreen<QuickStackConfigScreen.ConfigMenu> {

    private static final int PANEL_MAX_WIDTH = 360;
    private static final int PANEL_MAX_HEIGHT = 286;
    private static final int TARGET_ROW_HEIGHT = 20;
    private static final DropArea EMPTY_DROP_AREA = new DropArea(0, 0, 0, 0);
    private static final String[] COLOR_PRESETS = {
            "FFD700", "4FC3F7", "66D17A", "F06A6A", "FFFFFF", "B38CFF", "FF9F43", "7FDBD4"
    };

    private static QuickStackSettings.Snapshot cachedSettings = QuickStackSettings.snapshot();
    private static Set<ResourceLocation> cachedIncluded = Set.of();
    private static Set<ResourceLocation> cachedExcluded = Set.of();
    private static boolean cachedCanEdit;
    private static long cachedRevision;
    private static long nextRequestId = 1L;

    private final Screen parent;
    private final long requestId;
    private final List<ResourceLocation> includedTargets = new ArrayList<>();
    private final List<ResourceLocation> excludedTargets = new ArrayList<>();
    private final List<TargetRow> renderedTargetRows = new ArrayList<>();
    private final List<AbstractWidget> settingsWidgets = new ArrayList<>();

    private Tab activeTab = Tab.GENERAL;
    private TargetList compactTargetList = TargetList.INCLUDED;
    private QuickStackSettings.StorageDetection storageDetection;
    private boolean capacityFallback;
    private int capacityThreshold;
    private int searchRadius;
    private String outlineColor;
    private double outlineOpacity;
    private int outlineLifetimeMs;
    private boolean loaded;
    private boolean canEdit;
    private boolean dirtyServer;
    private boolean dirtyClient;
    private boolean saving;
    private boolean closeAfterSave;
    private long serverRevision;
    private int includedPage;
    private int excludedPage;
    private EditBox colorBox;
    private Component statusMessage = Component.empty();
    private DropArea includedDropArea = EMPTY_DROP_AREA;
    private DropArea excludedDropArea = EMPTY_DROP_AREA;
    private boolean renderingWidgets;

    public enum TargetList {
        INCLUDED,
        EXCLUDED
    }

    private enum Tab {
        GENERAL("quickstackcraft.config.tab.general"),
        STORAGE("quickstackcraft.config.tab.storage"),
        APPEARANCE("quickstackcraft.config.tab.appearance");

        private final String translationKey;

        Tab(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    public record DropArea(int x, int y, int width, int height) {
        public boolean contains(int pointX, int pointY) {
            return width > 0 && height > 0
                    && pointX >= x && pointX < x + width
                    && pointY >= y && pointY < y + height;
        }
    }

    private QuickStackConfigScreen(Screen parent, long requestId) {
        super(new ConfigMenu(), playerInventory(), Component.translatable("quickstackcraft.config.title"));
        this.parent = parent;
        this.requestId = requestId;
        loadServerState(cachedSettings, cachedIncluded, cachedExcluded, cachedCanEdit, cachedRevision);
        ClientPreferences.Snapshot client = ClientPreferences.snapshot();
        outlineColor = client.rgb();
        outlineOpacity = client.opacity();
        outlineLifetimeMs = client.lifetimeMs();
        loaded = false;
    }

    public static void open(Screen parent) {
        Minecraft.getInstance().setScreen(create(parent));
    }

    public static Screen create(Screen parent) {
        long requestId = nextRequestId++;
        NetworkManager.sendToServer(new ConfigRequestC2SPacket(requestId));
        return new QuickStackConfigScreen(parent, requestId);
    }

    public static void receive(ConfigSyncS2CPacket packet) {
        cachedSettings = packet.settings();
        cachedIncluded = Set.copyOf(packet.includedTargets());
        cachedExcluded = Set.copyOf(packet.excludedTargets());
        cachedCanEdit = packet.canEdit();
        cachedRevision = packet.revision();

        Screen active = Minecraft.getInstance().screen;
        if (!(active instanceof QuickStackConfigScreen screen)) {
            return;
        }
        if (packet.requestId() != screen.requestId) {
            return;
        }

        screen.loaded = true;
        screen.canEdit = packet.canEdit();
        screen.serverRevision = packet.revision();
        screen.saving = false;

        if (packet.reason() == ConfigSyncS2CPacket.SyncReason.REJECTED && screen.dirtyServer) {
            screen.closeAfterSave = false;
            screen.statusMessage = Component.translatable("quickstackcraft.config.reload_conflict");
            screen.rebuildSettingsWidgets();
            return;
        }

        screen.loadServerState(
                packet.settings(), packet.includedTargets(), packet.excludedTargets(),
                packet.canEdit(), packet.revision());
        screen.dirtyServer = false;
        screen.statusMessage = Component.empty();
        screen.rebuildSettingsWidgets();

        if (packet.reason() == ConfigSyncS2CPacket.SyncReason.SAVED && screen.closeAfterSave) {
            screen.closeNow();
        }
    }

    @Override
    protected void init() {
        Layout layout = layout();
        imageWidth = layout.panelWidth();
        imageHeight = layout.panelHeight();
        super.init();
        rebuildSettingsWidgets();
    }

    private void rebuildSettingsWidgets() {
        settingsWidgets.forEach(this::removeWidget);
        settingsWidgets.clear();
        renderedTargetRows.clear();
        includedDropArea = EMPTY_DROP_AREA;
        excludedDropArea = EMPTY_DROP_AREA;
        colorBox = null;

        Layout layout = layout();
        addTabs(layout);
        switch (activeTab) {
            case GENERAL -> addGeneralWidgets(layout);
            case STORAGE -> addStorageWidgets(layout);
            case APPEARANCE -> addAppearanceWidgets(layout);
        }
        addFooter(layout);
    }

    private void addTabs(Layout layout) {
        int gap = 2;
        int width = (layout.contentWidth() - gap * 2) / 3;
        int x = layout.contentLeft();
        for (Tab tab : Tab.values()) {
            Button button = addButton(x, layout.tabY(), width, 20,
                    Component.translatable(tab.translationKey),
                    Component.translatable(tab.translationKey + ".tooltip"),
                    pressed -> {
                        activeTab = tab;
                        rebuildSettingsWidgets();
                    });
            button.active = activeTab != tab;
            x += width + gap;
        }
    }

    private void addGeneralWidgets(Layout layout) {
        int x = layout.contentLeft();
        int y = layout.contentTop() + 16;
        int width = layout.contentWidth();
        boolean editable = serverEditable();

        Button mode = addButton(x, y, width, 20, modeLabel(),
                Component.translatable("quickstackcraft.config.storage_detection.tooltip"), pressed -> {
                    storageDetection = storageDetection == QuickStackSettings.StorageDetection.RECOGNIZED_STORAGE
                            ? QuickStackSettings.StorageDetection.ANY_ITEM_INVENTORY
                            : QuickStackSettings.StorageDetection.RECOGNIZED_STORAGE;
                    markServerDirty();
                });
        mode.active = editable;

        y += 24;
        addStepper(x, y, width, Component.translatable("quickstackcraft.config.radius", searchRadius),
                Component.translatable("quickstackcraft.config.radius.tooltip"),
                () -> searchRadius = Math.max(1, searchRadius - 1),
                () -> searchRadius = Math.min(64, searchRadius + 1), editable, this::markServerDirty);

        y += 24;
        Button fallback = addButton(x, y, width, 20,
                Component.translatable(capacityFallback
                        ? "quickstackcraft.config.slot_fallback_on"
                        : "quickstackcraft.config.slot_fallback_off"),
                Component.translatable("quickstackcraft.config.slot_fallback.tooltip"), pressed -> {
                    capacityFallback = !capacityFallback;
                    markServerDirty();
                });
        fallback.active = editable;

        y += 24;
        addStepper(x, y, width, Component.translatable("quickstackcraft.config.min_slots", capacityThreshold),
                Component.translatable("quickstackcraft.config.min_slots.tooltip"),
                () -> capacityThreshold = Math.max(1, capacityThreshold - 1),
                () -> capacityThreshold = Math.min(256, capacityThreshold + 1),
                editable && capacityFallback, this::markServerDirty);

        y += 28;
        Button preview = addButton(x, y, width, 20,
                Component.translatable("quickstackcraft.config.preview"),
                Component.translatable("quickstackcraft.config.preview.tooltip"),
                pressed -> previewAndClose());
        preview.active = loaded && !saving;
    }

    private void addStorageWidgets(Layout layout) {
        int y = storageControlsY(layout);
        boolean wide = layout.contentWidth() >= 326;
        if (wide) {
            int gap = 8;
            int columnWidth = (layout.contentWidth() - gap) / 2;
            addTargetColumn(layout.contentLeft(), y, columnWidth, layout.contentBottom(), TargetList.INCLUDED);
            addTargetColumn(layout.contentLeft() + columnWidth + gap, y, columnWidth,
                    layout.contentBottom(), TargetList.EXCLUDED);
            return;
        }

        int half = (layout.contentWidth() - 2) / 2;
        Button whitelist = addButton(layout.contentLeft(), y, half, 20,
                Component.translatable("quickstackcraft.config.whitelist"),
                Component.translatable("quickstackcraft.config.whitelist.tooltip"), pressed -> {
                    compactTargetList = TargetList.INCLUDED;
                    rebuildSettingsWidgets();
                });
        Button blacklist = addButton(layout.contentLeft() + half + 2, y, half, 20,
                Component.translatable("quickstackcraft.config.blacklist"),
                Component.translatable("quickstackcraft.config.blacklist.tooltip"), pressed -> {
                    compactTargetList = TargetList.EXCLUDED;
                    rebuildSettingsWidgets();
                });
        whitelist.active = compactTargetList != TargetList.INCLUDED;
        blacklist.active = compactTargetList != TargetList.EXCLUDED;
        addTargetColumn(layout.contentLeft(), y + 24, layout.contentWidth(),
                layout.contentBottom(), compactTargetList);
    }

    private void addTargetColumn(int x, int y, int width, int bottom, TargetList targetList) {
        int navSize = 18;
        int actionWidth = width - navSize * 2 - 4;
        String actionKey = targetList == TargetList.INCLUDED
                ? "quickstackcraft.config.whitelist_selected"
                : "quickstackcraft.config.blacklist_selected";
        String tooltipKey = targetList == TargetList.INCLUDED
                ? "quickstackcraft.config.whitelist_selected.tooltip"
                : "quickstackcraft.config.blacklist_selected.tooltip";
        Button selectedBlock = addButton(x, y, actionWidth, 18,
                Component.translatable(actionKey),
                Component.translatable(tooltipKey),
                pressed -> addSelectedStorage(targetList));
        selectedBlock.active = serverEditable();

        List<ResourceLocation> targets = targets(targetList);
        int listTop = y + 22;
        int availableHeight = Math.max(TARGET_ROW_HEIGHT, bottom - listTop);
        int rowsPerPage = Math.max(1, availableHeight / TARGET_ROW_HEIGHT);
        int pages = pageCount(targets, rowsPerPage);
        setPage(targetList, Math.min(page(targetList), pages - 1));

        UiIconButton previous = addIconButton(x + actionWidth + 2, y, navSize, UiIcon.PREVIOUS,
                Component.translatable("quickstackcraft.config.previous_page"),
                pressed -> changePage(targetList, -1, rowsPerPage));
        previous.active = page(targetList) > 0;
        UiIconButton next = addIconButton(x + actionWidth + navSize + 4, y, navSize, UiIcon.NEXT,
                Component.translatable("quickstackcraft.config.next_page"),
                pressed -> changePage(targetList, 1, rowsPerPage));
        next.active = page(targetList) + 1 < pages;

        DropArea area = new DropArea(x, listTop, width, rowsPerPage * TARGET_ROW_HEIGHT);
        setDropArea(targetList, area);
        int start = page(targetList) * rowsPerPage;
        int visible = Math.min(rowsPerPage, Math.max(0, targets.size() - start));
        for (int row = 0; row < visible; row++) {
            ResourceLocation id = targets.get(start + row);
            int rowY = listTop + row * TARGET_ROW_HEIGHT;
            renderedTargetRows.add(new TargetRow(id, targetList,
                    new DropArea(x, rowY, width, TARGET_ROW_HEIGHT)));
            UiIconButton remove = addIconButton(x + width - 19, rowY + 1, 18, UiIcon.REMOVE,
                    Component.translatable("quickstackcraft.config.remove_entry", id.toString()), pressed -> {
                        targets.remove(id);
                        clampPage(targetList, rowsPerPage);
                        markServerDirty();
                    });
            remove.active = serverEditable();
        }
    }

    private void addAppearanceWidgets(Layout layout) {
        int x = layout.contentLeft();
        int y = layout.contentTop() + 16;
        int width = layout.contentWidth();

        int swatchGap = 3;
        int swatchSize = Math.min(22, (width - swatchGap * (COLOR_PRESETS.length - 1)) / COLOR_PRESETS.length);
        int swatchX = x;
        for (String color : COLOR_PRESETS) {
            ColorSwatchButton swatch = new ColorSwatchButton(
                    swatchX, y, swatchSize, color, color.equalsIgnoreCase(outlineColor), pressed -> {
                selectOutlineColor(color);
            });
            addSettingsWidget(swatch);
            swatchX += swatchSize + swatchGap;
        }

        y += 26;
        colorBox = new EditBox(this.font, x, y, width, 20,
                Component.translatable("quickstackcraft.config.color"));
        colorBox.setMaxLength(7);
        colorBox.setValue(outlineColor);
        colorBox.setTooltip(Tooltip.create(Component.translatable("quickstackcraft.config.color.tooltip")));
        colorBox.setResponder(value -> {
            outlineColor = value;
            dirtyClient = true;
        });
        addSettingsWidget(colorBox);

        y += 24;
        addStepper(x, y, width,
                Component.translatable("quickstackcraft.config.alpha", (int) Math.round(outlineOpacity * 100.0D)),
                Component.translatable("quickstackcraft.config.alpha.tooltip"),
                () -> outlineOpacity = Math.max(0.1D, outlineOpacity - 0.1D),
                () -> outlineOpacity = Math.min(1.0D, outlineOpacity + 0.1D),
                !saving, this::markClientDirty);

        y += 24;
        addStepper(x, y, width,
                Component.translatable("quickstackcraft.config.duration", outlineLifetimeMs / 1000.0D),
                Component.translatable("quickstackcraft.config.duration.tooltip"),
                () -> outlineLifetimeMs = Math.max(500, outlineLifetimeMs - 500),
                () -> outlineLifetimeMs = Math.min(10000, outlineLifetimeMs + 500),
                !saving, this::markClientDirty);

    }

    private void addFooter(Layout layout) {
        int y = layout.footerY();
        UiIconButton tutorial = addIconButton(layout.contentLeft(), y, 20, UiIcon.TUTORIAL,
                Component.translatable("quickstackcraft.config.tutorial"),
                pressed -> {
                    persistClientPreferences();
                    TutorialScreen.open(this);
                });
        tutorial.active = !saving;
        UiIconButton about = addIconButton(layout.contentLeft() + 24, y, 20, UiIcon.ABOUT,
                Component.translatable("quickstackcraft.config.about"),
                pressed -> Minecraft.getInstance().setScreen(new AboutScreen(this)));
        about.active = !saving;

        Button done = addButton(layout.panelRight() - 84, y, 72, 20,
                Component.translatable(saving ? "quickstackcraft.config.saving" : "gui.done"),
                Component.translatable("quickstackcraft.config.done.tooltip"), pressed -> saveAndClose());
        done.active = !saving && (loaded || !dirtyServer);
    }

    private Button addButton(
            int x, int y, int width, int height,
            Component label, Component tooltip, Button.OnPress onPress) {
        Button button = Button.builder(label, onPress).bounds(x, y, width, height).build();
        button.setTooltip(Tooltip.create(tooltip));
        button.setTooltipDelay(Duration.ofMillis(300));
        addSettingsWidget(button);
        return button;
    }

    private UiIconButton addIconButton(
            int x, int y, int size, UiIcon icon, Component tooltip, Button.OnPress onPress) {
        UiIconButton button = new UiIconButton(x, y, size, icon, tooltip, onPress);
        addSettingsWidget(button);
        return button;
    }

    private <T extends AbstractWidget> T addSettingsWidget(T widget) {
        settingsWidgets.add(widget);
        return addRenderableWidget(widget);
    }

    private void addStepper(
            int x, int y, int width,
            Component label, Component tooltip,
            Runnable decrement, Runnable increment,
            boolean editable, Runnable afterChange) {
        int step = 22;
        Button minus = addButton(x, y, step, 20, Component.literal("-"), tooltip, pressed -> {
            decrement.run();
            afterChange.run();
        });
        minus.active = editable;
        Button value = addButton(x + step + 2, y, width - step * 2 - 4, 20,
                label, tooltip, pressed -> {});
        value.active = false;
        Button plus = addButton(x + width - step, y, step, 20, Component.literal("+"), tooltip, pressed -> {
            increment.run();
            afterChange.run();
        });
        plus.active = editable;
    }

    private void markServerDirty() {
        dirtyServer = true;
        statusMessage = Component.empty();
        rebuildSettingsWidgets();
    }

    private void markClientDirty() {
        dirtyClient = true;
        rebuildSettingsWidgets();
    }

    private void selectOutlineColor(String color) {
        outlineColor = color;
        if (colorBox != null) {
            colorBox.setValue(color);
        }
        markClientDirty();
    }

    private void addSelectedStorage(TargetList targetList) {
        BlockSelection selected = selectedBlock();
        if (selected == null) {
            statusMessage = Component.translatable("quickstackcraft.message.no_selected_block");
            return;
        }
        if (!ContainerScanner.hasItemStorage(Minecraft.getInstance().level, selected.position())) {
            statusMessage = Component.translatable("quickstackcraft.message.not_item_storage");
            return;
        }
        addTarget(selected.blockId(), targetList);
    }

    private void addTarget(ResourceLocation id, TargetList targetList) {
        List<ResourceLocation> target = targets(targetList);
        List<ResourceLocation> other = targets(opposite(targetList));
        other.remove(id);
        if (!target.contains(id)) {
            target.add(id);
            target.sort(Comparator.comparing(ResourceLocation::toString));
        }
        dirtyServer = true;
        statusMessage = Component.empty();
        rebuildSettingsWidgets();
    }

    public boolean canAcceptDrop(ItemStack stack) {
        return serverEditable() && StorageTargetResolver.fromItemStack(stack).isPresent();
    }

    public boolean acceptDrop(ItemStack stack, TargetList targetList) {
        if (!serverEditable() || !isTargetListVisible(targetList)) {
            return false;
        }
        return StorageTargetResolver.fromItemStack(stack)
                .map(id -> {
                    addTarget(id, targetList);
                    return true;
                })
                .orElseGet(() -> {
                    statusMessage = Component.translatable("quickstackcraft.config.block_items_only");
                    return false;
                });
    }

    public boolean acceptDrop(ItemStack stack, int mouseX, int mouseY) {
        if (includedDropArea.contains(mouseX, mouseY)) {
            return acceptDrop(stack, TargetList.INCLUDED);
        }
        if (excludedDropArea.contains(mouseX, mouseY)) {
            return acceptDrop(stack, TargetList.EXCLUDED);
        }
        return false;
    }

    public DropArea dropArea(TargetList targetList) {
        return targetList == TargetList.INCLUDED ? includedDropArea : excludedDropArea;
    }

    public boolean isTargetListVisible(TargetList targetList) {
        return dropArea(targetList).width() > 0;
    }

    public DropArea panelArea() {
        Layout layout = layout();
        return new DropArea(layout.panelLeft(), layout.panelTop(), layout.panelWidth(), layout.panelHeight());
    }

    private void changePage(TargetList targetList, int delta, int rowsPerPage) {
        int max = pageCount(targets(targetList), rowsPerPage) - 1;
        setPage(targetList, Math.max(0, Math.min(max, page(targetList) + delta)));
        rebuildSettingsWidgets();
    }

    private void clampPage(TargetList targetList, int rowsPerPage) {
        int max = pageCount(targets(targetList), rowsPerPage) - 1;
        setPage(targetList, Math.max(0, Math.min(max, page(targetList))));
    }

    private void setDropArea(TargetList targetList, DropArea area) {
        if (targetList == TargetList.INCLUDED) {
            includedDropArea = area;
        } else {
            excludedDropArea = area;
        }
    }

    private List<ResourceLocation> targets(TargetList targetList) {
        return targetList == TargetList.INCLUDED ? includedTargets : excludedTargets;
    }

    private static TargetList opposite(TargetList targetList) {
        return targetList == TargetList.INCLUDED ? TargetList.EXCLUDED : TargetList.INCLUDED;
    }

    private int page(TargetList targetList) {
        return targetList == TargetList.INCLUDED ? includedPage : excludedPage;
    }

    private void setPage(TargetList targetList, int page) {
        if (targetList == TargetList.INCLUDED) {
            includedPage = Math.max(0, page);
        } else {
            excludedPage = Math.max(0, page);
        }
    }

    private static int pageCount(List<ResourceLocation> ids, int rowsPerPage) {
        return Math.max(1, (ids.size() + rowsPerPage - 1) / rowsPerPage);
    }

    private BlockSelection selectedBlock() {
        Minecraft minecraft = Minecraft.getInstance();
        return BlockSelection.within(minecraft.player, 6.0D).orElse(null);
    }

    private void saveAndClose() {
        persistClientPreferences();

        if (!dirtyServer) {
            closeNow();
            return;
        }
        if (!loaded) {
            statusMessage = Component.translatable("quickstackcraft.config.loading");
            return;
        }
        if (!canEdit) {
            dirtyServer = false;
            closeNow();
            return;
        }

        saving = true;
        closeAfterSave = true;
        statusMessage = Component.translatable("quickstackcraft.config.saving");
        sendServerSettings();
        rebuildSettingsWidgets();
    }

    private void previewAndClose() {
        persistClientPreferences();
        if (dirtyServer && loaded && canEdit) {
            sendServerSettings();
        }
        NetworkManager.sendToServer(new StorageListActionC2SPacket(
                StorageListActionC2SPacket.Action.SHOW_NEARBY));

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null
                && minecraft.player.containerMenu != minecraft.player.inventoryMenu) {
            minecraft.player.closeContainer();
        }
        minecraft.setScreen(null);
    }

    private void persistClientPreferences() {
        outlineColor = colorBox == null ? outlineColor : colorBox.getValue();
        String sanitizedColor = ClientPreferences.sanitizeRgb(outlineColor, ClientPreferences.DEFAULT_RGB);
        ClientPreferences.apply(new ClientPreferences.Snapshot(
                sanitizedColor, outlineOpacity, outlineLifetimeMs));
        outlineColor = sanitizedColor;
        dirtyClient = false;
    }

    private void sendServerSettings() {
        QuickStackSettings.Snapshot current = QuickStackSettings.sanitize(new QuickStackSettings.Snapshot(
                storageDetection,
                capacityFallback,
                capacityThreshold,
                searchRadius,
                cachedSettings.outlineRgb(),
                cachedSettings.outlineOpacity(),
                cachedSettings.outlineLifetimeMs()));
        NetworkManager.sendToServer(new ConfigSaveC2SPacket(
                current,
                Set.copyOf(includedTargets),
                Set.copyOf(excludedTargets),
                requestId,
                serverRevision));
    }

    private void loadServerState(
            QuickStackSettings.Snapshot snapshot,
            Set<ResourceLocation> included,
            Set<ResourceLocation> excluded,
            boolean editable,
            long revision) {
        QuickStackSettings.Snapshot sanitized = QuickStackSettings.sanitize(snapshot);
        storageDetection = sanitized.storageDetection();
        capacityFallback = sanitized.capacityFallbackEnabled();
        capacityThreshold = sanitized.capacityFallbackThreshold();
        searchRadius = sanitized.searchRadius();
        includedTargets.clear();
        includedTargets.addAll(included.stream().sorted(Comparator.comparing(ResourceLocation::toString)).toList());
        excludedTargets.clear();
        excludedTargets.addAll(excluded.stream().sorted(Comparator.comparing(ResourceLocation::toString)).toList());
        canEdit = editable;
        serverRevision = revision;
    }

    private Component modeLabel() {
        return Component.translatable(storageDetection == QuickStackSettings.StorageDetection.RECOGNIZED_STORAGE
                ? "quickstackcraft.config.mode_storage_only"
                : "quickstackcraft.config.mode_all_containers");
    }

    private boolean serverEditable() {
        return loaded && canEdit && !saving;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        Layout layout = layout();

        graphics.fill(layout.panelLeft(), layout.panelTop(), layout.panelRight(), layout.panelBottom(), 0xEC111315);
        graphics.renderOutline(layout.panelLeft(), layout.panelTop(), layout.panelWidth(), layout.panelHeight(), 0xFF7A7A7A);
        graphics.drawCenteredString(font, title, width / 2, layout.panelTop() + 8, 0xFFFFFFFF);

        int sectionColor = activeTab == Tab.STORAGE ? 0xFF8ED9A0 : 0xFFD6D6D6;
        graphics.drawString(font, Component.translatable(switch (activeTab) {
            case GENERAL -> "quickstackcraft.config.section.general";
            case STORAGE -> "quickstackcraft.config.section.storage";
            case APPEARANCE -> "quickstackcraft.config.section.appearance";
        }), layout.contentLeft(), layout.contentTop() + 2, sectionColor, false);

        if (activeTab == Tab.STORAGE) {
            renderStorageScope(graphics, layout);
            renderTargetAreas(graphics);
        } else if (activeTab == Tab.APPEARANCE) {
            renderAppearancePreview(graphics, layout);
        }

        Component banner = statusMessage;
        if (banner.getString().isEmpty() && !loaded && activeTab != Tab.APPEARANCE) {
            banner = Component.translatable("quickstackcraft.config.loading");
        } else if (banner.getString().isEmpty() && loaded && !canEdit && activeTab != Tab.APPEARANCE) {
            banner = Component.translatable("quickstackcraft.config.view_only");
        }
        if (!banner.getString().isEmpty()) {
            graphics.drawCenteredString(font, banner, width / 2, layout.contentBottom() + 4, 0xFFFFD27A);
        }

        renderWidgets(graphics, mouseX, mouseY, delta);
        renderTargetTooltip(graphics, mouseX, mouseY);
    }

    private void renderAppearancePreview(GuiGraphics graphics, Layout layout) {
        int previewY = layout.contentTop() + 116;
        int previewHeight = Math.min(34, layout.contentBottom() - previewY);
        if (previewHeight < 20) {
            return;
        }

        int previewArgb = StorageHighlightPalette.destinationArgb(
                outlineColor, outlineOpacity);
        int left = layout.contentLeft();
        int right = left + layout.contentWidth();

        graphics.fill(left, previewY, right, previewY + previewHeight, 0xB0181A1C);
        graphics.renderOutline(left, previewY, layout.contentWidth(), previewHeight, 0xFF55595D);
        graphics.drawString(font, Component.translatable("quickstackcraft.config.appearance_preview"),
                left + 8, previewY + (previewHeight - font.lineHeight) / 2, 0xFFE4E4E4, false);

        int iconX = right - 27;
        int iconY = previewY + (previewHeight - 16) / 2;
        graphics.renderItem(new ItemStack(Blocks.CHEST), iconX, iconY);
        graphics.renderOutline(iconX - 2, iconY - 2, 20, 20, previewArgb);
    }

    private void renderTargetAreas(GuiGraphics graphics) {
        for (TargetList targetList : TargetList.values()) {
            DropArea area = dropArea(targetList);
            if (area.width() <= 0) {
                continue;
            }
            int color = targetList == TargetList.INCLUDED ? 0xFF4F9A62 : 0xFFB85462;
            graphics.fill(area.x(), area.y(), area.x() + area.width(), area.y() + area.height(), 0xA0141618);
            graphics.renderOutline(area.x(), area.y(), area.width(), area.height(), color);
            if (targets(targetList).isEmpty()) {
                Component emptyText = Component.translatable(targetList == TargetList.INCLUDED
                        ? "quickstackcraft.config.whitelist_empty"
                        : "quickstackcraft.config.blacklist_empty");
                List<FormattedCharSequence> lines = font.split(emptyText, Math.max(20, area.width() - 12));
                int textY = area.y() + Math.max(3, (area.height() - lines.size() * font.lineHeight) / 2);
                for (FormattedCharSequence line : lines) {
                    graphics.drawCenteredString(font, line, area.x() + area.width() / 2, textY, 0xFF9A9A9A);
                    textY += font.lineHeight;
                }
            }
        }

        for (TargetRow row : renderedTargetRows) {
            DropArea area = row.area();
            graphics.fill(area.x() + 1, area.y() + 1,
                    area.x() + area.width() - 1, area.y() + area.height() - 1, 0xB024272A);
            Block block = BuiltInRegistries.BLOCK.getOptional(row.id()).orElse(Blocks.AIR);
            ItemStack icon = new ItemStack(block);
            if (!icon.isEmpty()) {
                graphics.renderItem(icon, area.x() + 2, area.y() + 2);
            }
            Component name = block == Blocks.AIR ? Component.literal(row.id().toString()) : block.getName();
            String trimmed = trimToWidth(name.getString(), area.width() - 43);
            graphics.drawString(font, trimmed, area.x() + 21, area.y() + 6, 0xFFE7E7E7, false);
        }
    }

    private void renderTargetTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (TargetRow row : renderedTargetRows) {
            if (!row.area().contains(mouseX, mouseY)) {
                continue;
            }
            Block block = BuiltInRegistries.BLOCK.getOptional(row.id()).orElse(Blocks.AIR);
            Component name = block == Blocks.AIR ? Component.literal(row.id().toString()) : block.getName();
            graphics.renderComponentTooltip(font, List.of(
                    name,
                    Component.literal(row.id().toString()).withStyle(ChatFormatting.DARK_GRAY),
                    Component.translatable("quickstackcraft.config.storage_scope.tooltip")
                            .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
    }

    private void renderStorageScope(GuiGraphics graphics, Layout layout) {
        Component scope = Component.translatable("quickstackcraft.config.storage_scope");
        int y = layout.contentTop() + 13;
        for (FormattedCharSequence line : font.split(scope, layout.contentWidth())) {
            graphics.drawCenteredString(font, line, width / 2, y, 0xFFB8B8B8);
            y += font.lineHeight;
        }
    }

    private int storageControlsY(Layout layout) {
        int lines = Math.max(1, font.split(
                Component.translatable("quickstackcraft.config.storage_scope"),
                layout.contentWidth()).size());
        return layout.contentTop() + 13 + lines * font.lineHeight + 3;
    }

    private static Inventory playerInventory() {
        return Objects.requireNonNull(Minecraft.getInstance().player,
                "QuickStack settings can only open while a player is active").getInventory();
    }

    private void renderWidgets(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderingWidgets = true;
        try {
            super.render(graphics, mouseX, mouseY, delta);
        } finally {
            renderingWidgets = false;
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!renderingWidgets) {
            super.renderBackground(graphics, mouseX, mouseY, delta);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected boolean hasClickedOutside(
            double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return false;
    }

    @Override
    public void onClose() {
        if (saving) {
            return;
        }
        if (dirtyServer || dirtyClient) {
            saveAndClose();
        } else {
            closeNow();
        }
    }

    private void closeNow() {
        Minecraft.getInstance().setScreen(parent);
    }

    private String trimToWidth(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return value;
        }
        return font.plainSubstrByWidth(value, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    private Layout layout() {
        int panelWidth = Math.max(250, Math.min(PANEL_MAX_WIDTH, width - 16));
        int panelHeight = Math.max(214, Math.min(PANEL_MAX_HEIGHT, height - 8));
        int panelLeft = (width - panelWidth) / 2;
        int panelTop = Math.max(4, (height - panelHeight) / 2);
        int contentLeft = panelLeft + 12;
        int contentWidth = panelWidth - 24;
        int tabY = panelTop + 24;
        int contentTop = tabY + 24;
        int footerY = panelTop + panelHeight - 26;
        return new Layout(panelLeft, panelTop, panelWidth, panelHeight,
                contentLeft, contentWidth, tabY, contentTop, footerY - 18, footerY);
    }

    private record Layout(
            int panelLeft,
            int panelTop,
            int panelWidth,
            int panelHeight,
            int contentLeft,
            int contentWidth,
            int tabY,
            int contentTop,
            int contentBottom,
            int footerY) {
        int panelRight() {
            return panelLeft + panelWidth;
        }

        int panelBottom() {
            return panelTop + panelHeight;
        }
    }

    private record TargetRow(ResourceLocation id, TargetList targetList, DropArea area) {}
    static final class ConfigMenu extends AbstractContainerMenu {

        private ConfigMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static final class ColorSwatchButton extends Button {
        private final int rgb;

        private final boolean selected;

        private ColorSwatchButton(
                int x, int y, int size, String color, boolean selected, OnPress onPress) {
            super(x, y, size, 20, Component.literal("#" + color), onPress, DEFAULT_NARRATION);
            rgb = Integer.parseInt(color, 16);
            this.selected = selected;
            setTooltip(Tooltip.create(Component.translatable("quickstackcraft.config.color_preset", color)));
            setTooltipDelay(Duration.ofMillis(250));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int border = selected ? 0xFFFFD34E : (isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF777777);
            graphics.fill(getX(), getY(), getRight(), getBottom(), border);
            graphics.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2, 0xFF000000 | rgb);
        }

        @Override
        public void renderString(GuiGraphics graphics, Font font, int color) {
        }
    }
}

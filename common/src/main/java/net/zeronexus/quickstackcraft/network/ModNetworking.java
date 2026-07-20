package net.zeronexus.quickstackcraft.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.client.ContainerHighlightRenderer;
import net.zeronexus.quickstackcraft.client.NearbyItemsCache;
import net.zeronexus.quickstackcraft.config.QuickStackSettings;
import net.zeronexus.quickstackcraft.logic.ContainerScanner;
import net.zeronexus.quickstackcraft.logic.CraftFromNearbyLogic;
import net.zeronexus.quickstackcraft.logic.InventoryTransferService;
import net.zeronexus.quickstackcraft.logic.FavoritesManager;
import net.zeronexus.quickstackcraft.logic.OpenContainerSource;
import net.zeronexus.quickstackcraft.logic.StorageListState;
import net.zeronexus.quickstackcraft.logic.StorageBlockLists;
import net.zeronexus.quickstackcraft.logic.TransferResult;
import net.zeronexus.quickstackcraft.logic.TutorialProgressManager;
import net.zeronexus.quickstackcraft.util.ContainerAccess;
import net.zeronexus.quickstackcraft.util.BlockSelection;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModNetworking {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickStackCraft.MOD_ID);
    private static final int CLIENTBOUND_PACKET_COUNT = 6;
    private static final boolean DEFAULT_SKIP_HOTBAR = true;
    private static final boolean DEFAULT_INCLUDE_ENTITIES = true;
    private static long configRevision;

    private ModNetworking() {}

    public static void register() {
        // C2S: player inventory transfer actions
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                InventoryActionC2SPacket.TYPE,
                InventoryActionC2SPacket.CODEC,
                ModNetworking::handleInventoryAction
        );

        // C2S/S2C: in-game configuration screen
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ConfigRequestC2SPacket.TYPE,
                ConfigRequestC2SPacket.CODEC,
                ModNetworking::handleConfigRequest
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ConfigSaveC2SPacket.TYPE,
                ConfigSaveC2SPacket.CODEC,
                ModNetworking::handleConfigSave
        );
        // C2S: transfer from an open supported storage screen
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                OpenContainerTransferC2SPacket.TYPE,
                OpenContainerTransferC2SPacket.CODEC,
                ModNetworking::handleOpenContainerTransfer
        );

        // C2S: preview nearby storage or edit the selected block type
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                StorageListActionC2SPacket.TYPE,
                StorageListActionC2SPacket.CODEC,
                ModNetworking::handleStorageListAction
        );
        // C2S: server-validated recipe transfer from recipe viewers and the vanilla book
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                RecipeTransferC2SPacket.TYPE,
                RecipeTransferC2SPacket.CODEC,
                ModNetworking::handleRecipeTransfer
        );

        // C2S: Toggle Favorite
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                FavoriteToggleC2SPacket.TYPE,
                FavoriteToggleC2SPacket.CODEC,
                ModNetworking::handleFavoriteToggle
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                TutorialSeenC2SPacket.TYPE,
                TutorialSeenC2SPacket.CODEC,
                ModNetworking::handleTutorialSeen
        );

        // C2S: Nearby Items Scan (for JEI availability)
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                NearbyItemsScanC2SPacket.TYPE,
                NearbyItemsScanC2SPacket.CODEC,
                ModNetworking::handleNearbyItemsScan
        );

        registerClientboundPackets();
    }

    private static void registerClientboundPackets() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            registerClientboundReceivers();
            LOGGER.info("Registered {} clientbound packet receivers", CLIENTBOUND_PACKET_COUNT);
            return;
        }

        NetworkManager.registerS2CPayloadType(ConfigSyncS2CPacket.TYPE, ConfigSyncS2CPacket.CODEC);
        NetworkManager.registerS2CPayloadType(
                StorageListFeedbackS2CPacket.TYPE, StorageListFeedbackS2CPacket.CODEC);
        NetworkManager.registerS2CPayloadType(FavoriteSyncS2CPacket.TYPE, FavoriteSyncS2CPacket.CODEC);
        NetworkManager.registerS2CPayloadType(TutorialStatusS2CPacket.TYPE, TutorialStatusS2CPacket.CODEC);
        NetworkManager.registerS2CPayloadType(NearbyItemsSyncS2CPacket.TYPE, NearbyItemsSyncS2CPacket.CODEC);
        NetworkManager.registerS2CPayloadType(
                ContainerHighlightS2CPacket.TYPE, ContainerHighlightS2CPacket.CODEC);
        LOGGER.info("Registered {} clientbound payload types for the dedicated server", CLIENTBOUND_PACKET_COUNT);
    }

    private static void registerClientboundReceivers() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                ConfigSyncS2CPacket.TYPE,
                ConfigSyncS2CPacket.CODEC,
                ModNetworking::handleConfigSync
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                StorageListFeedbackS2CPacket.TYPE,
                StorageListFeedbackS2CPacket.CODEC,
                ModNetworking::handleStorageListFeedback
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                FavoriteSyncS2CPacket.TYPE,
                FavoriteSyncS2CPacket.CODEC,
                ModNetworking::handleFavoriteSync
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                TutorialStatusS2CPacket.TYPE,
                TutorialStatusS2CPacket.CODEC,
                ModNetworking::handleTutorialStatus
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                NearbyItemsSyncS2CPacket.TYPE,
                NearbyItemsSyncS2CPacket.CODEC,
                ModNetworking::handleNearbyItemsSync
        );
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                ContainerHighlightS2CPacket.TYPE,
                ContainerHighlightS2CPacket.CODEC,
                ModNetworking::handleContainerHighlight
        );
    }

    private static void handleConfigRequest(ConfigRequestC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            syncConfigToPlayer(player, packet.requestId(), ConfigSyncS2CPacket.SyncReason.RESPONSE);
        });
    }

    private static void handleConfigSave(ConfigSaveC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (rejectUnauthorizedStorageEdit(player)) {
                syncConfigToPlayer(player, packet.requestId(), ConfigSyncS2CPacket.SyncReason.REJECTED);
                return;
            }
            if (packet.baseRevision() != configRevision) {
                player.displayClientMessage(Component.translatable("quickstackcraft.message.config_changed"), true);
                syncConfigToPlayer(player, packet.requestId(), ConfigSyncS2CPacket.SyncReason.REJECTED);
                return;
            }

            QuickStackSettings.apply(packet.settings());
            StorageBlockLists.replace(packet.includedTargets(), packet.excludedTargets());
            configRevision++;
            player.displayClientMessage(Component.translatable("quickstackcraft.message.config_saved"), true);
            syncConfigToPlayer(player, packet.requestId(), ConfigSyncS2CPacket.SyncReason.SAVED);
        });
    }

    private static void handleConfigSync(ConfigSyncS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            net.zeronexus.quickstackcraft.client.QuickStackConfigScreen.receive(packet);
        });
    }

    private static void handleInventoryAction(
            InventoryActionC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            List<ContainerAccess> containers = nearbyStorage(player);

            boolean dumping = packet.action() == InventoryActionC2SPacket.Action.DUMP;
            InventoryTransferService.DepositMode mode = dumping
                    ? InventoryTransferService.DepositMode.ANY_STORAGE
                    : InventoryTransferService.DepositMode.MATCHING_STORAGE;
            TransferResult result = InventoryTransferService.movePlayerInventory(
                    player, containers, DEFAULT_SKIP_HOTBAR,
                    slot -> FavoritesManager.isFavorited(player, slot) || packet.protects(slot),
                    mode);

            player.containerMenu.broadcastChanges();

            if (result.didSomething()) {
                String messageKey = dumping
                        ? "quickstackcraft.message.dump"
                        : "quickstackcraft.message.quick_stack";
                player.displayClientMessage(Component.translatable(
                        messageKey, result.itemsMoved(), result.containersUsed()), true);
                sendHighlights(player, result);
            } else {
                String messageKey = dumping
                        ? "quickstackcraft.message.nothing_to_dump"
                        : "quickstackcraft.message.nothing_to_stack";
                player.displayClientMessage(Component.translatable(messageKey), true);
            }
        });
    }

    private static void handleFavoriteToggle(FavoriteToggleC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            int slot = packet.slot();

            // Validate slot range (main inventory: 0-35)
            if (slot < 0 || slot >= 36) return;

            FavoritesManager.toggle(player, slot);

            // Sync updated favorites back to client
            Set<Integer> favorites = FavoritesManager.getAll(player);
            NetworkManager.sendToPlayer(player, new FavoriteSyncS2CPacket(favorites));
        });
    }

    private static void handleFavoriteSync(FavoriteSyncS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ClientFavoritesCache.update(packet.slots());
        });
    }

    private static void handleTutorialStatus(
            TutorialStatusS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> net.zeronexus.quickstackcraft.client.tutorial.TutorialClientState
                .receiveStatus(packet.seen()));
    }

    private static void handleTutorialSeen(
            TutorialSeenC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> TutorialProgressManager.markSeen(context.getPlayer()));
    }

    private static void handleOpenContainerTransfer(
            OpenContainerTransferC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            List<Slot> sourceSlots = OpenContainerSource.contentSlots(player.containerMenu);
            if (sourceSlots.isEmpty()) {
                player.displayClientMessage(Component.translatable("quickstackcraft.message.no_open_storage"), true);
                return;
            }

            List<ContainerAccess> containers = nearbyStorage(player);

            boolean allItems = packet.kind() == OpenContainerTransferC2SPacket.TransferKind.ALL_ITEMS;
            InventoryTransferService.DepositMode mode = allItems
                    ? InventoryTransferService.DepositMode.ANY_STORAGE
                    : InventoryTransferService.DepositMode.MATCHING_STORAGE;
            TransferResult result = InventoryTransferService.moveOpenContainer(
                    player, sourceSlots, containers, mode);

            player.containerMenu.broadcastChanges();

            if (result.didSomething()) {
                Component message = allItems
                        ? Component.translatable("quickstackcraft.message.dump", result.itemsMoved(), result.containersUsed())
                        : Component.translatable("quickstackcraft.message.quick_stack", result.itemsMoved(), result.containersUsed());
                player.displayClientMessage(message, true);
                sendHighlights(player, result);
            } else {
                player.displayClientMessage(
                        Component.translatable(allItems
                                ? "quickstackcraft.message.nothing_to_dump"
                                : "quickstackcraft.message.nothing_to_stack"),
                        true);
            }
        });
    }

    private static void handleStorageListAction(
            StorageListActionC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            switch (packet.action()) {
                case SHOW_NEARBY -> outlineNearbyStorage(player);
                case CYCLE_LOOKED_AT -> cycleLookedAtStorageList(player);
            }
        });
    }

    private static void outlineNearbyStorage(ServerPlayer player) {
        List<ContainerAccess> containers = nearbyStorage(player);

        if (containers.isEmpty()) {
            player.displayClientMessage(Component.translatable("quickstackcraft.message.no_nearby_storage"), true);
            return;
        }

        NetworkManager.sendToPlayer(player, new ContainerHighlightS2CPacket(
                blockTargets(containers), entityTargets(containers)));
        player.displayClientMessage(
                Component.translatable("quickstackcraft.message.preview_storage_result", containers.size()), true);
    }

    private static void cycleLookedAtStorageList(ServerPlayer player) {
        if (rejectUnauthorizedStorageEdit(player)) {
            return;
        }

        BlockSelection target = BlockSelection.within(player, 6.0D).orElse(null);
        if (target == null) {
            player.displayClientMessage(Component.translatable("quickstackcraft.message.no_selected_block"), true);
            return;
        }

        if (!ContainerScanner.hasItemStorage(player.level(), target.position())) {
            player.displayClientMessage(Component.translatable("quickstackcraft.message.not_item_storage"), true);
            return;
        }

        StorageListState state = StorageBlockLists.cycle(target.blockId());
        configRevision++;
        NetworkManager.sendToPlayer(player, new StorageListFeedbackS2CPacket(
                target.position(), target.blockId(), state));
        player.displayClientMessage(Component.translatable(switch (state) {
            case WHITELISTED -> "quickstackcraft.message.storage_whitelisted";
            case BLACKLISTED -> "quickstackcraft.message.storage_blacklisted";
            case DEFAULT -> "quickstackcraft.message.storage_default";
        }, target.blockId().toString()), true);
    }

    private static void handleStorageListFeedback(
            StorageListFeedbackS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> ContainerHighlightRenderer.onListStateFeedback(
                packet.blockPos(), packet.state()));
    }

    private static void handleRecipeTransfer(RecipeTransferC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            net.minecraft.world.inventory.AbstractContainerMenu activeMenu = player.containerMenu;
            if (player.isSpectator()
                    || activeMenu.containerId != packet.containerId()
                    || !activeMenu.stillValid(player)) {
                return;
            }
            RecipeHolder<?> holder = player.getServer().getRecipeManager().byKey(packet.recipeId()).orElse(null);

            if (holder == null || !(holder.value() instanceof CraftingRecipe recipe)) {
                return;
            }

            int gridWidth;
            int gridHeight;
            List<net.minecraft.world.inventory.Slot> craftSlots = new java.util.ArrayList<>();

            if (activeMenu instanceof net.minecraft.world.inventory.CraftingMenu craftingMenu) {
                gridWidth = craftingMenu.getGridWidth();
                gridHeight = craftingMenu.getGridHeight();
                for (int i = 1; i <= gridWidth * gridHeight; i++) {
                    craftSlots.add(craftingMenu.getSlot(i));
                }
            } else if (activeMenu == player.inventoryMenu) {
                gridWidth = player.inventoryMenu.getGridWidth();
                gridHeight = player.inventoryMenu.getGridHeight();
                for (int i = 1; i <= gridWidth * gridHeight; i++) {
                    craftSlots.add(player.inventoryMenu.getSlot(i));
                }
            } else {
                return;
            }

            if (!recipe.canCraftInDimensions(gridWidth, gridHeight)) {
                return;
            }

            List<ContainerAccess> containers = nearbyStorage(player);

            CraftFromNearbyLogic.CraftResult result = CraftFromNearbyLogic.execute(
                    player,
                    ingredientsForGrid(recipe, gridWidth, gridHeight),
                    craftSlots,
                    containers,
                    slot -> FavoritesManager.isFavorited(player, slot) || packet.isLocked(slot),
                    packet.maxTransfer());

            activeMenu.broadcastChanges();

            if (result.isComplete()) {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.craft_ready",
                                result.placed()),
                        true);
            } else {
                player.displayClientMessage(
                        Component.translatable("quickstackcraft.message.craft_no_ingredients"),
                        true);
            }
        });
    }

    private static List<Ingredient> ingredientsForGrid(CraftingRecipe recipe, int gridWidth, int gridHeight) {
        int gridSize = gridWidth * gridHeight;
        List<Ingredient> grid = new java.util.ArrayList<>(gridSize);
        for (int i = 0; i < gridSize; i++) {
            grid.add(Ingredient.EMPTY);
        }

        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        if (recipe instanceof ShapedRecipe shaped) {
            int recipeWidth = shaped.getWidth();
            int recipeHeight = shaped.getHeight();

            for (int row = 0; row < recipeHeight && row < gridHeight; row++) {
                for (int column = 0; column < recipeWidth && column < gridWidth; column++) {
                    int recipeIndex = row * recipeWidth + column;
                    int gridIndex = row * gridWidth + column;
                    if (recipeIndex < ingredients.size()) {
                        grid.set(gridIndex, ingredients.get(recipeIndex));
                    }
                }
            }
            return grid;
        }

        for (int i = 0; i < Math.min(ingredients.size(), gridSize); i++) {
            grid.set(i, ingredients.get(i));
        }
        return grid;
    }

    private static void handleNearbyItemsScan(NearbyItemsScanC2SPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            List<ContainerAccess> containers = nearbyStorage(player);

            Map<Item, Integer> available = new HashMap<>();
            for (ContainerAccess ca : containers) {
                Container c = ca.container();
                for (int i = 0; i < c.getContainerSize(); i++) {
                    ItemStack stack = c.getItem(i);
                    if (!stack.isEmpty()) {
                        available.merge(stack.getItem(), stack.getCount(), Integer::sum);
                    }
                }
            }

            NetworkManager.sendToPlayer(player, new NearbyItemsSyncS2CPacket(available));
        });
    }

    private static void handleNearbyItemsSync(NearbyItemsSyncS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            NearbyItemsCache.update(packet.items());
        });
    }

    private static void handleContainerHighlight(ContainerHighlightS2CPacket packet, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ContainerHighlightRenderer.onHighlightReceived(packet.blockPositions(), packet.entityIds());
        });
    }

    private static void sendHighlights(ServerPlayer player, TransferResult result) {
        if (!result.blockPositions().isEmpty() || !result.entityIds().isEmpty()) {
            NetworkManager.sendToPlayer(player, new ContainerHighlightS2CPacket(
                    result.blockPositions(), result.entityIds()));
            spawnHighlightParticles(player, result);
        }
    }

    private static List<BlockPos> blockTargets(List<ContainerAccess> containers) {
        List<BlockPos> positions = new java.util.ArrayList<>();
        for (ContainerAccess access : containers) {
            if (access.isBlockContainer()) {
                positions.add(access.blockPos());
            }
        }
        return positions;
    }

    private static List<Integer> entityTargets(List<ContainerAccess> containers) {
        List<Integer> ids = new java.util.ArrayList<>();
        for (ContainerAccess access : containers) {
            if (access.entity() != null) {
                ids.add(access.entity().getId());
            }
        }
        return ids;
    }

    private static void spawnHighlightParticles(ServerPlayer player, TransferResult result) {
        ServerLevel level = player.serverLevel();
        Vec3 playerPos = player.position().add(0, 1, 0); // Chest height

        for (BlockPos pos : result.blockPositions()) {
            Vec3 target = Vec3.atCenterOf(pos);
            // Spawn particles along the trail from player to container
            int steps = (int) Math.max(3, playerPos.distanceTo(target) * 2);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double x = playerPos.x + (target.x - playerPos.x) * t;
                double y = playerPos.y + (target.y - playerPos.y) * t;
                double z = playerPos.z + (target.z - playerPos.z) * t;
                level.sendParticles(player, ParticleTypes.HAPPY_VILLAGER,
                        true, x, y, z, 1, 0.05, 0.05, 0.05, 0);
            }
        }
    }

    /**
     * Send current favorites to a player (call on login/respawn).
     */
    public static void syncFavoritesToPlayer(ServerPlayer player) {
        Set<Integer> favorites = FavoritesManager.getAll(player);
        NetworkManager.sendToPlayer(player, new FavoriteSyncS2CPacket(favorites));
    }

    public static void syncTutorialToPlayer(ServerPlayer player) {
        NetworkManager.sendToPlayer(player,
                new TutorialStatusS2CPacket(TutorialProgressManager.hasSeen(player)));
    }

    public static void syncConfigToPlayer(ServerPlayer player) {
        syncConfigToPlayer(player, 0L, ConfigSyncS2CPacket.SyncReason.INITIAL);
    }

    public static void markConfigChanged() {
        configRevision++;
    }

    private static List<ContainerAccess> nearbyStorage(ServerPlayer player) {
        return ContainerScanner.findNearby(
                player.level(), player.position(), QuickStackSettings.searchRadius, DEFAULT_INCLUDE_ENTITIES);
    }

    private static boolean rejectUnauthorizedStorageEdit(ServerPlayer player) {
        if (player.hasPermissions(2)) {
            return false;
        }
        player.displayClientMessage(
                Component.translatable("quickstackcraft.message.no_permission"), true);
        return true;
    }

    private static void syncConfigToPlayer(
            ServerPlayer player, long requestId, ConfigSyncS2CPacket.SyncReason reason) {
        NetworkManager.sendToPlayer(player, new ConfigSyncS2CPacket(
                QuickStackSettings.snapshot(),
                StorageBlockLists.whitelist(),
                StorageBlockLists.blacklist(),
                player.hasPermissions(2),
                configRevision,
                requestId,
                reason));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(QuickStackCraft.MOD_ID, path);
    }
}

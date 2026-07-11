package net.zeronexus.quickstackcraft.compat;

import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.zeronexus.quickstackcraft.client.ClientFavoritesCache;
import net.zeronexus.quickstackcraft.client.NearbyItemsCache;
import net.zeronexus.quickstackcraft.logic.ChoiceAllocator;
import net.zeronexus.quickstackcraft.network.NearbyItemsScanC2SPacket;
import net.zeronexus.quickstackcraft.util.InventoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecipeViewerTransferHelper {

    private RecipeViewerTransferHelper() {}

    public static void requestNearbyRefreshIfNeeded(Player player) {
        long tick = player.level().getGameTime();
        if (NearbyItemsCache.needsRefresh(tick)) {
            NearbyItemsCache.markQueried(tick);
            NetworkManager.sendToServer(new NearbyItemsScanC2SPacket());
        }
    }

    public static boolean hasIngredients(List<List<ItemStack>> choices, Player player) {
        long externalLockedMask = ExternalSlotLocks.snapshot();
        List<StackGroup> groups = collectAvailableItems(player, externalLockedMask);
        List<List<Integer>> allocationOptions = new ArrayList<>();

        for (List<ItemStack> slotChoices : choices) {
            if (slotChoices.stream().noneMatch(stack -> !stack.isEmpty())) {
                continue;
            }

            List<Integer> matchingGroups = new ArrayList<>();
            for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                ItemStack available = groups.get(groupIndex).stack();
                if (slotChoices.stream().anyMatch(choice -> !choice.isEmpty()
                        && ItemStack.isSameItemSameComponents(available, choice))) {
                    matchingGroups.add(groupIndex);
                }
            }
            allocationOptions.add(matchingGroups);
        }

        Map<Integer, Integer> available = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            available.put(i, groups.get(i).count());
        }
        return ChoiceAllocator.allocate(allocationOptions, available, 1).complete();
    }

    private static List<StackGroup> collectAvailableItems(Player player, long externalLockedMask) {
        List<StackGroup> available = new ArrayList<>();

        Inventory inv = player.getInventory();
        for (int i = InventoryUtil.HOTBAR_START; i < InventoryUtil.MAIN_INV_END; i++) {
            if (isProtectedInventorySlot(i, externalLockedMask)) {
                continue;
            }
            ItemStack stack = inv.getItem(i);
            addStack(available, stack);
        }

        if (player.containerMenu instanceof CraftingMenu craftingMenu) {
            for (int i = 1; i <= 9; i++) {
                addStack(available, craftingMenu.getSlot(i).getItem());
            }
        } else {
            for (int i = 1; i <= 4; i++) {
                addStack(available, player.inventoryMenu.getSlot(i).getItem());
            }
        }

        NearbyItemsCache.forEachItem((item, count) -> addStack(available, new ItemStack(item, count)));

        return available;
    }

    public static boolean isProtectedInventorySlot(int slot, long externalLockedMask) {
        return ClientFavoritesCache.isFavorited(slot) || SlotMask.contains(externalLockedMask, slot);
    }

    private static void addStack(List<StackGroup> available, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (int i = 0; i < available.size(); i++) {
            StackGroup group = available.get(i);
            if (ItemStack.isSameItemSameComponents(group.stack(), stack)) {
                available.set(i, new StackGroup(group.stack(), saturatedAdd(group.count(), stack.getCount())));
                return;
            }
        }
        available.add(new StackGroup(stack.copyWithCount(1), stack.getCount()));
    }

    private static int saturatedAdd(int left, int right) {
        return (int) Math.min(Integer.MAX_VALUE, (long) left + right);
    }

    private record StackGroup(ItemStack stack, int count) {}
}

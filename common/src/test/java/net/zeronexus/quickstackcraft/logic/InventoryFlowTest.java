package net.zeronexus.quickstackcraft.logic;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.zeronexus.quickstackcraft.util.ContainerAccess;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.zeronexus.quickstackcraft.logic.InventoryTransferService.DepositMode.*;
import static org.junit.jupiter.api.Assertions.*;

class InventoryFlowTest {

    @BeforeAll
    static void initializeMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void hoveredStackMovesOnlyWhatFitsAndPreservesItsComponents() {
        Inventory inventory = new Inventory(null);
        ItemStack named = new ItemStack(Items.DIAMOND, 10);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Keep this name"));
        inventory.setItem(9, named);
        inventory.setItem(10, new ItemStack(Items.DIAMOND, 17));
        SimpleContainer chest = new SimpleContainer(named.copyWithCount(60));

        TransferResult result = InventoryTransferService.moveInventory(inventory, destinations(chest),
                false, slot -> slot != 9, MATCHING_STORAGE);

        assertEquals(4, result.itemsMoved());
        assertEquals(1, result.containersUsed());
        assertEquals(64, chest.getItem(0).getCount());
        assertEquals(6, inventory.getItem(9).getCount());
        assertEquals(17, inventory.getItem(10).getCount());
        assertTrue(ItemStack.isSameItemSameComponents(inventory.getItem(9), chest.getItem(0)));
        assertEquals(70, inventory.getItem(9).getCount() + chest.getItem(0).getCount());
    }

    @Test
    void quickStackProtectsHotbarLockedSlotsArmorAndOffhand() {
        Inventory inventory = new Inventory(null);
        for (int slot : new int[] {0, 9, 10, 36, 40}) {
            inventory.setItem(slot, new ItemStack(Items.IRON_INGOT, 8));
        }
        SimpleContainer chest = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 1));
        TransferResult result = InventoryTransferService.moveInventory(inventory, destinations(chest),
                true, slot -> slot == 9, MATCHING_STORAGE);
        assertEquals(8, result.itemsMoved());
        assertTrue(inventory.getItem(10).isEmpty());
        for (int slot : new int[] {0, 9, 36, 40}) {
            assertEquals(8, inventory.getItem(slot).getCount());
        }
    }

    @Test
    void selectingAHotbarStackDoesNotMoveOtherStacks() {
        Inventory inventory = new Inventory(null);
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 8));
        inventory.setItem(9, new ItemStack(Items.IRON_INGOT, 12));
        SimpleContainer chest = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 1));
        var result = InventoryTransferService.moveInventory(inventory, destinations(chest),
                false, slot -> slot != 0, MATCHING_STORAGE);
        assertEquals(8, result.itemsMoved());
        assertTrue(inventory.getItem(0).isEmpty());
        assertEquals(12, inventory.getItem(9).getCount());
    }

    @Test
    void fullOrNonmatchingStorageDoesNotConsumeOrReportItems() {
        Inventory inventory = new Inventory(null);
        inventory.setItem(9, new ItemStack(Items.DIAMOND, 7));
        SimpleContainer full = new SimpleContainer(new ItemStack(Items.DIAMOND, 64));
        SimpleContainer different = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 1));
        SimpleContainer empty = new SimpleContainer(1);
        for (SimpleContainer chest : List.of(full, different, empty)) {
            assertFalse(InventoryTransferService.moveInventory(inventory, destinations(chest),
                    false, slot -> slot != 9, MATCHING_STORAGE).didSomething());
            assertEquals(7, inventory.getItem(9).getCount());
        }
    }

    @Test
    void differentComponentsAreNotMatchingStorage() {
        Inventory inventory = new Inventory(null);
        ItemStack named = new ItemStack(Items.DIAMOND, 7);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Named"));
        inventory.setItem(9, named);
        SimpleContainer chest = new SimpleContainer(new ItemStack(Items.DIAMOND, 1));
        assertEquals(TransferResult.EMPTY, InventoryTransferService.moveInventory(inventory, destinations(chest),
                false, slot -> false, MATCHING_STORAGE));
        assertEquals(7, inventory.getItem(9).getCount());
    }

    @Test
    void dumpCanUseEmptyStorageWhileKeepingLockedStacks() {
        Inventory inventory = new Inventory(null);
        inventory.setItem(9, new ItemStack(Items.DIAMOND, 7));
        inventory.setItem(10, new ItemStack(Items.IRON_INGOT, 11));
        SimpleContainer chest = new SimpleContainer(2);
        var result = InventoryTransferService.moveInventory(inventory, destinations(chest),
                true, slot -> slot == 10, ANY_STORAGE);
        assertEquals(7, result.itemsMoved());
        assertEquals(7, chest.getItem(0).getCount());
        assertEquals(11, inventory.getItem(10).getCount());
    }

    @Test
    void restockFillsExistingStacksWithoutCreatingOrChangingProtectedStacks() {
        Inventory inventory = new Inventory(null);
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 60));
        inventory.setItem(9, new ItemStack(Items.IRON_INGOT, 8));
        SimpleContainer chest = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 20));
        var result = InventoryRestockService.restockInventory(inventory, destinations(chest), slot -> slot == 9);
        assertEquals(4, result.itemsMoved());
        assertEquals(64, inventory.getItem(0).getCount());
        assertEquals(8, inventory.getItem(9).getCount());
        assertTrue(inventory.getItem(10).isEmpty());
        assertEquals(16, chest.getItem(0).getCount());
    }

    private static List<ContainerAccess> destinations(SimpleContainer container) {
        return List.of(new ContainerAccess(container, BlockPos.ZERO, 1.0D));
    }
}

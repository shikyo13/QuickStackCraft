package net.zeronexus.quickstackcraft.logic.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zeronexus.quickstackcraft.util.DirectInsertContainer;

/**
 * Exposes Fabric Transfer API inventories through the common storage scanner contract.
 */
public final class ContainerScannerImpl {

    private ContainerScannerImpl() {}

    public static Container getContainerFromCapability(Level level, BlockPos pos) {
        var candidate = ItemStorage.SIDED.find(level, pos, null);
        if (!(candidate instanceof SlottedStorage<ItemVariant> slots) || slots.getSlotCount() == 0) {
            return null;
        }
        return new TransferApiContainer(slots);
    }

    private static final class TransferApiContainer implements Container, DirectInsertContainer {
        private final SlottedStorage<ItemVariant> slots;

        private TransferApiContainer(SlottedStorage<ItemVariant> slots) {
            this.slots = slots;
        }

        @Override
        public ItemStack insertDirect(ItemStack offered) {
            if (offered.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int accepted;
            try (Transaction transaction = Transaction.openOuter()) {
                accepted = safeCount(slots.insert(
                        ItemVariant.of(offered), offered.getCount(), transaction));
                if (accepted > 0) {
                    transaction.commit();
                }
            }

            int left = offered.getCount() - accepted;
            return left == 0 ? ItemStack.EMPTY : offered.copyWithCount(left);
        }

        @Override
        public int getContainerSize() {
            return slots.getSlotCount();
        }

        @Override
        public boolean isEmpty() {
            for (SingleSlotStorage<ItemVariant> slot : slots.getSlots()) {
                if (slot.getAmount() != 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            SingleSlotStorage<ItemVariant> slot = slot(index);
            if (slot == null || slot.getResource().isBlank() || slot.getAmount() == 0) {
                return ItemStack.EMPTY;
            }
            return slot.getResource().toStack(safeCount(slot.getAmount()));
        }

        @Override
        public ItemStack removeItem(int index, int requested) {
            SingleSlotStorage<ItemVariant> slot = slot(index);
            if (slot == null || requested <= 0 || slot.getResource().isBlank()) {
                return ItemStack.EMPTY;
            }

            ItemVariant item = slot.getResource();
            int removed;
            try (Transaction transaction = Transaction.openOuter()) {
                removed = safeCount(slot.extract(item, requested, transaction));
                if (removed > 0) {
                    transaction.commit();
                }
            }
            return removed == 0 ? ItemStack.EMPTY : item.toStack(removed);
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            SingleSlotStorage<ItemVariant> slot = slot(index);
            return slot == null ? ItemStack.EMPTY : removeItem(index, safeCount(slot.getAmount()));
        }

        @Override
        public void setItem(int index, ItemStack replacement) {
            SingleSlotStorage<ItemVariant> slot = slot(index);
            if (slot == null) {
                return;
            }

            try (Transaction transaction = Transaction.openOuter()) {
                ItemVariant existing = slot.getResource();
                if (!existing.isBlank()) {
                    slot.extract(existing, slot.getAmount(), transaction);
                }
                long inserted = replacement.isEmpty()
                        ? 0
                        : slot.insert(ItemVariant.of(replacement), replacement.getCount(), transaction);
                if (replacement.isEmpty() || inserted == replacement.getCount()) {
                    transaction.commit();
                }
            }
        }

        @Override
        public void clearContent() {
            try (Transaction transaction = Transaction.openOuter()) {
                for (SingleSlotStorage<ItemVariant> slot : slots.getSlots()) {
                    ItemVariant item = slot.getResource();
                    if (!item.isBlank()) {
                        slot.extract(item, slot.getAmount(), transaction);
                    }
                }
                transaction.commit();
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void setChanged() {
        }

        private SingleSlotStorage<ItemVariant> slot(int index) {
            return index >= 0 && index < slots.getSlotCount() ? slots.getSlot(index) : null;
        }

        private static int safeCount(long amount) {
            return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, amount));
        }
    }
}

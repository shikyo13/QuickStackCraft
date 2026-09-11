package net.zeronexus.quickstackcraft.logic.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.zeronexus.quickstackcraft.util.DirectInsertContainer;

/**
 * Bridges Forge item-handler capabilities into the common nearby-storage path.
 */
public final class ContainerScannerImpl {

    private ContainerScannerImpl() {}

    public static Container getContainerFromCapability(Level level, BlockPos pos) {
        var blockEntity = level.getBlockEntity(pos);
        IItemHandler capability = blockEntity == null ? null
                : blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        return capability == null || capability.getSlots() == 0
                ? null
                : new CapabilityContainerView(capability);
    }

    private static final class CapabilityContainerView implements Container, DirectInsertContainer {
        private final IItemHandler capability;

        private CapabilityContainerView(IItemHandler capability) {
            this.capability = capability;
        }

        @Override
        public ItemStack insertDirect(ItemStack offered) {
            ItemStack remainder = offered.copy();
            for (int index = 0; index < capability.getSlots() && !remainder.isEmpty(); index++) {
                remainder = capability.insertItem(index, remainder, false);
            }
            return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
        }

        @Override
        public int getContainerSize() {
            return capability.getSlots();
        }

        @Override
        public boolean isEmpty() {
            for (int index = 0; index < capability.getSlots(); index++) {
                if (!capability.getStackInSlot(index).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return valid(index) ? capability.getStackInSlot(index) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int index, int amount) {
            return valid(index) && amount > 0
                    ? capability.extractItem(index, amount, false)
                    : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            return valid(index)
                    ? capability.extractItem(index, capability.getStackInSlot(index).getCount(), false)
                    : ItemStack.EMPTY;
        }

        @Override
        public void setItem(int index, ItemStack replacement) {
            if (!valid(index)) {
                return;
            }
            ItemStack existing = capability.getStackInSlot(index);
            capability.extractItem(index, existing.getCount(), false);
            if (!replacement.isEmpty()) {
                capability.insertItem(index, replacement.copy(), false);
            }
        }

        @Override
        public void clearContent() {
            for (int index = 0; index < capability.getSlots(); index++) {
                removeItemNoUpdate(index);
            }
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        private boolean valid(int index) {
            return index >= 0 && index < capability.getSlots();
        }
    }
}

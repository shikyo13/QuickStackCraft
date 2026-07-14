package net.zeronexus.quickstackcraft.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.zeronexus.quickstackcraft.compat.ClassNameRules;
import net.zeronexus.quickstackcraft.mixin.CompoundContainerAccessor;
import net.zeronexus.quickstackcraft.util.ContainerAccess;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Identifies actual content slots in supported open storage menus.
 */
public final class OpenContainerSource {

    public record Selection(
            List<Slot> slots,
            List<Container> backingContainers,
            Set<BlockPos> blockPositions) {

        private static final Selection EMPTY = new Selection(List.of(), List.of(), Set.of());

        public boolean isEmpty() {
            return slots.isEmpty();
        }

        public List<ContainerAccess> excludeOpenStorage(List<ContainerAccess> nearby) {
            return nearby.stream().filter(destination -> !isSource(destination)).toList();
        }

        private boolean isSource(ContainerAccess destination) {
            if (destination.isBlockContainer() && blockPositions.contains(destination.blockPos())) {
                return true;
            }

            Container target = destination.container();
            for (Container source : backingContainers) {
                if (target == source) {
                    return true;
                }
                if (target instanceof CompoundContainer compound && compound.contains(source)) {
                    return true;
                }
                if (source instanceof CompoundContainer compound && compound.contains(target)) {
                    return true;
                }
            }
            return false;
        }
    }

    private OpenContainerSource() {}

    public static Selection select(AbstractContainerMenu menu) {
        if (menu == null) {
            return Selection.EMPTY;
        }

        List<Slot> slots = sourceSlots(menu);
        if (slots.isEmpty()) {
            return Selection.EMPTY;
        }

        List<Container> containers = new ArrayList<>();
        Set<BlockPos> positions = new LinkedHashSet<>();
        for (Slot slot : slots) {
            collectContainer(slot.container, containers, positions);
        }
        collectReflectedPositions(menu, positions);

        return new Selection(List.copyOf(slots), List.copyOf(containers), Set.copyOf(positions));
    }

    public static List<Slot> contentSlots(AbstractContainerMenu menu) {
        return select(menu).slots();
    }

    public static boolean supports(AbstractContainerMenu menu) {
        return !select(menu).isEmpty();
    }

    private static List<Slot> sourceSlots(AbstractContainerMenu menu) {
        if (menu instanceof ChestMenu || menu instanceof ShulkerBoxMenu) {
            return menu.slots.stream().filter(OpenContainerSource::isStorageSlot).toList();
        }

        List<Slot> knownSlots = menu.slots.stream()
                .filter(slot -> ClassNameRules.isSophisticatedStorageSlot(slot.getClass())
                        || ClassNameRules.isTravelersBackpackStorageSlot(slot.getClass()))
                .toList();
        if (!knownSlots.isEmpty()) {
            return knownSlots;
        }

        if (ClassNameRules.isInmisBackpackMenu(menu.getClass())) {
            return menu.slots.stream().filter(OpenContainerSource::isStorageSlot).toList();
        }
        return List.of();
    }

    private static boolean isStorageSlot(Slot slot) {
        return !(slot.container instanceof Inventory);
    }

    private static void collectContainer(
            Container container, List<Container> containers, Set<BlockPos> positions) {
        if (container == null || containsIdentity(containers, container)) {
            return;
        }
        containers.add(container);
        if (container instanceof BlockEntity blockEntity) {
            positions.add(blockEntity.getBlockPos().immutable());
        }
        if (container instanceof CompoundContainer compound) {
            CompoundContainerAccessor accessor = (CompoundContainerAccessor) compound;
            collectContainer(accessor.quickstackcraft$firstContainer(), containers, positions);
            collectContainer(accessor.quickstackcraft$secondContainer(), containers, positions);
        }
    }

    private static boolean containsIdentity(List<Container> containers, Container candidate) {
        for (Container container : containers) {
            if (container == candidate) {
                return true;
            }
        }
        return false;
    }

    private static void collectReflectedPositions(AbstractContainerMenu menu, Set<BlockPos> positions) {
        collectPositionValue(invokeNoArg(menu, "getBlockPosition"), positions);
        Object wrapper = invokeNoArg(menu, "getWrapper");
        if (wrapper != null) {
            collectPositionValue(invokeNoArg(wrapper, "getBackpackPos"), positions);
            collectPositionValue(invokeNoArg(wrapper, "getBlockPosition"), positions);
        }
    }

    private static Object invokeNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException | LinkageError | SecurityException ignored) {
            return null;
        }
    }

    private static void collectPositionValue(Object value, Set<BlockPos> positions) {
        if (value instanceof BlockPos position) {
            positions.add(position.immutable());
        } else if (value instanceof Optional<?> optional && optional.orElse(null) instanceof BlockPos position) {
            positions.add(position.immutable());
        }
    }
}

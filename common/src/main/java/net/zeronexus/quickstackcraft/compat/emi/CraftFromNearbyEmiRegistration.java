package net.zeronexus.quickstackcraft.compat.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CraftFromNearbyEmiRegistration {

    private CraftFromNearbyEmiRegistration() {}

    public static void register(EmiRegistry registry) {
        prependRecipeHandler(registry, null, new CraftFromNearbyEmiInventoryHandler());
        prependRecipeHandler(registry, MenuType.CRAFTING, new CraftFromNearbyEmiCraftingHandler());
        registry.addScreenBoundsProvider(QuickStackConfigScreen.class, screen -> {
            QuickStackConfigScreen.DropArea area = screen.panelArea();
            return new Bounds(area.x(), area.y(), area.width(), area.height());
        });
        registry.addDragDropHandler(QuickStackConfigScreen.class, new StorageTargetEmiDragDropHandler());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends AbstractContainerMenu> void prependRecipeHandler(
            EmiRegistry registry, MenuType<T> type, EmiRecipeHandler<T> handler) {
        try {
            Class<?> filler = Class.forName("dev.emi.emi.registry.EmiRecipeFiller");
            Field handlersField = filler.getField("handlers");
            Map<MenuType<?>, List<EmiRecipeHandler<?>>> handlers =
                    (Map<MenuType<?>, List<EmiRecipeHandler<?>>>) handlersField.get(null);
            List list = handlers.computeIfAbsent(type, ignored -> new ArrayList<>());
            list.add(0, handler);
        } catch (ReflectiveOperationException | LinkageError error) {
            registry.addRecipeHandler(type, handler);
        }
    }
}

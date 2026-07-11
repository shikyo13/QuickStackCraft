package net.zeronexus.quickstackcraft.compat.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.zeronexus.quickstackcraft.client.QuickStackConfigScreen;

public final class JeiPluginSetup {

    private JeiPluginSetup() {}

    public static void registerCrafting(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new CraftFromNearbyTransferHandler(), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new CraftFromNearbyCraftingTransferHandler(), RecipeTypes.CRAFTING);
    }

    public static void registerSettingsIntegration(IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(QuickStackConfigScreen.class, new QuickStackConfigJeiScreenHandler());
        registration.addGhostIngredientHandler(
                QuickStackConfigScreen.class, new StorageTargetGhostIngredientHandler());
    }
}

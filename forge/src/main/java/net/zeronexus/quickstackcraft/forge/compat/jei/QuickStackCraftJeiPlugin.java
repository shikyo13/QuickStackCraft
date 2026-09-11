package net.zeronexus.quickstackcraft.forge.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.compat.jei.JeiPluginSetup;

/**
 * Forge-side JEI plugin entry point. Must be in the platform module
 * (not common) for Forge's ModFileScanData to discover @JeiPlugin.
 */
@JeiPlugin
public class QuickStackCraftJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(QuickStackCraft.MOD_ID, "jei");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        JeiPluginSetup.registerCrafting(registration);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        JeiPluginSetup.registerSettingsIntegration(registration);
    }
}

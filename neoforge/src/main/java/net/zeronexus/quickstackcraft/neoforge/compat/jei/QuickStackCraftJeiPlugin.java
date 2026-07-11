package net.zeronexus.quickstackcraft.neoforge.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.zeronexus.quickstackcraft.QuickStackCraft;
import net.zeronexus.quickstackcraft.compat.jei.JeiPluginSetup;

/**
 * NeoForge-side JEI plugin entry point. Must be in the platform module
 * (not common) for NeoForge's ModFileScanData to discover @JeiPlugin.
 */
@JeiPlugin
public class QuickStackCraftJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(QuickStackCraft.MOD_ID, "jei");
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

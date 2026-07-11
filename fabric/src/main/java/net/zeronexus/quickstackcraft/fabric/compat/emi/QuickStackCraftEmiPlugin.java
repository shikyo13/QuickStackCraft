package net.zeronexus.quickstackcraft.fabric.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.zeronexus.quickstackcraft.compat.emi.CraftFromNearbyEmiRegistration;

public class QuickStackCraftEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        CraftFromNearbyEmiRegistration.register(registry);
    }
}

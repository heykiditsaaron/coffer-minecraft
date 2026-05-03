package dev.coffer.minecraft.platform.fabric;

import net.fabricmc.api.ModInitializer;

public final class CofferMinecraftFabricEntrypoint implements ModInitializer {
    private static CofferMinecraftFabricService service;

    @Override
    public void onInitialize() {
        service = new CofferMinecraftFabricService();
        service.initialize();
    }
}

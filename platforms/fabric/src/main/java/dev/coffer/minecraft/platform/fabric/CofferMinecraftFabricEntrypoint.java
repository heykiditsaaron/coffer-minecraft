package dev.coffer.minecraft.platform.fabric;

import net.fabricmc.api.ModInitializer;

public final class CofferMinecraftFabricEntrypoint implements ModInitializer {
    private final CofferMinecraftFabricService service = new CofferMinecraftFabricService();

    @Override
    public void onInitialize() {
        service.initialize();
    }
}

package dev.coffer.minecraft.platform.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class CofferMinecraftFabricEntrypoint implements ModInitializer {
    private static CofferMinecraftFabricService service;

    @Override
    public void onInitialize() {
        service = new CofferMinecraftFabricService();
        service.initialize();
        ServerLifecycleEvents.SERVER_STARTED.register(service::attachServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> service.detachServer());
    }
}

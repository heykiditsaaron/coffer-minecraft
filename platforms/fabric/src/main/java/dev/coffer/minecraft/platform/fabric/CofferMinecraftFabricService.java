package dev.coffer.minecraft.platform.fabric;

final class CofferMinecraftFabricService {
    private boolean initialized;

    void initialize() {
        initialized = true;
    }

    void shutdown() {
        initialized = false;
    }

    boolean initialized() {
        return initialized;
    }
}

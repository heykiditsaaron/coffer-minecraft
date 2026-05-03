package dev.coffer.minecraft.platform.fabric;

final class CofferMinecraftFabricService {
    private static final System.Logger LOGGER =
            System.getLogger(CofferMinecraftFabricService.class.getName());

    private final CofferPlaceholders cofferPlaceholders;
    private boolean initialized;

    CofferMinecraftFabricService() {
        this.cofferPlaceholders = new CofferPlaceholders();
    }

    void initialize() {
        initialized = true;
        LOGGER.log(System.Logger.Level.INFO, "Coffer Fabric platform initialized");
    }

    void shutdown() {
        initialized = false;
    }

    boolean initialized() {
        return initialized;
    }

    private static final class CofferPlaceholders {
        private Object cofferCore;
        private Object cofferRuntime;
        private Object transferableValueCoreAuthority;
        private Object transferableValueRuntimeAuthority;
    }
}

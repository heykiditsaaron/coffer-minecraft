package dev.coffer.minecraft.platform.fabric;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.coffer.core.model.request.ExchangeRequest;

public final class CofferMinecraftFabricEntrypoint implements ModInitializer {
    private static final CofferMinecraftExchangeService UNINITIALIZED_SERVICE =
            new UninitializedExchangeService();
    private static volatile CofferMinecraftFabricService service;

    public static CofferMinecraftExchangeService exchangeService() {
        CofferMinecraftFabricService currentService = service;
        if (currentService == null || !currentService.initialized()) {
            return UNINITIALIZED_SERVICE;
        }
        return currentService;
    }

    @Override
    public void onInitialize() {
        CofferMinecraftFabricService initializedService = new CofferMinecraftFabricService();
        initializedService.initialize();
        service = initializedService;
        ServerLifecycleEvents.SERVER_STARTED.register(initializedService::attachServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> initializedService.detachServer());
    }

    private static final class UninitializedExchangeService implements CofferMinecraftExchangeService {
        @Override
        public CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request) {
            Objects.requireNonNull(request, "request");
            return CompletableFuture.completedFuture(
                    new FabricCofferExecutionResult.Unavailable("SERVICE_UNINITIALIZED"));
        }
    }
}

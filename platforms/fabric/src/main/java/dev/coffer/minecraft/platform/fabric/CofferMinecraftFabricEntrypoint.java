package dev.coffer.minecraft.platform.fabric;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.coffer.core.model.request.ExchangePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CofferMinecraftFabricEntrypoint implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CofferMinecraftFabricEntrypoint.class);
    private static final CofferMinecraftExchangeService UNINITIALIZED_SERVICE =
            new UninitializedExchangeService();
    private static final CofferMinecraftLifecycleAccountability LIFECYCLE_ACCOUNTABILITY =
            CofferMinecraftLifecycleAccountability.create();
    private static final CofferMinecraftFabricConstructionContactProbe CONSTRUCTION_CONTACT_PROBE =
            CofferMinecraftFabricConstructionContactProbe.create(LIFECYCLE_ACCOUNTABILITY);
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
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            initializedService.attachServer(server);
            java.nio.file.Path runDirectory = server.getRunDirectory().toPath();
            emitLifecycleRecord(runDirectory, true);
            emitStartupConstructionProbe(runDirectory);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            emitLifecycleRecord(server.getRunDirectory().toPath(), false);
            initializedService.detachServer();
        });
        ServerTickEvents.END_SERVER_TICK.register(initializedService::drainPendingExchanges);
    }

    private static void emitLifecycleRecord(java.nio.file.Path runDirectory, boolean started) {
        try {
            if (started) {
                LIFECYCLE_ACCOUNTABILITY.recordServerStarted(runDirectory);
            } else {
                LIFECYCLE_ACCOUNTABILITY.recordServerStopped(runDirectory);
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Fabric lifecycle accountability emission failed; started={}", started, exception);
        }
    }

    private static void emitStartupConstructionProbe(java.nio.file.Path runDirectory) {
        try {
            CONSTRUCTION_CONTACT_PROBE.recordStartupProbe(runDirectory);
        } catch (RuntimeException exception) {
            LOGGER.warn("Fabric startup construction contact probe failed", exception);
        }
    }

    private static final class UninitializedExchangeService implements CofferMinecraftExchangeService {
        @Override
        public CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangePayload request) {
            Objects.requireNonNull(request, "request");
            LOGGER.warn(
                    "Fabric submitExchange called before platform service initialization; payloadId={}",
                    request.payloadId().value());
            return CompletableFuture.completedFuture(
                    new FabricCofferExecutionResult.Unavailable("SERVICE_UNINITIALIZED"));
        }
    }
}

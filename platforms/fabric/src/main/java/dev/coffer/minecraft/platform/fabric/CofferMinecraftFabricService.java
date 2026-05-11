package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftDescriptorFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadInterpreter;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimeValueSetResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.AuthorityResolver;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.mutation.MutationPlan;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeAuthority;
import org.coffer.runtime.CofferRuntime;
import org.coffer.runtime.authority.RuntimeAuthority;
import org.coffer.runtime.model.id.ExecutionPlanId;
import org.coffer.runtime.model.id.ExecutionResultId;
import org.coffer.runtime.model.id.ExecutionStepId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CofferMinecraftFabricService implements CofferMinecraftExchangeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CofferMinecraftFabricService.class);

    private final CofferRuntime cofferRuntime;
    private final TransferableValueCoreAuthority transferableValueCoreAuthority;
    private final TransferableValueRuntimeAuthority transferableValueRuntimeAuthority;
    private final AuthorityResolver coreAuthorityResolver;
    private final List<RuntimeAuthority> runtimeAuthorities;
    private final Queue<PendingExchange> pendingExchanges = new ConcurrentLinkedQueue<>();
    private final ThreadLocal<Boolean> drainingExchangeQueue =
            ThreadLocal.withInitial(() -> Boolean.FALSE);
    private MinecraftServer server;
    private boolean initialized;

    CofferMinecraftFabricService() {
        MinecraftContainerResolver containerResolver =
                new MinecraftContainerResolver(this::resolvePlayerInventorySlots);
        MinecraftDescriptorFactory descriptorFactory = new MinecraftDescriptorFactory();
        MinecraftRuntimePayloadFactory runtimePayloadFactory = new MinecraftRuntimePayloadFactory();
        MinecraftRuntimeValueSetResolver runtimeValueSetResolver = new MinecraftRuntimeValueSetResolver();
        MinecraftRuntimePayloadInterpreter runtimePayloadInterpreter = new MinecraftRuntimePayloadInterpreter();

        this.transferableValueCoreAuthority = new TransferableValueCoreAuthority(
                containerResolver,
                descriptorFactory,
                runtimePayloadFactory);
        this.transferableValueRuntimeAuthority = new TransferableValueRuntimeAuthority(
                containerResolver,
                runtimeValueSetResolver,
                runtimePayloadInterpreter,
                reasonCode -> new OpaqueObject(Map.of("reasonCode", reasonCode)));
        this.cofferRuntime = new CofferRuntime();
        this.coreAuthorityResolver = authority -> {
            if (transferableValueCoreAuthority.identifiers().contains(authority)) {
                return new ResolutionResult.Resolved(transferableValueCoreAuthority);
            }
            return new ResolutionResult.Unresolved();
        };
        this.runtimeAuthorities = List.of(transferableValueRuntimeAuthority);
    }

    void initialize() {
        initialized = true;
        LOGGER.info("Coffer Fabric platform initialized");
        LOGGER.info("Coffer authorities wired");
    }

    void attachServer(MinecraftServer server) {
        this.server = server;
        LOGGER.info("Coffer Fabric platform attached server");
    }

    void detachServer() {
        server = null;
        completePendingUnavailable("SERVER_DETACHED");
        LOGGER.info("Coffer Fabric platform detached server");
    }

    void shutdown() {
        completePendingUnavailable("SERVICE_SHUTDOWN");
        detachServer();
        initialized = false;
    }

    boolean initialized() {
        return initialized;
    }

    @Override
    public CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangePayload request) {
        Objects.requireNonNull(request, "request");
        LOGGER.info("Fabric called; payloadId={}", request.payloadId().value());
        return enqueueExchange(request);
    }

    FabricCofferExecutionResult executeExchange(ExchangePayload exchangePayload) {
        Objects.requireNonNull(exchangePayload, "exchangePayload");
        requireServerThread();

        String payloadId = exchangePayload.payloadId().value();
        LOGGER.info("Fabric exchange execution entered; payloadId={}", payloadId);
        MutationPlanId mutationPlanId = new MutationPlanId(payloadId + ":fabric-mutation-plan");
        LOGGER.info("Fabric arbitration starting; payloadId={}", payloadId);
        ArbitrationResult arbitration = CofferCore.arbitrate(
                exchangePayload,
                coreAuthorityResolver,
                new OutcomeId(payloadId + ":fabric-outcome"),
                mutationPlanId,
                denialReasonIds(payloadId));
        LOGGER.info(
                "Fabric arbitration completed; payloadId={}, decision={}, hasMutationPlan={}",
                payloadId,
                arbitration.outcome().decision(),
                arbitration.mutationPlan() != null);

        if (arbitration.outcome().decision() == Decision.DENIED) {
            LOGGER.info("Fabric arbitration denied exchange; payloadId={}", payloadId);
            return new FabricCofferExecutionResult.Denied(arbitration.outcome());
        }

        MutationPlan mutationPlan = arbitration.mutationPlan();
        if (mutationPlan == null) {
            LOGGER.error(
                    "Fabric arbitration approved exchange without mutation plan; payloadId={}, reasonCode=APPROVED_WITHOUT_MUTATION_PLAN",
                    payloadId);
            return platformUnavailable("APPROVED_WITHOUT_MUTATION_PLAN");
        }

        LOGGER.info(
                "Fabric arbitration approved exchange; payloadId={}, mutationCount={}",
                payloadId,
                mutationPlan.mutations().size());
        LOGGER.info("Fabric runtime execution starting; payloadId={}", payloadId);
        FabricCofferExecutionResult.Executed executed = new FabricCofferExecutionResult.Executed(cofferRuntime.execute(
                new ExecutionPlanId(payloadId + ":fabric-execution-plan"),
                new ExecutionResultId(payloadId + ":fabric-execution-result"),
                mutationPlan,
                executionStepIds(payloadId, mutationPlan.mutations().size()),
                runtimeAuthorities));
        LOGGER.info(
                "Fabric runtime execution completed; payloadId={}, status={}",
                payloadId,
                executed.result().status());
        return executed;
    }

    CompletableFuture<FabricCofferExecutionResult> enqueueExchange(ExchangePayload exchangePayload) {
        Objects.requireNonNull(exchangePayload, "exchangePayload");
        String payloadId = exchangePayload.payloadId().value();
        LOGGER.info("Fabric exchange enqueue entered; payloadId={}", payloadId);

        MinecraftServer currentServer = server;
        LOGGER.info(
                "Fabric exchange enqueue server snapshot resolved; payloadId={}, hasServer={}, callerThread={}",
                payloadId,
                currentServer != null,
                Thread.currentThread().getName());
        if (currentServer == null) {
            LOGGER.warn("Fabric exchange enqueue unavailable; payloadId={}, reasonCode=SERVER_UNAVAILABLE", payloadId);
            return CompletableFuture.completedFuture(platformUnavailable("SERVER_UNAVAILABLE"));
        }

        CompletableFuture<FabricCofferExecutionResult> result = new CompletableFuture<>();
        PendingExchange pendingExchange = new PendingExchange(exchangePayload, result);
        pendingExchanges.add(pendingExchange);
        if (server != currentServer && pendingExchanges.remove(pendingExchange)) {
            LOGGER.warn("Fabric exchange enqueue unavailable after server detach; payloadId={}, reasonCode=SERVER_DETACHED", payloadId);
            result.complete(platformUnavailable("SERVER_DETACHED"));
            return result;
        }
        LOGGER.info(
                "Fabric exchange enqueued for server tick; payloadId={}, serverHash={}",
                payloadId,
                System.identityHashCode(currentServer));
        return result;
    }

    void drainPendingExchanges(MinecraftServer tickServer) {
        Objects.requireNonNull(tickServer, "tickServer");

        PendingExchange pendingExchange;
        while ((pendingExchange = pendingExchanges.poll()) != null) {
            completeExchange(pendingExchange.request(), tickServer, pendingExchange.future());
        }
    }

    private void completeExchange(
            ExchangePayload exchangePayload,
            MinecraftServer tickServer,
            CompletableFuture<FabricCofferExecutionResult> result) {
        String payloadId = exchangePayload.payloadId().value();
        LOGGER.info(
                "Fabric queued exchange task entered; payloadId={}, taskThread={}, serverStillAttached={}",
                payloadId,
                Thread.currentThread().getName(),
                server == tickServer);
        if (server != tickServer) {
            LOGGER.warn("Fabric queued exchange unavailable during task; payloadId={}, reasonCode=SERVER_UNAVAILABLE", payloadId);
            result.complete(platformUnavailable("SERVER_UNAVAILABLE"));
            return;
        }

        boolean previousDraining = drainingExchangeQueue.get();
        drainingExchangeQueue.set(Boolean.TRUE);
        try {
            FabricCofferExecutionResult executionResult = executeExchange(exchangePayload);
            LOGGER.info(
                    "Fabric queued exchange completing future; payloadId={}, resultType={}",
                    payloadId,
                    executionResult.getClass().getSimpleName());
            result.complete(executionResult);
        } catch (Throwable throwable) {
            LOGGER.error("Fabric queued exchange completing future exceptionally; payloadId={}", payloadId, throwable);
            result.completeExceptionally(throwable);
        } finally {
            drainingExchangeQueue.set(previousDraining);
        }
    }

    private void completePendingUnavailable(String reasonCode) {
        PendingExchange pendingExchange;
        while ((pendingExchange = pendingExchanges.poll()) != null) {
            String payloadId = pendingExchange.request().payloadId().value();
            LOGGER.warn("Fabric queued exchange unavailable during detach; payloadId={}, reasonCode={}", payloadId, reasonCode);
            pendingExchange.future().complete(platformUnavailable(reasonCode));
        }
    }

    private static FabricCofferExecutionResult.Unavailable platformUnavailable(String reasonCode) {
        return new FabricCofferExecutionResult.Unavailable(reasonCode);
    }

    private void requireServerThread() {
        if (!drainingExchangeQueue.get()) {
            throw new IllegalStateException("exchange execution must occur on the Minecraft server thread");
        }
    }

    private Optional<List<ItemStack>> resolvePlayerInventorySlots(
            UUID playerId,
            MinecraftPlayerInventoryContainer.Region region) {
        MinecraftServer currentServer = server;
        if (currentServer == null || !drainingExchangeQueue.get()) {
            return Optional.empty();
        }

        return PlayerLookup.all(currentServer).stream()
                .filter(player -> player.getUuid().equals(playerId))
                .findFirst()
                .map(ServerPlayerEntity::getInventory)
                .map(inventory -> slotsForRegion(inventory, region));
    }

    private static List<ItemStack> slotsForRegion(
            PlayerInventory inventory,
            MinecraftPlayerInventoryContainer.Region region) {
        return switch (region) {
            case MAIN -> inventory.main.subList(PlayerInventory.getHotbarSize(), inventory.main.size());
            case HOTBAR -> inventory.main.subList(0, PlayerInventory.getHotbarSize());
            case ARMOR -> inventory.armor;
            case OFFHAND -> inventory.offHand;
        };
    }

    private static List<ReasonId> denialReasonIds(String requestId) {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 256; index++) {
            reasonIds.add(new ReasonId(requestId + ":fabric-denial-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private static List<ExecutionStepId> executionStepIds(String requestId, int count) {
        List<ExecutionStepId> stepIds = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            stepIds.add(new ExecutionStepId(requestId + ":fabric-execution-step-" + index));
        }
        return List.copyOf(stepIds);
    }

    private record PendingExchange(
            ExchangePayload request,
            CompletableFuture<FabricCofferExecutionResult> future) {
    }
}

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
import org.coffer.core.model.request.ExchangeRequest;
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
    public CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");
        LOGGER.info("Fabric called; requestId={}", request.requestId().value());
        return enqueueExchange(request);
    }

    FabricCofferExecutionResult executeExchange(ExchangeRequest exchangeRequest) {
        Objects.requireNonNull(exchangeRequest, "exchangeRequest");
        requireServerThread();

        String requestId = exchangeRequest.requestId().value();
        LOGGER.info("Fabric exchange execution entered; requestId={}", requestId);
        MutationPlanId mutationPlanId = new MutationPlanId(requestId + ":fabric-mutation-plan");
        LOGGER.info("Fabric arbitration starting; requestId={}", requestId);
        ArbitrationResult arbitration = CofferCore.arbitrate(
                exchangeRequest,
                coreAuthorityResolver,
                new OutcomeId(requestId + ":fabric-outcome"),
                mutationPlanId,
                denialReasonIds(requestId),
                exchangeRequest.metadata());
        LOGGER.info(
                "Fabric arbitration completed; requestId={}, decision={}, hasMutationPlan={}",
                requestId,
                arbitration.outcome().decision(),
                arbitration.mutationPlan() != null);

        if (arbitration.outcome().decision() == Decision.DENIED) {
            LOGGER.info("Fabric arbitration denied exchange; requestId={}", requestId);
            return new FabricCofferExecutionResult.Denied(arbitration.outcome());
        }

        MutationPlan mutationPlan = arbitration.mutationPlan();
        if (mutationPlan == null) {
            LOGGER.error(
                    "Fabric arbitration approved exchange without mutation plan; requestId={}, reasonCode=APPROVED_WITHOUT_MUTATION_PLAN",
                    requestId);
            return platformUnavailable("APPROVED_WITHOUT_MUTATION_PLAN");
        }

        LOGGER.info(
                "Fabric arbitration approved exchange; requestId={}, mutationCount={}",
                requestId,
                mutationPlan.mutations().size());
        LOGGER.info("Fabric runtime execution starting; requestId={}", requestId);
        FabricCofferExecutionResult.Executed executed = new FabricCofferExecutionResult.Executed(cofferRuntime.execute(
                new ExecutionPlanId(requestId + ":fabric-execution-plan"),
                new ExecutionResultId(requestId + ":fabric-execution-result"),
                mutationPlan,
                executionStepIds(requestId, mutationPlan.mutations().size()),
                runtimeAuthorities,
                exchangeRequest.metadata()));
        LOGGER.info(
                "Fabric runtime execution completed; requestId={}, status={}",
                requestId,
                executed.result().status());
        return executed;
    }

    CompletableFuture<FabricCofferExecutionResult> enqueueExchange(ExchangeRequest exchangeRequest) {
        Objects.requireNonNull(exchangeRequest, "exchangeRequest");
        String requestId = exchangeRequest.requestId().value();
        LOGGER.info("Fabric exchange enqueue entered; requestId={}", requestId);

        MinecraftServer currentServer = server;
        LOGGER.info(
                "Fabric exchange enqueue server snapshot resolved; requestId={}, hasServer={}, callerThread={}",
                requestId,
                currentServer != null,
                Thread.currentThread().getName());
        if (currentServer == null) {
            LOGGER.warn("Fabric exchange enqueue unavailable; requestId={}, reasonCode=SERVER_UNAVAILABLE", requestId);
            return CompletableFuture.completedFuture(platformUnavailable("SERVER_UNAVAILABLE"));
        }

        CompletableFuture<FabricCofferExecutionResult> result = new CompletableFuture<>();
        PendingExchange pendingExchange = new PendingExchange(exchangeRequest, result);
        pendingExchanges.add(pendingExchange);
        if (server != currentServer && pendingExchanges.remove(pendingExchange)) {
            LOGGER.warn("Fabric exchange enqueue unavailable after server detach; requestId={}, reasonCode=SERVER_DETACHED", requestId);
            result.complete(platformUnavailable("SERVER_DETACHED"));
            return result;
        }
        LOGGER.info(
                "Fabric exchange enqueued for server tick; requestId={}, serverHash={}",
                requestId,
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
            ExchangeRequest exchangeRequest,
            MinecraftServer tickServer,
            CompletableFuture<FabricCofferExecutionResult> result) {
        String requestId = exchangeRequest.requestId().value();
        LOGGER.info(
                "Fabric queued exchange task entered; requestId={}, taskThread={}, serverStillAttached={}",
                requestId,
                Thread.currentThread().getName(),
                server == tickServer);
        if (server != tickServer) {
            LOGGER.warn("Fabric queued exchange unavailable during task; requestId={}, reasonCode=SERVER_UNAVAILABLE", requestId);
            result.complete(platformUnavailable("SERVER_UNAVAILABLE"));
            return;
        }

        boolean previousDraining = drainingExchangeQueue.get();
        drainingExchangeQueue.set(Boolean.TRUE);
        try {
            FabricCofferExecutionResult executionResult = executeExchange(exchangeRequest);
            LOGGER.info(
                    "Fabric queued exchange completing future; requestId={}, resultType={}",
                    requestId,
                    executionResult.getClass().getSimpleName());
            result.complete(executionResult);
        } catch (Throwable throwable) {
            LOGGER.error("Fabric queued exchange completing future exceptionally; requestId={}", requestId, throwable);
            result.completeExceptionally(throwable);
        } finally {
            drainingExchangeQueue.set(previousDraining);
        }
    }

    private void completePendingUnavailable(String reasonCode) {
        PendingExchange pendingExchange;
        while ((pendingExchange = pendingExchanges.poll()) != null) {
            String requestId = pendingExchange.request().requestId().value();
            LOGGER.warn("Fabric queued exchange unavailable during detach; requestId={}, reasonCode={}", requestId, reasonCode);
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
            ExchangeRequest request,
            CompletableFuture<FabricCofferExecutionResult> future) {
    }
}

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
import java.util.UUID;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.AuthorityResolver;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.ReasonId;
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

final class CofferMinecraftFabricService {
    private static final System.Logger LOGGER =
            System.getLogger(CofferMinecraftFabricService.class.getName());

    private final CofferRuntime cofferRuntime;
    private final TransferableValueCoreAuthority transferableValueCoreAuthority;
    private final TransferableValueRuntimeAuthority transferableValueRuntimeAuthority;
    private final AuthorityResolver coreAuthorityResolver;
    private final List<RuntimeAuthority> runtimeAuthorities;
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
        LOGGER.log(System.Logger.Level.INFO, "Coffer Fabric platform initialized");
        LOGGER.log(System.Logger.Level.INFO, "Coffer authorities wired");
    }

    void attachServer(MinecraftServer server) {
        this.server = server;
    }

    void detachServer() {
        server = null;
    }

    void shutdown() {
        detachServer();
        initialized = false;
    }

    boolean initialized() {
        return initialized;
    }

    Object executeExchange(ExchangeRequest exchangeRequest) {
        Objects.requireNonNull(exchangeRequest, "exchangeRequest");
        requireServerThread();

        String requestId = exchangeRequest.requestId().value();
        MutationPlanId mutationPlanId = new MutationPlanId(requestId + ":fabric-mutation-plan");
        ArbitrationResult arbitration = CofferCore.arbitrate(
                exchangeRequest,
                coreAuthorityResolver,
                new OutcomeId(requestId + ":fabric-outcome"),
                mutationPlanId,
                denialReasonIds(requestId),
                exchangeRequest.metadata());

        if (arbitration.outcome().decision() == Decision.DENIED) {
            return arbitration.outcome();
        }

        return cofferRuntime.execute(
                new ExecutionPlanId(requestId + ":fabric-execution-plan"),
                new ExecutionResultId(requestId + ":fabric-execution-result"),
                arbitration.mutationPlan(),
                executionStepIds(requestId, arbitration.mutationPlan().mutations().size()),
                runtimeAuthorities,
                exchangeRequest.metadata());
    }

    private void requireServerThread() {
        MinecraftServer currentServer = server;
        if (currentServer != null && !currentServer.isOnThread()) {
            throw new IllegalStateException("exchange execution must occur on the Minecraft server thread");
        }
    }

    private Optional<List<ItemStack>> resolvePlayerInventorySlots(
            UUID playerId,
            MinecraftPlayerInventoryContainer.Region region) {
        MinecraftServer currentServer = server;
        if (currentServer == null || !currentServer.isOnThread()) {
            return Optional.empty();
        }

        PlayerManager playerManager = currentServer.getPlayerManager();
        if (playerManager == null) {
            return Optional.empty();
        }

        ServerPlayerEntity player = playerManager.getPlayer(playerId);
        if (player == null) {
            return Optional.empty();
        }

        PlayerInventory inventory = player.getInventory();
        return Optional.of(slotsForRegion(inventory, region));
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
}

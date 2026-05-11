package dev.coffer.minecraft.testsupport.ghostadapter;

import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftDescriptorFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadInterpreter;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimeValueSetResolver;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.mutation.MutationPlan;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapConstruction;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapRefs;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionRefusal;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult.Refused;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult.Success;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeAuthority;
import org.coffer.runtime.CofferRuntime;
import org.coffer.runtime.model.execution.ExecutionResult;
import org.coffer.runtime.model.execution.MutationExecutionResult;
import org.coffer.runtime.model.execution.MutationExecutionStatus;
import org.coffer.runtime.model.id.ExecutionPlanId;
import org.coffer.runtime.model.id.ExecutionResultId;
import org.coffer.runtime.model.id.ExecutionStepId;

public final class GhostAdapterExchangeHarness {
    private final ConstructionGateway constructionGateway;
    private final CoreGateway coreGateway;
    private final RuntimeGateway runtimeGateway;
    private final Runnable beforeRuntime;
    private final TransferableValueCoreAuthority coreAuthority;
    private final TransferableValueRuntimeAuthority runtimeAuthority;

    public GhostAdapterExchangeHarness(
            MinecraftContainerResolver.PlayerInventorySlots slots,
            ConstructionGateway constructionGateway,
            CoreGateway coreGateway,
            RuntimeGateway runtimeGateway,
            Runnable beforeRuntime) {
        Objects.requireNonNull(slots, "slots");
        this.constructionGateway = Objects.requireNonNull(constructionGateway, "constructionGateway");
        this.coreGateway = Objects.requireNonNull(coreGateway, "coreGateway");
        this.runtimeGateway = Objects.requireNonNull(runtimeGateway, "runtimeGateway");
        this.beforeRuntime = Objects.requireNonNull(beforeRuntime, "beforeRuntime");

        MinecraftContainerResolver resolver = new MinecraftContainerResolver(slots);
        this.coreAuthority = new TransferableValueCoreAuthority(
                resolver,
                new MinecraftDescriptorFactory(),
                new MinecraftRuntimePayloadFactory());
        this.runtimeAuthority = new TransferableValueRuntimeAuthority(
                resolver,
                new MinecraftRuntimeValueSetResolver(),
                new MinecraftRuntimePayloadInterpreter(),
                reasonCode -> new org.coffer.core.model.support.OpaqueObject(Map.of("reasonCode", reasonCode)));
    }

    public static GhostAdapterExchangeHarness create(MinecraftContainerResolver.PlayerInventorySlots slots) {
        return new GhostAdapterExchangeHarness(
                slots,
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                (payload, authority) -> CofferCore.arbitrate(
                        payload,
                        ignored -> new ResolutionResult.Resolved(authority),
                        new OutcomeId("ghost-outcome-1"),
                        new MutationPlanId("ghost-mutation-plan-1"),
                        denialReasonIds()),
                (mutationPlan, authority) -> new CofferRuntime().execute(
                        new ExecutionPlanId("ghost-execution-plan-1"),
                        new ExecutionResultId("ghost-execution-result-1"),
                        mutationPlan,
                        executionStepIds(mutationPlan.mutations().size()),
                        List.of(authority)),
                () -> {
                });
    }

    public GhostAdapterProjection submitAtomicSwap(GhostAdapterAtomicSwapRequest request) {
        Objects.requireNonNull(request, "request");

        TransferableValueConstructionResult construction =
                constructionGateway.construct(request.toConstruction());
        if (construction instanceof Refused refused) {
            return GhostAdapterProjection.constructionRefused(refused.refusal());
        }

        ExchangePayload payload = ((Success) construction).payload();
        ArbitrationResult arbitration = coreGateway.arbitrate(payload, coreAuthority);
        if (arbitration.outcome().decision() == Decision.DENIED) {
            return GhostAdapterProjection.coreDenied(
                    arbitration,
                    String.valueOf(arbitration.outcome().reasons().get(0).detail().values().get("reasonCode")));
        }

        MutationPlan mutationPlan = Objects.requireNonNull(arbitration.mutationPlan(), "mutationPlan");
        beforeRuntime.run();
        ExecutionResult execution = runtimeGateway.execute(mutationPlan, runtimeAuthority);
        MutationExecutionResult mutationResult = execution.mutationResults().get(0);
        String reasonCode = (String) mutationResult.detail().values().get("reasonCode");

        if (mutationResult.status() == MutationExecutionStatus.MUTATION_SUCCEEDED) {
            return GhostAdapterProjection.runtimeSuccess(arbitration, execution);
        }
        if (mutationResult.status() == MutationExecutionStatus.MUTATION_FAILED) {
            return GhostAdapterProjection.runtimeFailure(arbitration, execution, reasonCode);
        }
        return GhostAdapterProjection.runtimeUnknown(arbitration, execution, reasonCode);
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("ghost-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private static List<ExecutionStepId> executionStepIds(int count) {
        List<ExecutionStepId> stepIds = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            stepIds.add(new ExecutionStepId("ghost-execution-step-" + index));
        }
        return List.copyOf(stepIds);
    }

    @FunctionalInterface
    public interface ConstructionGateway {
        TransferableValueConstructionResult construct(TransferableValueAtomicSwapConstruction construction);
    }

    @FunctionalInterface
    public interface CoreGateway {
        ArbitrationResult arbitrate(ExchangePayload payload, TransferableValueCoreAuthority authority);
    }

    @FunctionalInterface
    public interface RuntimeGateway {
        ExecutionResult execute(MutationPlan mutationPlan, TransferableValueRuntimeAuthority authority);
    }

    public enum ProjectionKind {
        CONSTRUCTION_REFUSED,
        CORE_DENIED,
        RUNTIME_SUCCESS,
        RUNTIME_FAILURE,
        RUNTIME_UNKNOWN
    }

    public record GhostAdapterProjection(
            ProjectionKind kind,
            String reasonCode,
            TransferableValueConstructionRefusal refusal,
            ArbitrationResult arbitration,
            ExecutionResult executionResult) {
        static GhostAdapterProjection constructionRefused(TransferableValueConstructionRefusal refusal) {
            return new GhostAdapterProjection(ProjectionKind.CONSTRUCTION_REFUSED, null, refusal, null, null);
        }

        static GhostAdapterProjection coreDenied(ArbitrationResult arbitration, String reasonCode) {
            return new GhostAdapterProjection(ProjectionKind.CORE_DENIED, reasonCode, null, arbitration, null);
        }

        static GhostAdapterProjection runtimeSuccess(ArbitrationResult arbitration, ExecutionResult executionResult) {
            return new GhostAdapterProjection(ProjectionKind.RUNTIME_SUCCESS, null, null, arbitration, executionResult);
        }

        static GhostAdapterProjection runtimeFailure(
                ArbitrationResult arbitration,
                ExecutionResult executionResult,
                String reasonCode) {
            return new GhostAdapterProjection(
                    ProjectionKind.RUNTIME_FAILURE,
                    reasonCode,
                    null,
                    arbitration,
                    executionResult);
        }

        static GhostAdapterProjection runtimeUnknown(
                ArbitrationResult arbitration,
                ExecutionResult executionResult,
                String reasonCode) {
            return new GhostAdapterProjection(
                    ProjectionKind.RUNTIME_UNKNOWN,
                    reasonCode,
                    null,
                    arbitration,
                    executionResult);
        }
    }

    public record GhostAdapterAtomicSwapRequest(
            String bindingId,
            ActorDeclaration firstActor,
            ActorDeclaration secondActor,
            OfferRef firstOfferRef,
            OfferRef secondOfferRef,
            List<ValueDeclaration> firstValues,
            List<ValueDeclaration> secondValues) {
        public GhostAdapterAtomicSwapRequest {
            Objects.requireNonNull(firstActor, "firstActor");
            Objects.requireNonNull(secondActor, "secondActor");
            Objects.requireNonNull(firstOfferRef, "firstOfferRef");
            Objects.requireNonNull(secondOfferRef, "secondOfferRef");
            firstValues = List.copyOf(Objects.requireNonNull(firstValues, "firstValues"));
            secondValues = List.copyOf(Objects.requireNonNull(secondValues, "secondValues"));
        }

        TransferableValueAtomicSwapConstruction toConstruction() {
            return new TransferableValueAtomicSwapConstruction(
                    refs(),
                    firstActor,
                    secondActor,
                    firstOfferRef,
                    secondOfferRef,
                    firstValues,
                    secondValues,
                    bindingId);
        }

        private TransferableValueAtomicSwapRefs refs() {
            return new TransferableValueAtomicSwapRefs(
                    new PayloadId("ghost-payload-1"),
                    truthRefs(firstValues, valueRef -> new TruthRef("ghost-first-truth-" + valueRef.value())),
                    truthRefs(secondValues, valueRef -> new TruthRef("ghost-second-truth-" + valueRef.value())),
                    new TruthRef("ghost-first-can-receive"),
                    new TruthRef("ghost-second-can-receive"),
                    new MutationRequirementRef("ghost-mutation-requirement-1"));
        }

        private static Map<ValueRef, TruthRef> truthRefs(
                List<ValueDeclaration> values,
                Function<ValueRef, TruthRef> mapper) {
            Map<ValueRef, TruthRef> truthRefs = new LinkedHashMap<>();
            for (ValueDeclaration value : values) {
                truthRefs.put(value.valueRef(), mapper.apply(value.valueRef()));
            }
            return Map.copyOf(truthRefs);
        }
    }
}

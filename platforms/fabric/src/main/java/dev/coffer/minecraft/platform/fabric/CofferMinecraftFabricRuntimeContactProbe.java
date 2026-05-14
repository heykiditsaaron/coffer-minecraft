package dev.coffer.minecraft.platform.fabric;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimePayloadInterpreter;
import dev.coffer.minecraft.bindings.inventory.MinecraftRuntimeValueSetResolver;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.id.ActorRef;
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
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapConstruction;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapRefs;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CofferMinecraftFabricRuntimeContactProbe {
    private static final Logger LOGGER = LoggerFactory.getLogger(CofferMinecraftFabricRuntimeContactProbe.class);
    private final ConstructionGateway constructionGateway;
    private final CoreGateway coreGateway;
    private final RuntimeGateway runtimeGateway;
    private final CofferMinecraftLifecycleAccountability accountability;

    CofferMinecraftFabricRuntimeContactProbe(
            ConstructionGateway constructionGateway,
            CoreGateway coreGateway,
            RuntimeGateway runtimeGateway,
            CofferMinecraftLifecycleAccountability accountability) {
        this.constructionGateway = Objects.requireNonNull(constructionGateway, "constructionGateway");
        this.coreGateway = Objects.requireNonNull(coreGateway, "coreGateway");
        this.runtimeGateway = Objects.requireNonNull(runtimeGateway, "runtimeGateway");
        this.accountability = Objects.requireNonNull(accountability, "accountability");
    }

    static CofferMinecraftFabricRuntimeContactProbe create(CofferMinecraftLifecycleAccountability accountability) {
        TransferableValueRuntimeAuthority inertRuntimeAuthority = new TransferableValueRuntimeAuthority(
                new MinecraftContainerResolver((playerId, region) -> Optional.empty()),
                new MinecraftRuntimeValueSetResolver(),
                new MinecraftRuntimePayloadInterpreter(),
                reasonCode -> new OpaqueObject(Map.of("reasonCode", reasonCode)));

        return new CofferMinecraftFabricRuntimeContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                payload -> CofferCore.arbitrate(
                        payload,
                        ignored -> new ResolutionResult.Resolved(new InertApprovingAuthority()),
                        new OutcomeId("fabric-runtime-probe-outcome-1"),
                        new MutationPlanId("fabric-runtime-probe-mutation-plan-1"),
                        denialReasonIds()),
                mutationPlan -> new CofferRuntime().execute(
                        new ExecutionPlanId("fabric-runtime-probe-execution-plan-1"),
                        new ExecutionResultId("fabric-runtime-probe-execution-result-1"),
                        mutationPlan,
                        executionStepIds(mutationPlan.mutations().size()),
                        List.of(inertRuntimeAuthority)),
                accountability);
    }

    void recordStartupProbe(Path runDirectory) {
        TransferableValueConstructionResult construction = constructionGateway.construct(coreProbeConstruction());
        if (!(construction instanceof Success success)) {
            LOGGER.warn("Fabric startup runtime probe unexpectedly failed before Core");
            return;
        }

        ExchangePayload payload = success.payload();
        ArbitrationResult arbitration = coreGateway.arbitrate(payload);
        if (arbitration.outcome().decision() != Decision.APPROVED || arbitration.mutationPlan() == null) {
            LOGGER.warn(
                    "Fabric startup runtime probe unexpectedly failed to approve; decision={}, hasMutationPlan={}",
                    arbitration.outcome().decision(),
                    arbitration.mutationPlan() != null);
            return;
        }

        ExecutionResult execution = runtimeGateway.execute(arbitration.mutationPlan());
        if (execution.mutationResults().isEmpty()) {
            LOGGER.warn("Fabric startup runtime probe unexpectedly produced no mutation results");
            return;
        }

        MutationExecutionResult mutationResult = execution.mutationResults().get(0);
        if (mutationResult.status() == MutationExecutionStatus.MUTATION_UNKNOWN) {
            Object reasonCode = mutationResult.detail().values().get("reasonCode");
            accountability.recordRuntimeUnknown(runDirectory, reasonCode == null ? null : String.valueOf(reasonCode));
            return;
        }

        LOGGER.warn(
                "Fabric startup runtime probe unexpectedly avoided unknown runtime contact; status={}, reasonCode={}",
                mutationResult.status(),
                mutationResult.detail().values().get("reasonCode"));
    }

    private static TransferableValueAtomicSwapConstruction coreProbeConstruction() {
        List<ValueDeclaration> firstValues = List.of(value("fabric-runtime-first-value", "minecraft:stone", 1));
        List<ValueDeclaration> secondValues = List.of(value("fabric-runtime-second-value", "minecraft:dirt", 1));
        return new TransferableValueAtomicSwapConstruction(
                refs(firstValues, secondValues),
                actor("fabric-runtime:first"),
                actor("fabric-runtime:second"),
                new OfferRef("fabric-runtime-offer-1"),
                new OfferRef("fabric-runtime-offer-2"),
                firstValues,
                secondValues,
                "minecraft-inventory");
    }

    private static TransferableValueAtomicSwapRefs refs(
            List<ValueDeclaration> firstValues,
            List<ValueDeclaration> secondValues) {
        return new TransferableValueAtomicSwapRefs(
                new PayloadId("fabric-runtime-payload-1"),
                truthRefs(firstValues, valueRef -> new TruthRef("fabric-runtime-first-truth-" + valueRef.value())),
                truthRefs(secondValues, valueRef -> new TruthRef("fabric-runtime-second-truth-" + valueRef.value())),
                new TruthRef("fabric-runtime-first-can-receive"),
                new TruthRef("fabric-runtime-second-can-receive"),
                new MutationRequirementRef("fabric-runtime-mutation-requirement-1"));
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

    private static ActorDeclaration actor(String actorRef) {
        return new ActorDeclaration(
                new ActorRef(actorRef),
                "fabric-runtime-probe",
                new OpaqueObject(Map.of()));
    }

    private static ValueDeclaration value(String valueRef, String itemId, long quantity) {
        return new ValueDeclaration(
                new ValueRef(valueRef),
                TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.of(
                        "itemId", itemId,
                        "quantity", quantity)));
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("fabric-runtime-probe-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    private static List<ExecutionStepId> executionStepIds(int count) {
        List<ExecutionStepId> stepIds = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            stepIds.add(new ExecutionStepId("fabric-runtime-probe-execution-step-" + index));
        }
        return List.copyOf(stepIds);
    }

    @FunctionalInterface
    interface ConstructionGateway {
        TransferableValueConstructionResult construct(TransferableValueAtomicSwapConstruction construction);
    }

    @FunctionalInterface
    interface CoreGateway {
        ArbitrationResult arbitrate(ExchangePayload payload);
    }

    @FunctionalInterface
    interface RuntimeGateway {
        ExecutionResult execute(MutationPlan mutationPlan);
    }

    private static final class InertApprovingAuthority implements org.coffer.core.authority.Authority {
        @Override
        public java.util.Set<org.coffer.core.model.id.AuthorityIdentifier> identifiers() {
            return java.util.Set.of(TransferableValueCoreAuthority.AUTHORITY_ID);
        }

        @Override
        public org.coffer.core.model.authority.AuthorityResponse respond(org.coffer.core.model.authority.AuthorityRequest request) {
            List<org.coffer.core.model.authority.AuthorityAttestation> attestations = new ArrayList<>();
            for (org.coffer.core.model.request.RequiredTruth truth : request.requiredTruths()) {
                attestations.add(new org.coffer.core.model.authority.AuthorityAttestation(
                        truth.truthRef(),
                        true,
                        new OpaqueObject(Map.of())));
            }

            List<org.coffer.core.model.authority.CandidateMutation> candidateMutations = new ArrayList<>();
            int index = 0;
            for (org.coffer.core.model.request.MutationRequirement mutationRequirement : request.mutationRequirements()) {
                candidateMutations.add(new org.coffer.core.model.authority.CandidateMutation(
                        new org.coffer.core.model.id.MutationRef("fabric-runtime-probe-mutation-" + index),
                        mutationRequirement.mutationRequirementRef(),
                        mutationRequirement.authority(),
                        new OpaqueObject(mutationRequirement.requirement().values()),
                        mutationRequirement.appliesTo()));
                index++;
            }

            return new org.coffer.core.model.authority.AuthorityResponse(
                    TransferableValueCoreAuthority.AUTHORITY_ID,
                    List.copyOf(attestations),
                    List.of(),
                    List.copyOf(candidateMutations));
        }
    }
}

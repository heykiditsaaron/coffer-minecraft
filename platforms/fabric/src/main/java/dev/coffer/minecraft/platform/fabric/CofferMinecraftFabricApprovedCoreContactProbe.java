package dev.coffer.minecraft.platform.fabric;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.Authority;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.authority.AuthorityAttestation;
import org.coffer.core.model.authority.AuthorityRequest;
import org.coffer.core.model.authority.AuthorityResponse;
import org.coffer.core.model.authority.CandidateMutation;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.MutationRequirement;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.core.model.support.ReferenceSet;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapConstruction;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapRefs;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult.Success;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CofferMinecraftFabricApprovedCoreContactProbe {
    private static final Logger LOGGER = LoggerFactory.getLogger(CofferMinecraftFabricApprovedCoreContactProbe.class);
    private final ConstructionGateway constructionGateway;
    private final CoreGateway coreGateway;
    private final CofferMinecraftLifecycleAccountability accountability;

    CofferMinecraftFabricApprovedCoreContactProbe(
            ConstructionGateway constructionGateway,
            CoreGateway coreGateway,
            CofferMinecraftLifecycleAccountability accountability) {
        this.constructionGateway = Objects.requireNonNull(constructionGateway, "constructionGateway");
        this.coreGateway = Objects.requireNonNull(coreGateway, "coreGateway");
        this.accountability = Objects.requireNonNull(accountability, "accountability");
    }

    static CofferMinecraftFabricApprovedCoreContactProbe create(
            CofferMinecraftLifecycleAccountability accountability) {
        Authority approvingAuthority = new InertApprovingAuthority();
        return new CofferMinecraftFabricApprovedCoreContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                payload -> CofferCore.arbitrate(
                        payload,
                        ignored -> new ResolutionResult.Resolved(approvingAuthority),
                        new OutcomeId("fabric-core-approved-probe-outcome-1"),
                        new MutationPlanId("fabric-core-approved-probe-mutation-plan-1"),
                        denialReasonIds()),
                accountability);
    }

    void recordStartupProbe(Path runDirectory) {
        TransferableValueConstructionResult construction = constructionGateway.construct(coreProbeConstruction());
        if (!(construction instanceof Success success)) {
            LOGGER.warn("Fabric startup approved-core probe unexpectedly failed before Core");
            return;
        }

        ExchangePayload payload = success.payload();
        ArbitrationResult arbitration = coreGateway.arbitrate(payload);
        if (arbitration.outcome().decision() == Decision.APPROVED && arbitration.mutationPlan() != null) {
            accountability.recordCoreApproved(runDirectory);
            return;
        }

        LOGGER.warn(
                "Fabric startup approved-core probe unexpectedly failed to approve; decision={}, hasMutationPlan={}",
                arbitration.outcome().decision(),
                arbitration.mutationPlan() != null);
    }

    private static TransferableValueAtomicSwapConstruction coreProbeConstruction() {
        List<ValueDeclaration> firstValues = List.of(value("fabric-core-approved-first-value", "minecraft:stone", 1));
        List<ValueDeclaration> secondValues = List.of(value("fabric-core-approved-second-value", "minecraft:dirt", 1));
        return new TransferableValueAtomicSwapConstruction(
                refs(firstValues, secondValues),
                actor("fabric-core-approved:first"),
                actor("fabric-core-approved:second"),
                new OfferRef("fabric-core-approved-offer-1"),
                new OfferRef("fabric-core-approved-offer-2"),
                firstValues,
                secondValues,
                "minecraft-inventory");
    }

    private static TransferableValueAtomicSwapRefs refs(
            List<ValueDeclaration> firstValues,
            List<ValueDeclaration> secondValues) {
        return new TransferableValueAtomicSwapRefs(
                new PayloadId("fabric-core-approved-payload-1"),
                truthRefs(firstValues, valueRef -> new TruthRef("fabric-core-approved-first-truth-" + valueRef.value())),
                truthRefs(secondValues, valueRef -> new TruthRef("fabric-core-approved-second-truth-" + valueRef.value())),
                new TruthRef("fabric-core-approved-first-can-receive"),
                new TruthRef("fabric-core-approved-second-can-receive"),
                new MutationRequirementRef("fabric-core-approved-mutation-requirement-1"));
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
                "fabric-core-approved-probe",
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
            reasonIds.add(new ReasonId("fabric-core-approved-probe-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }

    @FunctionalInterface
    interface ConstructionGateway {
        TransferableValueConstructionResult construct(TransferableValueAtomicSwapConstruction construction);
    }

    @FunctionalInterface
    interface CoreGateway {
        ArbitrationResult arbitrate(ExchangePayload payload);
    }

    private static final class InertApprovingAuthority implements Authority {
        @Override
        public Set<org.coffer.core.model.id.AuthorityIdentifier> identifiers() {
            return Set.of(TransferableValueCoreAuthority.AUTHORITY_ID);
        }

        @Override
        public AuthorityResponse respond(AuthorityRequest request) {
            List<AuthorityAttestation> attestations = new ArrayList<>();
            for (org.coffer.core.model.request.RequiredTruth truth : request.requiredTruths()) {
                attestations.add(new AuthorityAttestation(
                        truth.truthRef(),
                        true,
                        new OpaqueObject(Map.of())));
            }

            List<CandidateMutation> candidateMutations = new ArrayList<>();
            int index = 0;
            for (MutationRequirement mutationRequirement : request.mutationRequirements()) {
                candidateMutations.add(new CandidateMutation(
                        new MutationRef("fabric-core-approved-mutation-" + index),
                        mutationRequirement.mutationRequirementRef(),
                        mutationRequirement.authority(),
                        new OpaqueObject(mutationRequirement.requirement().values()),
                        mutationRequirement.appliesTo()));
                index++;
            }

            return new AuthorityResponse(
                    TransferableValueCoreAuthority.AUTHORITY_ID,
                    List.copyOf(attestations),
                    List.of(),
                    List.copyOf(candidateMutations));
        }
    }
}

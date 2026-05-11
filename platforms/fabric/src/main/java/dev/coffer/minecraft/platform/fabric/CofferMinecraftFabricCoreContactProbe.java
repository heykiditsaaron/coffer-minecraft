package dev.coffer.minecraft.platform.fabric;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CofferMinecraftFabricCoreContactProbe {
    private static final Logger LOGGER = LoggerFactory.getLogger(CofferMinecraftFabricCoreContactProbe.class);
    private final ConstructionGateway constructionGateway;
    private final CoreGateway coreGateway;
    private final CofferMinecraftLifecycleAccountability accountability;

    CofferMinecraftFabricCoreContactProbe(
            ConstructionGateway constructionGateway,
            CoreGateway coreGateway,
            CofferMinecraftLifecycleAccountability accountability) {
        this.constructionGateway = Objects.requireNonNull(constructionGateway, "constructionGateway");
        this.coreGateway = Objects.requireNonNull(coreGateway, "coreGateway");
        this.accountability = Objects.requireNonNull(accountability, "accountability");
    }

    static CofferMinecraftFabricCoreContactProbe create(
            CofferMinecraftLifecycleAccountability accountability) {
        return new CofferMinecraftFabricCoreContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                payload -> CofferCore.arbitrate(
                        payload,
                        ignored -> new ResolutionResult.Unresolved(),
                        new OutcomeId("fabric-core-probe-outcome-1"),
                        new MutationPlanId("fabric-core-probe-mutation-plan-1"),
                        denialReasonIds()),
                accountability);
    }

    void recordStartupProbe(Path runDirectory) {
        TransferableValueConstructionResult construction = constructionGateway.construct(coreProbeConstruction());
        if (!(construction instanceof Success success)) {
            LOGGER.warn("Fabric startup core contact probe unexpectedly failed before Core");
            return;
        }

        ExchangePayload payload = success.payload();
        ArbitrationResult arbitration = coreGateway.arbitrate(payload);
        if (arbitration.outcome().decision() == Decision.DENIED) {
            accountability.recordCoreDenied(runDirectory, denialCode(arbitration));
            return;
        }

        LOGGER.warn("Fabric startup core contact probe unexpectedly avoided denial");
    }

    private static TransferableValueAtomicSwapConstruction coreProbeConstruction() {
        List<ValueDeclaration> firstValues = List.of(value("fabric-core-probe-first-value", "minecraft:stone", 1));
        List<ValueDeclaration> secondValues = List.of(value("fabric-core-probe-second-value", "minecraft:dirt", 1));
        return new TransferableValueAtomicSwapConstruction(
                refs(firstValues, secondValues),
                actor("player:00000000-0000-0000-0000-000000000401:inventory:main"),
                actor("player:00000000-0000-0000-0000-000000000402:inventory:main"),
                new OfferRef("fabric-core-probe-offer-1"),
                new OfferRef("fabric-core-probe-offer-2"),
                firstValues,
                secondValues,
                "minecraft-inventory");
    }

    private static TransferableValueAtomicSwapRefs refs(
            List<ValueDeclaration> firstValues,
            List<ValueDeclaration> secondValues) {
        return new TransferableValueAtomicSwapRefs(
                new PayloadId("fabric-core-probe-payload-1"),
                truthRefs(firstValues, valueRef -> new TruthRef("fabric-core-probe-first-truth-" + valueRef.value())),
                truthRefs(secondValues, valueRef -> new TruthRef("fabric-core-probe-second-truth-" + valueRef.value())),
                new TruthRef("fabric-core-probe-first-can-receive"),
                new TruthRef("fabric-core-probe-second-can-receive"),
                new MutationRequirementRef("fabric-core-probe-mutation-requirement-1"));
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
                "fabric-core-probe",
                new OpaqueObject(Map.of()));
    }

    private static ValueDeclaration value(String valueRef, String itemId, long quantity) {
        return new ValueDeclaration(
                new ValueRef(valueRef),
                org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.of(
                        "itemId", itemId,
                        "quantity", quantity)));
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("fabric-core-probe-reason-" + index));
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

    private static String denialCode(ArbitrationResult arbitration) {
        Object reasonCode = arbitration.outcome().reasons().get(0).detail().values().get("reasonCode");
        return reasonCode == null ? null : String.valueOf(reasonCode);
    }
}

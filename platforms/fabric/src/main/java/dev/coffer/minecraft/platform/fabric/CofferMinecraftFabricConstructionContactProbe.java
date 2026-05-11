package dev.coffer.minecraft.platform.fabric;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapConstruction;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapRefs;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult.Refused;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CofferMinecraftFabricConstructionContactProbe {
    private static final Logger LOGGER = LoggerFactory.getLogger(CofferMinecraftFabricConstructionContactProbe.class);
    private final ConstructionGateway constructionGateway;
    private final CofferMinecraftLifecycleAccountability accountability;

    CofferMinecraftFabricConstructionContactProbe(
            ConstructionGateway constructionGateway,
            CofferMinecraftLifecycleAccountability accountability) {
        this.constructionGateway = Objects.requireNonNull(constructionGateway, "constructionGateway");
        this.accountability = Objects.requireNonNull(accountability, "accountability");
    }

    static CofferMinecraftFabricConstructionContactProbe create(
            CofferMinecraftLifecycleAccountability accountability) {
        return new CofferMinecraftFabricConstructionContactProbe(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap,
                accountability);
    }

    void recordStartupProbe(Path runDirectory) {
        TransferableValueConstructionResult result = constructionGateway.construct(inertConstruction());
        if (result instanceof Refused refused) {
            accountability.recordConstructionRefused(runDirectory, refused.refusal().reason().name());
            return;
        }

        LOGGER.warn("Fabric startup construction probe unexpectedly succeeded");
    }

    private static TransferableValueAtomicSwapConstruction inertConstruction() {
        List<ValueDeclaration> firstValues = List.of(value("fabric-probe-first-value", "minecraft:stone", 1));
        List<ValueDeclaration> secondValues = List.of(value("fabric-probe-second-value", "minecraft:dirt", 1));
        return new TransferableValueAtomicSwapConstruction(
                refs(firstValues, secondValues),
                actor("fabric-probe:first"),
                actor("fabric-probe:second"),
                new OfferRef("fabric-probe-offer-1"),
                new OfferRef("fabric-probe-offer-2"),
                firstValues,
                secondValues,
                " ");
    }

    private static TransferableValueAtomicSwapRefs refs(
            List<ValueDeclaration> firstValues,
            List<ValueDeclaration> secondValues) {
        return new TransferableValueAtomicSwapRefs(
                new PayloadId("fabric-probe-payload-1"),
                truthRefs(firstValues, valueRef -> new TruthRef("fabric-probe-first-truth-" + valueRef.value())),
                truthRefs(secondValues, valueRef -> new TruthRef("fabric-probe-second-truth-" + valueRef.value())),
                new TruthRef("fabric-probe-first-can-receive"),
                new TruthRef("fabric-probe-second-can-receive"),
                new MutationRequirementRef("fabric-probe-mutation-requirement-1"));
    }

    private static Map<ValueRef, TruthRef> truthRefs(
            List<ValueDeclaration> values,
            java.util.function.Function<ValueRef, TruthRef> mapper) {
        Map<ValueRef, TruthRef> truthRefs = new LinkedHashMap<>();
        for (ValueDeclaration value : values) {
            truthRefs.put(value.valueRef(), mapper.apply(value.valueRef()));
        }
        return Map.copyOf(truthRefs);
    }

    private static ActorDeclaration actor(String actorRef) {
        return new ActorDeclaration(
                new ActorRef(actorRef),
                "fabric-probe",
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

    @FunctionalInterface
    interface ConstructionGateway {
        TransferableValueConstructionResult construct(TransferableValueAtomicSwapConstruction construction);
    }
}

package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftDescriptorFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
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
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapConstruction;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapRefs;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult.Success;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;

final class CofferMinecraftSelectedExchangeRequestAssembly {
    static final String FIRST_SELECTED_VALUE_NOT_MATERIALIZED = "FIRST_SELECTED_VALUE_NOT_MATERIALIZED";
    static final String SECOND_SELECTED_VALUE_NOT_MATERIALIZED = "SECOND_SELECTED_VALUE_NOT_MATERIALIZED";

    private final ConstructionGateway constructionGateway;

    CofferMinecraftSelectedExchangeRequestAssembly(ConstructionGateway constructionGateway) {
        this.constructionGateway = Objects.requireNonNull(constructionGateway, "constructionGateway");
    }

    static CofferMinecraftSelectedExchangeRequestAssembly create() {
        return new CofferMinecraftSelectedExchangeRequestAssembly(
                TransferableValueExchangePayloadConstruction::constructAtomicSwap);
    }

    AssemblyResult assemble(SelectedParticipant first, SelectedParticipant second, String bindingId) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        Objects.requireNonNull(bindingId, "bindingId");

        if (first.snapshot().selectedValue().isEmpty()) {
            return new AssemblyResult.Refused(FIRST_SELECTED_VALUE_NOT_MATERIALIZED);
        }
        if (second.snapshot().selectedValue().isEmpty()) {
            return new AssemblyResult.Refused(SECOND_SELECTED_VALUE_NOT_MATERIALIZED);
        }

        List<ValueDeclaration> firstValues = List.of(value("selected-first-value", first.snapshot().selectedValue().orElseThrow()));
        List<ValueDeclaration> secondValues = List.of(value("selected-second-value", second.snapshot().selectedValue().orElseThrow()));
        TransferableValueAtomicSwapConstruction construction = new TransferableValueAtomicSwapConstruction(
                refs(firstValues, secondValues),
                actor(first.actorRef()),
                actor(second.actorRef()),
                first.offerRef(),
                second.offerRef(),
                firstValues,
                secondValues,
                bindingId);

        TransferableValueConstructionResult result = constructionGateway.construct(construction);
        if (result instanceof Success success) {
            return new AssemblyResult.Prepared(construction, success.payload());
        }

        TransferableValueConstructionResult.Refused refused = (TransferableValueConstructionResult.Refused) result;
        return new AssemblyResult.Refused(refused.refusal().reason().name());
    }

    private static ActorDeclaration actor(ActorRef actorRef) {
        return new ActorDeclaration(
                actorRef,
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND,
                new OpaqueObject(Map.of()));
    }

    private static ValueDeclaration value(String valueRef, MinecraftItemDescriptor descriptor) {
        Map<String, Object> descriptorValues = new LinkedHashMap<>();
        descriptorValues.put(MinecraftDescriptorFactory.ITEM_ID, descriptor.itemId());
        descriptorValues.put(MinecraftDescriptorFactory.QUANTITY, descriptor.quantity());
        descriptor.nbtPayload().ifPresent(payload -> descriptorValues.put(MinecraftDescriptorFactory.NBT_PAYLOAD, payload));
        return new ValueDeclaration(
                new ValueRef(valueRef),
                TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.copyOf(descriptorValues)));
    }

    private static TransferableValueAtomicSwapRefs refs(
            List<ValueDeclaration> firstValues,
            List<ValueDeclaration> secondValues) {
        return new TransferableValueAtomicSwapRefs(
                new PayloadId("selected-exchange-payload-1"),
                truthRefs(firstValues, "selected-first-truth-"),
                truthRefs(secondValues, "selected-second-truth-"),
                new TruthRef("selected-first-can-receive"),
                new TruthRef("selected-second-can-receive"),
                new MutationRequirementRef("selected-exchange-mutation-requirement-1"));
    }

    private static Map<ValueRef, TruthRef> truthRefs(List<ValueDeclaration> values, String prefix) {
        Map<ValueRef, TruthRef> truthRefs = new LinkedHashMap<>();
        for (ValueDeclaration value : values) {
            truthRefs.put(value.valueRef(), new TruthRef(prefix + value.valueRef().value()));
        }
        return Map.copyOf(truthRefs);
    }

    record SelectedParticipant(
            ActorRef actorRef,
            OfferRef offerRef,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        SelectedParticipant {
            Objects.requireNonNull(actorRef, "actorRef");
            Objects.requireNonNull(offerRef, "offerRef");
            Objects.requireNonNull(snapshot, "snapshot");
        }
    }

    sealed interface AssemblyResult permits AssemblyResult.Prepared, AssemblyResult.Refused {
        record Prepared(TransferableValueAtomicSwapConstruction construction, ExchangePayload payload)
                implements AssemblyResult {
            public Prepared {
                Objects.requireNonNull(construction, "construction");
                Objects.requireNonNull(payload, "payload");
            }
        }

        record Refused(String reasonCode) implements AssemblyResult {
            public Refused {
                if (reasonCode == null || reasonCode.isBlank()) {
                    throw new IllegalArgumentException("reasonCode must not be null or blank");
                }
            }
        }
    }

    @FunctionalInterface
    interface ConstructionGateway {
        TransferableValueConstructionResult construct(TransferableValueAtomicSwapConstruction construction);
    }
}

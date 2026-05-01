package dev.coffer.minecraft.bindings.inventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueRuntimePayloadFactory;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;

public final class MinecraftRuntimePayloadFactory implements TransferableValueRuntimePayloadFactory {
    public static final String BINDING_ID = "bindingId";

    @Override
    public Map<String, Object> runtimeDescriptor(
            ValueDeclaration value,
            TransferableValueDescriptor descriptor,
            String bindingId) {
        if (!(descriptor instanceof MinecraftItemDescriptor minecraftDescriptor)) {
            return Map.of();
        }

        Map<String, Object> runtimeDescriptor = new LinkedHashMap<>();
        runtimeDescriptor.put(MinecraftDescriptorFactory.ITEM_ID, minecraftDescriptor.itemId());
        minecraftDescriptor.nbtPayload()
                .ifPresent(payload -> runtimeDescriptor.put(MinecraftDescriptorFactory.NBT_PAYLOAD, payload));
        return Map.copyOf(runtimeDescriptor);
    }

    @Override
    public Map<String, Object> runtimePayload(
            ActorRef firstActorRef,
            ActorRef secondActorRef,
            List<ValueDeclaration> firstValues,
            List<ValueDeclaration> secondValues,
            String bindingId) {
        return Map.of(BINDING_ID, bindingId);
    }
}

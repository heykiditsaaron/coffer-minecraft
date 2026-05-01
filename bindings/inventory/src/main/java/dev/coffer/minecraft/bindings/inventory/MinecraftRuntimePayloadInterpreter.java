package dev.coffer.minecraft.bindings.inventory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimePayloadInterpreter;
import org.coffer.runtime.model.execution.MutationExecutionRequest;

public final class MinecraftRuntimePayloadInterpreter implements TransferableValueRuntimePayloadInterpreter {
    @Override
    public Optional<Map<String, Object>> interpret(
            String bindingId,
            Map<String, Object> runtimePayload,
            MutationExecutionRequest request) {
        Objects.requireNonNull(bindingId, "bindingId");
        Objects.requireNonNull(runtimePayload, "runtimePayload");

        Object payloadBindingId = runtimePayload.get(MinecraftRuntimePayloadFactory.BINDING_ID);
        if (payloadBindingId != null && !bindingId.equals(payloadBindingId)) {
            return Optional.empty();
        }
        return Optional.of(Map.copyOf(runtimePayload));
    }
}

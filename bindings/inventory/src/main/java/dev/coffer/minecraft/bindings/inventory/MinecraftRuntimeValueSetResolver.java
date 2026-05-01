package dev.coffer.minecraft.bindings.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueSet;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeValueEntry;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeValueSetResolver;
import org.coffer.runtime.model.execution.MutationExecutionRequest;

public final class MinecraftRuntimeValueSetResolver implements TransferableValueRuntimeValueSetResolver {
    private final MinecraftDescriptorFactory descriptorFactory;

    public MinecraftRuntimeValueSetResolver() {
        this(new MinecraftDescriptorFactory());
    }

    MinecraftRuntimeValueSetResolver(MinecraftDescriptorFactory descriptorFactory) {
        this.descriptorFactory = Objects.requireNonNull(descriptorFactory, "descriptorFactory");
    }

    @Override
    public Optional<TransferableValueSet> resolve(
            List<TransferableValueRuntimeValueEntry> values,
            String bindingId,
            Map<String, Object> runtimePayload,
            MutationExecutionRequest request) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(runtimePayload, "runtimePayload");

        List<TransferableValueDescriptor> descriptors = new ArrayList<>();
        for (TransferableValueRuntimeValueEntry value : values) {
            Optional<MinecraftItemDescriptor> descriptor =
                    descriptorFactory.createRuntimeDescriptor(value.runtimeDescriptor(), value.quantity());
            if (descriptor.isEmpty()) {
                return Optional.empty();
            }
            descriptors.add(descriptor.get());
        }
        return Optional.of(new TransferableValueSet(descriptors));
    }
}

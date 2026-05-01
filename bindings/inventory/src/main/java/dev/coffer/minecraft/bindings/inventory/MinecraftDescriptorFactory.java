package dev.coffer.minecraft.bindings.inventory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueDescriptorFactory;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;

public final class MinecraftDescriptorFactory implements TransferableValueDescriptorFactory {
    public static final String ITEM_ID = "itemId";
    public static final String QUANTITY = "quantity";
    public static final String NBT_PAYLOAD = "nbtPayload";

    @Override
    public Optional<TransferableValueDescriptor> create(ValueDeclaration value, String bindingId) {
        Objects.requireNonNull(value, "value");
        return create(value.descriptor().values()).map(descriptor -> descriptor);
    }

    Optional<MinecraftItemDescriptor> createRuntimeDescriptor(Map<String, Object> runtimeDescriptor, long quantity) {
        Objects.requireNonNull(runtimeDescriptor, "runtimeDescriptor");
        return create(runtimeDescriptor, quantity);
    }

    private Optional<MinecraftItemDescriptor> create(Map<String, Object> descriptor) {
        Optional<Long> quantity = positiveLong(descriptor.get(QUANTITY));
        if (quantity.isEmpty()) {
            return Optional.empty();
        }
        return create(descriptor, quantity.get());
    }

    private Optional<MinecraftItemDescriptor> create(Map<String, Object> descriptor, long quantity) {
        Optional<String> itemId = requiredString(descriptor.get(ITEM_ID));
        Optional<Optional<String>> nbtPayload = optionalString(descriptor, NBT_PAYLOAD);
        if (itemId.isEmpty() || nbtPayload.isEmpty() || quantity <= 0) {
            return Optional.empty();
        }

        try {
            return Optional.of(new MinecraftItemDescriptor(itemId.get(), quantity, nbtPayload.get()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static Optional<String> requiredString(Object value) {
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Optional.of(stringValue);
        }
        return Optional.empty();
    }

    private static Optional<Optional<String>> optionalString(Map<String, Object> values, String key) {
        if (!values.containsKey(key) || values.get(key) == null) {
            return Optional.of(Optional.empty());
        }
        Object value = values.get(key);
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Optional.of(Optional.of(stringValue));
        }
        return Optional.empty();
    }

    private static Optional<Long> positiveLong(Object value) {
        if (value instanceof Number number) {
            long longValue = number.longValue();
            if (longValue > 0) {
                return Optional.of(longValue);
            }
        }
        return Optional.empty();
    }
}

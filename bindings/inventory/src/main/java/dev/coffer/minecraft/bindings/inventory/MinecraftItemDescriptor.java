package dev.coffer.minecraft.bindings.inventory;

import java.util.Objects;
import java.util.Optional;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;

public final class MinecraftItemDescriptor implements TransferableValueDescriptor {
    private final String itemId;
    private final long quantity;
    private final Optional<String> nbtPayload;

    public MinecraftItemDescriptor(String itemId, long quantity, Optional<String> nbtPayload) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must not be null or blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        Optional<String> checkedNbtPayload = Objects.requireNonNull(nbtPayload, "nbtPayload");
        checkedNbtPayload.ifPresent(payload -> {
            if (payload.isBlank()) {
                throw new IllegalArgumentException("nbtPayload must be absent or non-blank");
            }
        });

        this.itemId = itemId;
        this.quantity = quantity;
        this.nbtPayload = checkedNbtPayload;
    }

    public String itemId() {
        return itemId;
    }

    @Override
    public long quantity() {
        return quantity;
    }

    public Optional<String> nbtPayload() {
        return nbtPayload;
    }
}

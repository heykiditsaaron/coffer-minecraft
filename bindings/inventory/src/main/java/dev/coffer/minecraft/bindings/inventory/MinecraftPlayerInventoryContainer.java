package dev.coffer.minecraft.bindings.inventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.item.ItemStack;
import org.coffer.firstparty.authority.transferablevalue.port.MutationApplicationResult;
import org.coffer.firstparty.authority.transferablevalue.port.ReceivabilityResult;
import org.coffer.firstparty.authority.transferablevalue.port.RemovabilityResult;
import org.coffer.firstparty.authority.transferablevalue.port.SimulationResult;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueContainer;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueSet;

public final class MinecraftPlayerInventoryContainer implements TransferableValueContainer {
    public static final String VALUE_NOT_REMOVABLE = "minecraft.value.not_removable";
    public static final String CONTAINER_UNAVAILABLE = "minecraft.container.unavailable";

    private final String containerId;
    private final Region region;
    private final Supplier<Optional<List<ItemStack>>> slotsResolver;

    public MinecraftPlayerInventoryContainer(UUID playerId, Region region, List<ItemStack> slots) {
        this(containerId(playerId, region), region, () -> Optional.of(List.copyOf(slots)));
    }

    public MinecraftPlayerInventoryContainer(
            String containerId,
            Region region,
            Supplier<Optional<List<ItemStack>>> slotsResolver) {
        if (containerId == null || containerId.isBlank()) {
            throw new IllegalArgumentException("containerId must not be null or blank");
        }
        this.containerId = containerId;
        this.region = Objects.requireNonNull(region, "region");
        this.slotsResolver = Objects.requireNonNull(slotsResolver, "slotsResolver");
    }

    public String containerId() {
        return containerId;
    }

    public Region region() {
        return region;
    }

    @Override
    public RemovabilityResult canRemove(TransferableValueSet values) {
        Objects.requireNonNull(values, "values");

        Optional<List<ItemStack>> resolvedSlots = slotsResolver.get();
        if (resolvedSlots == null || resolvedSlots.isEmpty()) {
            return new RemovabilityResult.Unknown(CONTAINER_UNAVAILABLE);
        }

        Map<DescriptorKey, RequiredValue> requiredValues = new LinkedHashMap<>();
        for (TransferableValueDescriptor value : values.values()) {
            if (!(value instanceof MinecraftItemDescriptor descriptor)) {
                return new RemovabilityResult.Failed(VALUE_NOT_REMOVABLE);
            }

            DescriptorKey key = DescriptorKey.from(descriptor);
            requiredValues.merge(
                    key,
                    new RequiredValue(descriptor, descriptor.quantity()),
                    (existing, incoming) -> existing.plus(incoming.quantity()));
        }

        for (RequiredValue requiredValue : requiredValues.values()) {
            long available = matchingQuantity(resolvedSlots.get(), requiredValue.descriptor());
            if (available < requiredValue.quantity()) {
                return new RemovabilityResult.Failed(VALUE_NOT_REMOVABLE);
            }
        }

        return new RemovabilityResult.Success();
    }

    @Override
    public ReceivabilityResult canReceive(TransferableValueSet values) {
        throw new UnsupportedOperationException("canReceive is not implemented yet");
    }

    @Override
    public SimulationResult simulateAtomicSwap(
            TransferableValueContainer other,
            TransferableValueSet fromThis,
            TransferableValueSet fromOther) {
        throw new UnsupportedOperationException("simulateAtomicSwap is not implemented yet");
    }

    @Override
    public MutationApplicationResult applyAtomicSwap(
            TransferableValueContainer other,
            TransferableValueSet fromThis,
            TransferableValueSet fromOther) {
        throw new UnsupportedOperationException("applyAtomicSwap is not implemented yet");
    }

    private static long matchingQuantity(List<ItemStack> slots, MinecraftItemDescriptor descriptor) {
        long total = 0;
        for (ItemStack stack : slots) {
            if (MinecraftItemMatcher.matches(stack, descriptor)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static String containerId(UUID playerId, Region region) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(region, "region");
        return "player:" + playerId + ":inventory:" + region.serializedName();
    }

    public enum Region {
        MAIN("main"),
        HOTBAR("hotbar"),
        ARMOR("armor"),
        OFFHAND("offhand");

        private final String serializedName;

        Region(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }
    }

    private record DescriptorKey(String itemId, Optional<String> nbtPayload) {
        static DescriptorKey from(MinecraftItemDescriptor descriptor) {
            return new DescriptorKey(descriptor.itemId(), descriptor.nbtPayload());
        }
    }

    private record RequiredValue(MinecraftItemDescriptor descriptor, long quantity) {
        RequiredValue plus(long additionalQuantity) {
            return new RequiredValue(descriptor, quantity + additionalQuantity);
        }
    }
}

package dev.coffer.minecraft.bindings.inventory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.coffer.firstparty.authority.transferablevalue.port.MutationApplicationResult;
import org.coffer.firstparty.authority.transferablevalue.port.ReceivabilityResult;
import org.coffer.firstparty.authority.transferablevalue.port.RemovabilityResult;
import org.coffer.firstparty.authority.transferablevalue.port.SimulationResult;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueContainer;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueSet;

public final class MinecraftPlayerInventoryContainer implements TransferableValueContainer {
    public static final String VALUE_NOT_REMOVABLE = "minecraft.value.not_removable";
    public static final String VALUE_NOT_RECEIVABLE = "minecraft.value.not_receivable";
    public static final String CONTAINER_UNAVAILABLE = "minecraft.container.unavailable";

    private final String containerId;
    private final Region region;
    private final Supplier<Optional<List<ItemStack>>> slotsResolver;

    public MinecraftPlayerInventoryContainer(UUID playerId, Region region, List<ItemStack> slots) {
        this(containerId(playerId, region), region, () -> Optional.of(Objects.requireNonNull(slots, "slots")));
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

        Optional<Map<DescriptorKey, RequiredValue>> requiredValues = aggregate(values);
        if (requiredValues.isEmpty()) {
            return new RemovabilityResult.Failed(VALUE_NOT_REMOVABLE);
        }

        return canRemoveFrom(resolvedSlots.get(), requiredValues.get());
    }

    @Override
    public ReceivabilityResult canReceive(TransferableValueSet values) {
        Objects.requireNonNull(values, "values");

        Optional<List<ItemStack>> resolvedSlots = slotsResolver.get();
        if (resolvedSlots == null || resolvedSlots.isEmpty()) {
            return new ReceivabilityResult.Unknown(CONTAINER_UNAVAILABLE);
        }

        Optional<Map<DescriptorKey, RequiredValue>> incomingValues = aggregate(values);
        if (incomingValues.isEmpty()) {
            return new ReceivabilityResult.Failed(VALUE_NOT_RECEIVABLE);
        }

        return canReceiveInto(resolvedSlots.get(), incomingValues.get());
    }

    @Override
    public SimulationResult simulateAtomicSwap(
            TransferableValueContainer other,
            TransferableValueSet fromThis,
            TransferableValueSet fromOther) {
        Objects.requireNonNull(other, "other");
        Objects.requireNonNull(fromThis, "fromThis");
        Objects.requireNonNull(fromOther, "fromOther");

        if (!(other instanceof MinecraftPlayerInventoryContainer otherContainer)) {
            return new SimulationResult.Unknown(CONTAINER_UNAVAILABLE);
        }

        Optional<List<ItemStack>> thisResolvedSlots = slotsResolver.get();
        Optional<List<ItemStack>> otherResolvedSlots = otherContainer.slotsResolver.get();
        if (thisResolvedSlots == null || thisResolvedSlots.isEmpty()
                || otherResolvedSlots == null || otherResolvedSlots.isEmpty()) {
            return new SimulationResult.Unknown(CONTAINER_UNAVAILABLE);
        }

        Optional<Map<DescriptorKey, RequiredValue>> thisOutgoing = aggregate(fromThis);
        Optional<Map<DescriptorKey, RequiredValue>> otherOutgoing = aggregate(fromOther);
        if (thisOutgoing.isEmpty() || otherOutgoing.isEmpty()) {
            return new SimulationResult.Failed(VALUE_NOT_REMOVABLE);
        }

        List<ItemStack> thisWorkingSlots = copySlots(thisResolvedSlots.get());
        List<ItemStack> otherWorkingSlots = copySlots(otherResolvedSlots.get());

        RemovabilityResult thisRemoval = removeFrom(thisWorkingSlots, thisOutgoing.get());
        if (thisRemoval instanceof RemovabilityResult.Failed failed) {
            return new SimulationResult.Failed(failed.reasonCode());
        }
        if (thisRemoval instanceof RemovabilityResult.Unknown unknown) {
            return new SimulationResult.Unknown(unknown.reasonCode());
        }

        RemovabilityResult otherRemoval = removeFrom(otherWorkingSlots, otherOutgoing.get());
        if (otherRemoval instanceof RemovabilityResult.Failed failed) {
            return new SimulationResult.Failed(failed.reasonCode());
        }
        if (otherRemoval instanceof RemovabilityResult.Unknown unknown) {
            return new SimulationResult.Unknown(unknown.reasonCode());
        }

        ReceivabilityResult otherReceive = insertInto(otherWorkingSlots, thisOutgoing.get());
        if (otherReceive instanceof ReceivabilityResult.Failed failed) {
            return new SimulationResult.Failed(failed.reasonCode());
        }
        if (otherReceive instanceof ReceivabilityResult.Unknown unknown) {
            return new SimulationResult.Unknown(unknown.reasonCode());
        }

        ReceivabilityResult thisReceive = insertInto(thisWorkingSlots, otherOutgoing.get());
        if (thisReceive instanceof ReceivabilityResult.Failed failed) {
            return new SimulationResult.Failed(failed.reasonCode());
        }
        if (thisReceive instanceof ReceivabilityResult.Unknown unknown) {
            return new SimulationResult.Unknown(unknown.reasonCode());
        }

        return new SimulationResult.Success();
    }

    @Override
    public MutationApplicationResult applyAtomicSwap(
            TransferableValueContainer other,
            TransferableValueSet fromThis,
            TransferableValueSet fromOther) {
        SimulationResult simulation = simulateAtomicSwap(other, fromThis, fromOther);
        if (simulation instanceof SimulationResult.Failed failed) {
            return new MutationApplicationResult.Failed(failed.reasonCode());
        }
        if (simulation instanceof SimulationResult.Unknown unknown) {
            return new MutationApplicationResult.Unknown(unknown.reasonCode());
        }

        if (!(other instanceof MinecraftPlayerInventoryContainer otherContainer)) {
            return new MutationApplicationResult.Unknown(CONTAINER_UNAVAILABLE);
        }

        Optional<List<ItemStack>> thisResolvedSlots = slotsResolver.get();
        Optional<List<ItemStack>> otherResolvedSlots = otherContainer.slotsResolver.get();
        if (thisResolvedSlots == null || thisResolvedSlots.isEmpty()
                || otherResolvedSlots == null || otherResolvedSlots.isEmpty()) {
            return new MutationApplicationResult.Unknown(CONTAINER_UNAVAILABLE);
        }

        Optional<Map<DescriptorKey, RequiredValue>> thisOutgoing = aggregate(fromThis);
        Optional<Map<DescriptorKey, RequiredValue>> otherOutgoing = aggregate(fromOther);
        if (thisOutgoing.isEmpty() || otherOutgoing.isEmpty()) {
            return new MutationApplicationResult.Unknown(VALUE_NOT_REMOVABLE);
        }

        Optional<List<ItemStack>> removedFromThis = removeStacksFrom(thisResolvedSlots.get(), thisOutgoing.get());
        if (removedFromThis.isEmpty()) {
            return new MutationApplicationResult.Unknown(VALUE_NOT_REMOVABLE);
        }

        Optional<List<ItemStack>> removedFromOther = removeStacksFrom(otherResolvedSlots.get(), otherOutgoing.get());
        if (removedFromOther.isEmpty()) {
            return new MutationApplicationResult.Unknown(VALUE_NOT_REMOVABLE);
        }

        if (!insertStacksInto(otherResolvedSlots.get(), removedFromThis.get())) {
            return new MutationApplicationResult.Unknown(VALUE_NOT_RECEIVABLE);
        }

        if (!insertStacksInto(thisResolvedSlots.get(), removedFromOther.get())) {
            return new MutationApplicationResult.Unknown(VALUE_NOT_RECEIVABLE);
        }

        return new MutationApplicationResult.Success();
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

    private static RemovabilityResult canRemoveFrom(
            List<ItemStack> slots,
            Map<DescriptorKey, RequiredValue> requiredValues) {
        for (RequiredValue requiredValue : requiredValues.values()) {
            long available = matchingQuantity(slots, requiredValue.descriptor());
            if (available < requiredValue.quantity()) {
                return new RemovabilityResult.Failed(VALUE_NOT_REMOVABLE);
            }
        }

        return new RemovabilityResult.Success();
    }

    private static ReceivabilityResult canReceiveInto(
            List<ItemStack> slots,
            Map<DescriptorKey, RequiredValue> incomingValues) {
        long emptySlots = emptySlotCount(slots);
        for (RequiredValue incomingValue : incomingValues.values()) {
            Optional<Integer> maxCount = maxCountForDescriptor(incomingValue.descriptor());
            if (maxCount.isEmpty()) {
                return new ReceivabilityResult.Failed(VALUE_NOT_RECEIVABLE);
            }

            long remaining = incomingValue.quantity() - compatiblePartialCapacity(slots, incomingValue.descriptor());
            if (remaining <= 0) {
                continue;
            }

            long requiredEmptySlots = divideRoundingUp(remaining, maxCount.get());
            if (requiredEmptySlots > emptySlots) {
                return new ReceivabilityResult.Failed(VALUE_NOT_RECEIVABLE);
            }
            emptySlots -= requiredEmptySlots;
        }

        return new ReceivabilityResult.Success();
    }

    private static RemovabilityResult removeFrom(
            List<ItemStack> slots,
            Map<DescriptorKey, RequiredValue> outgoingValues) {
        RemovabilityResult canRemove = canRemoveFrom(slots, outgoingValues);
        if (!(canRemove instanceof RemovabilityResult.Success)) {
            return canRemove;
        }

        for (RequiredValue outgoingValue : outgoingValues.values()) {
            long remaining = outgoingValue.quantity();
            for (ItemStack stack : slots) {
                if (remaining <= 0) {
                    break;
                }
                if (!MinecraftItemMatcher.matches(stack, outgoingValue.descriptor())) {
                    continue;
                }
                int removed = (int) Math.min(remaining, stack.getCount());
                stack.decrement(removed);
                remaining -= removed;
            }
        }

        return new RemovabilityResult.Success();
    }

    private static ReceivabilityResult insertInto(
            List<ItemStack> slots,
            Map<DescriptorKey, RequiredValue> incomingValues) {
        ReceivabilityResult canReceive = canReceiveInto(slots, incomingValues);
        if (!(canReceive instanceof ReceivabilityResult.Success)) {
            return canReceive;
        }

        for (RequiredValue incomingValue : incomingValues.values()) {
            long remaining = incomingValue.quantity();

            for (ItemStack stack : slots) {
                if (remaining <= 0) {
                    break;
                }
                if (!MinecraftItemMatcher.matches(stack, incomingValue.descriptor())) {
                    continue;
                }
                int accepted = (int) Math.min(remaining, stack.getMaxCount() - stack.getCount());
                stack.increment(accepted);
                remaining -= accepted;
            }

            for (int index = 0; index < slots.size() && remaining > 0; index++) {
                if (!slots.get(index).isEmpty()) {
                    continue;
                }
                Optional<ItemStack> stack = stackFrom(incomingValue.descriptor());
                if (stack.isEmpty()) {
                    return new ReceivabilityResult.Failed(VALUE_NOT_RECEIVABLE);
                }
                int accepted = (int) Math.min(remaining, stack.get().getMaxCount());
                stack.get().setCount(accepted);
                slots.set(index, stack.get());
                remaining -= accepted;
            }
        }

        return new ReceivabilityResult.Success();
    }

    private static Optional<List<ItemStack>> removeStacksFrom(
            List<ItemStack> slots,
            Map<DescriptorKey, RequiredValue> outgoingValues) {
        if (!(canRemoveFrom(slots, outgoingValues) instanceof RemovabilityResult.Success)) {
            return Optional.empty();
        }

        List<ItemStack> removedStacks = new ArrayList<>();
        try {
            for (RequiredValue outgoingValue : outgoingValues.values()) {
                long remaining = outgoingValue.quantity();
                for (int index = 0; index < slots.size() && remaining > 0; index++) {
                    ItemStack stack = slots.get(index);
                    if (!MinecraftItemMatcher.matches(stack, outgoingValue.descriptor())) {
                        continue;
                    }
                    int removed = (int) Math.min(remaining, stack.getCount());
                    ItemStack removedStack = stack.split(removed);
                    if (stack.isEmpty()) {
                        slots.set(index, ItemStack.EMPTY);
                    }
                    removedStacks.add(removedStack);
                    remaining -= removed;
                }
                if (remaining > 0) {
                    return Optional.empty();
                }
            }
        } catch (RuntimeException exception) {
            return Optional.empty();
        }

        return Optional.of(removedStacks);
    }

    private static boolean insertStacksInto(List<ItemStack> slots, List<ItemStack> incomingStacks) {
        try {
            for (ItemStack incomingStack : incomingStacks) {
                ItemStack remainingStack = incomingStack.copy();

                for (ItemStack stack : slots) {
                    if (remainingStack.isEmpty()) {
                        break;
                    }
                    if (!canMerge(stack, remainingStack)) {
                        continue;
                    }
                    int accepted = Math.min(remainingStack.getCount(), stack.getMaxCount() - stack.getCount());
                    stack.increment(accepted);
                    remainingStack.decrement(accepted);
                }

                for (int index = 0; index < slots.size() && !remainingStack.isEmpty(); index++) {
                    if (!slots.get(index).isEmpty()) {
                        continue;
                    }
                    int accepted = Math.min(remainingStack.getCount(), remainingStack.getMaxCount());
                    ItemStack inserted = remainingStack.copyWithCount(accepted);
                    slots.set(index, inserted);
                    remainingStack.decrement(accepted);
                }

                if (!remainingStack.isEmpty()) {
                    return false;
                }
            }
        } catch (RuntimeException exception) {
            return false;
        }

        return true;
    }

    private static boolean canMerge(ItemStack existing, ItemStack incoming) {
        if (existing.isEmpty() || incoming.isEmpty()) {
            return false;
        }
        if (existing.getCount() >= existing.getMaxCount()) {
            return false;
        }
        return ItemStack.canCombine(existing, incoming);
    }

    private static Optional<Map<DescriptorKey, RequiredValue>> aggregate(TransferableValueSet values) {
        Map<DescriptorKey, RequiredValue> aggregatedValues = new LinkedHashMap<>();
        for (TransferableValueDescriptor value : values.values()) {
            if (!(value instanceof MinecraftItemDescriptor descriptor)) {
                return Optional.empty();
            }

            DescriptorKey key = DescriptorKey.from(descriptor);
            aggregatedValues.merge(
                    key,
                    new RequiredValue(descriptor, descriptor.quantity()),
                    (existing, incoming) -> existing.plus(incoming.quantity()));
        }
        return Optional.of(aggregatedValues);
    }

    private static long compatiblePartialCapacity(List<ItemStack> slots, MinecraftItemDescriptor descriptor) {
        long capacity = 0;
        for (ItemStack stack : slots) {
            if (MinecraftItemMatcher.matches(stack, descriptor)) {
                capacity += Math.max(0, stack.getMaxCount() - stack.getCount());
            }
        }
        return capacity;
    }

    private static long emptySlotCount(List<ItemStack> slots) {
        long emptySlots = 0;
        for (ItemStack stack : slots) {
            if (stack.isEmpty()) {
                emptySlots++;
            }
        }
        return emptySlots;
    }

    private static Optional<Integer> maxCountForDescriptor(MinecraftItemDescriptor descriptor) {
        Optional<ItemStack> stack = stackFrom(descriptor);
        return stack.map(ItemStack::getMaxCount);
    }

    private static Optional<ItemStack> stackFrom(MinecraftItemDescriptor descriptor) {
        if (descriptor.nbtPayload().isPresent()) {
            try {
                StringNbtReader.parse(descriptor.nbtPayload().orElseThrow());
            } catch (CommandSyntaxException exception) {
                return Optional.empty();
            }
        }

        Identifier id = Identifier.tryParse(descriptor.itemId());
        if (id == null || !Registries.ITEM.containsId(id)) {
            return Optional.empty();
        }
        ItemStack representative = new ItemStack(Registries.ITEM.get(id));
        if (representative.isEmpty()) {
            return Optional.empty();
        }
        if (descriptor.nbtPayload().isPresent()) {
            try {
                representative.setNbt(StringNbtReader.parse(descriptor.nbtPayload().orElseThrow()));
            } catch (CommandSyntaxException exception) {
                return Optional.empty();
            }
        }
        return Optional.of(representative);
    }

    private static long divideRoundingUp(long value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private static List<ItemStack> copySlots(List<ItemStack> slots) {
        return slots.stream()
                .map(ItemStack::copy)
                .collect(Collectors.toCollection(ArrayList::new));
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

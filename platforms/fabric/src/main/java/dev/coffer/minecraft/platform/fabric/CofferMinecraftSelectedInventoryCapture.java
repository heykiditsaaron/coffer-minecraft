package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

final class CofferMinecraftSelectedInventoryCapture {
    private static final int HOTBAR_SIZE = 9;
    private static final String MAIN_HAND_HOTBAR = "main_hand_hotbar";

    private CofferMinecraftSelectedInventoryCapture() {
    }

    static SelectedInventoryCaptureResult capture(ServerPlayerEntity player) {
        Objects.requireNonNull(player, "player");
        return capture(
                player.getUuid(),
                player.getInventory().selectedSlot,
                player.getInventory().main.subList(0, HOTBAR_SIZE).stream()
                        .map(CofferMinecraftSelectedInventoryCapture::observedStack)
                        .toList(),
                observedStack(player.getMainHandStack()));
    }

    static SelectedInventoryCaptureResult capture(
            UUID playerId,
            int selectedHotbarSlot,
            List<ObservedStack> hotbar,
            ObservedStack mainHandStack) {
        Objects.requireNonNull(playerId, "playerId");
        hotbar = List.copyOf(Objects.requireNonNull(hotbar, "hotbar"));
        Objects.requireNonNull(mainHandStack, "mainHandStack");

        if (selectedHotbarSlot < 0 || selectedHotbarSlot >= HOTBAR_SIZE || hotbar.size() != HOTBAR_SIZE) {
            return new SelectedInventoryCaptureResult.Refused("SELECTED_HOTBAR_SLOT_UNAVAILABLE");
        }

        ObservedStack selectedStack = hotbar.get(selectedHotbarSlot);
        if (!selectedStack.equals(mainHandStack)) {
            return new SelectedInventoryCaptureResult.Refused("SELECTED_MAIN_HAND_MISMATCH");
        }

        return new SelectedInventoryCaptureResult.Captured(new SelectedInventorySnapshot(
                playerId,
                new SelectedValueBoundary(
                        MAIN_HAND_HOTBAR,
                        MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        selectedHotbarSlot),
                descriptor(selectedStack)));
    }

    private static ObservedStack observedStack(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) {
            return ObservedStack.empty();
        }

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        Optional<String> nbtPayload = stack.hasNbt()
                ? Optional.of(Objects.requireNonNull(stack.getNbt(), "stack nbt").toString())
                : Optional.empty();
        return new ObservedStack(itemId, stack.getCount(), nbtPayload);
    }

    private static Optional<MinecraftItemDescriptor> descriptor(ObservedStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MinecraftItemDescriptor(stack.itemId(), stack.count(), stack.nbtPayload()));
    }

    sealed interface SelectedInventoryCaptureResult
            permits SelectedInventoryCaptureResult.Captured, SelectedInventoryCaptureResult.Refused {
        record Captured(SelectedInventorySnapshot snapshot) implements SelectedInventoryCaptureResult {
            public Captured {
                Objects.requireNonNull(snapshot, "snapshot");
            }
        }

        record Refused(String reasonCode) implements SelectedInventoryCaptureResult {
            public Refused {
                if (reasonCode == null || reasonCode.isBlank()) {
                    throw new IllegalArgumentException("reasonCode must not be null or blank");
                }
            }
        }
    }

    record SelectedInventorySnapshot(
            UUID playerId,
            SelectedValueBoundary boundary,
            Optional<MinecraftItemDescriptor> selectedValue) {
        SelectedInventorySnapshot {
            Objects.requireNonNull(playerId, "playerId");
            Objects.requireNonNull(boundary, "boundary");
            selectedValue = Objects.requireNonNull(selectedValue, "selectedValue");
        }
    }

    record SelectedValueBoundary(
            String selectionKind,
            MinecraftPlayerInventoryContainer.Region region,
            int slotIndex) {
        SelectedValueBoundary {
            if (selectionKind == null || selectionKind.isBlank()) {
                throw new IllegalArgumentException("selectionKind must not be null or blank");
            }
            Objects.requireNonNull(region, "region");
            if (region != MinecraftPlayerInventoryContainer.Region.HOTBAR) {
                throw new IllegalArgumentException("selected capture region must remain HOTBAR");
            }
            if (slotIndex < 0 || slotIndex >= HOTBAR_SIZE) {
                throw new IllegalArgumentException("slotIndex must be within the hotbar");
            }
        }
    }

    record ObservedStack(String itemId, int count, Optional<String> nbtPayload) {
        ObservedStack {
            if (itemId == null || itemId.isBlank()) {
                throw new IllegalArgumentException("itemId must not be null or blank");
            }
            if (count <= 0) {
                throw new IllegalArgumentException("count must be positive");
            }
            nbtPayload = Objects.requireNonNull(nbtPayload, "nbtPayload");
        }

        static ObservedStack empty() {
            return new ObservedStack("minecraft:air", 1, Optional.empty());
        }

        boolean isEmpty() {
            return "minecraft:air".equals(itemId);
        }
    }
}

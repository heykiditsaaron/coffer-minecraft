package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CofferMinecraftSelectedInventoryCaptureTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");

    @Test
    void capturePreservesPreciseSelectedHotbarBoundaryWithoutWidening() {
        List<CofferMinecraftSelectedInventoryCapture.ObservedStack> hotbar = hotbar(
                stack("minecraft:dirt", 64),
                stack("minecraft:stone", 2),
                stack("minecraft:stone", 5),
                empty(),
                stack("minecraft:stone", 48),
                empty(),
                empty(),
                empty(),
                empty());

        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured captured =
                assertInstanceOf(
                        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured.class,
                        CofferMinecraftSelectedInventoryCapture.capture(
                                PLAYER_ID,
                                2,
                                hotbar,
                                hotbar.get(2)));

        MinecraftItemDescriptor selectedValue = captured.snapshot().selectedValue().orElseThrow();
        assertEquals("main_hand_hotbar", captured.snapshot().boundary().selectionKind());
        assertEquals(MinecraftPlayerInventoryContainer.Region.HOTBAR, captured.snapshot().boundary().region());
        assertEquals(2, captured.snapshot().boundary().slotIndex());
        assertEquals("minecraft:stone", selectedValue.itemId());
        assertEquals(5, selectedValue.quantity());
    }

    @Test
    void duplicateMatchingMaterialElsewhereInHotbarIsNotCaptured() {
        List<CofferMinecraftSelectedInventoryCapture.ObservedStack> hotbar = hotbar(
                stack("minecraft:stone", 64),
                empty(),
                stack("minecraft:stone", 3),
                empty(),
                stack("minecraft:stone", 1),
                empty(),
                empty(),
                empty(),
                empty());

        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured captured =
                assertInstanceOf(
                        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured.class,
                        CofferMinecraftSelectedInventoryCapture.capture(
                                PLAYER_ID,
                                2,
                                hotbar,
                                hotbar.get(2)));

        MinecraftItemDescriptor selectedValue = captured.snapshot().selectedValue().orElseThrow();
        assertEquals(2, captured.snapshot().boundary().slotIndex());
        assertEquals(3, selectedValue.quantity());
        assertFalse(selectedValue.quantity() == 68);
    }

    @Test
    void emptySelectedSlotIsRepresentedHonestlyWithoutInventingValue() {
        List<CofferMinecraftSelectedInventoryCapture.ObservedStack> hotbar = hotbar(
                stack("minecraft:dirt", 1),
                empty(),
                empty(),
                empty(),
                empty(),
                empty(),
                empty(),
                empty(),
                empty());

        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured captured =
                assertInstanceOf(
                        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured.class,
                        CofferMinecraftSelectedInventoryCapture.capture(
                                PLAYER_ID,
                                1,
                                hotbar,
                                empty()));

        assertEquals(1, captured.snapshot().boundary().slotIndex());
        assertTrue(captured.snapshot().selectedValue().isEmpty());
    }

    @Test
    void captureIsReadOnlyAndSnapshotRemainsStableAfterInventoryMutation() {
        List<CofferMinecraftSelectedInventoryCapture.ObservedStack> hotbar = new ArrayList<>(hotbar(
                stack("minecraft:dirt", 1),
                stack("minecraft:diamond", 4),
                empty(),
                empty(),
                empty(),
                empty(),
                empty(),
                empty(),
                empty()));
        CofferMinecraftSelectedInventoryCapture.ObservedStack selectedBeforeCapture = hotbar.get(1);

        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured captured =
                assertInstanceOf(
                        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured.class,
                        CofferMinecraftSelectedInventoryCapture.capture(
                                PLAYER_ID,
                                1,
                                hotbar,
                                hotbar.get(1)));

        assertEquals(4, hotbar.get(1).count());
        hotbar.set(1, stack("minecraft:diamond", 1));

        MinecraftItemDescriptor selectedValue = captured.snapshot().selectedValue().orElseThrow();
        assertNotSame(hotbar.get(1), selectedBeforeCapture);
        assertEquals(4, selectedBeforeCapture.count());
        assertEquals(1, hotbar.get(1).count());
        assertEquals(4, selectedValue.quantity());
    }

    @Test
    void selectedMainHandMismatchIsRefusedRatherThanWidenedOrGuessed() {
        List<CofferMinecraftSelectedInventoryCapture.ObservedStack> hotbar = hotbar(
                stack("minecraft:stone", 2),
                stack("minecraft:dirt", 5),
                empty(),
                empty(),
                empty(),
                empty(),
                empty(),
                empty(),
                empty());

        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Refused.class,
                        CofferMinecraftSelectedInventoryCapture.capture(
                                PLAYER_ID,
                                1,
                                hotbar,
                                hotbar.get(0)));

        assertEquals("SELECTED_MAIN_HAND_MISMATCH", refused.reasonCode());
    }

    @Test
    void selectedCaptureSurfaceDoesNotExposeCoreRuntimeOrMutationPath() {
        List<String> methodNames = Arrays.stream(CofferMinecraftSelectedInventoryCapture.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        assertTrue(methodNames.contains("capture"));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("submit")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("arbitrat")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("runtime")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("execute")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("mutat")));
    }

    private static List<CofferMinecraftSelectedInventoryCapture.ObservedStack> hotbar(
            CofferMinecraftSelectedInventoryCapture.ObservedStack... stacks) {
        ArrayList<CofferMinecraftSelectedInventoryCapture.ObservedStack> hotbar = new ArrayList<>(List.of(stacks));
        if (hotbar.size() != 9) {
            throw new IllegalArgumentException("hotbar must have exactly 9 slots");
        }
        return hotbar;
    }

    private static CofferMinecraftSelectedInventoryCapture.ObservedStack stack(String itemId, int count) {
        return new CofferMinecraftSelectedInventoryCapture.ObservedStack(itemId, count, java.util.Optional.empty());
    }

    private static CofferMinecraftSelectedInventoryCapture.ObservedStack empty() {
        return CofferMinecraftSelectedInventoryCapture.ObservedStack.empty();
    }
}

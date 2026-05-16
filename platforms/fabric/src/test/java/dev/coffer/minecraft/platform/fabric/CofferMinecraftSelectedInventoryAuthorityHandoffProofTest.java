package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.bindings.inventory.MinecraftContainerResolver;
import dev.coffer.minecraft.bindings.inventory.MinecraftDescriptorFactory;
import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueContainer;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;
import org.junit.jupiter.api.Test;

class CofferMinecraftSelectedInventoryAuthorityHandoffProofTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000904");

    @Test
    void selectedSnapshotBoundaryCannotBeNamedByCurrentAuthoritySurface() {
        List<CofferMinecraftSelectedInventoryCapture.ObservedStack> observedHotbar = List.of(
                observed("minecraft:stone", 64),
                CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                observed("minecraft:stone", 3),
                CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                CofferMinecraftSelectedInventoryCapture.ObservedStack.empty());

        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured captured =
                assertInstanceOf(
                        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured.class,
                        CofferMinecraftSelectedInventoryCapture.capture(
                                PLAYER_ID,
                                2,
                                observedHotbar,
                                observedHotbar.get(2)));

        MinecraftItemDescriptor selectedValue = captured.snapshot().selectedValue().orElseThrow();
        ActorDeclaration actor = new ActorDeclaration(
                new ActorRef("player:" + PLAYER_ID + ":inventory:hotbar"),
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND,
                new OpaqueObject(Map.of()));

        assertEquals(2, captured.snapshot().boundary().slotIndex());
        assertEquals("player:" + PLAYER_ID + ":inventory:hotbar", actor.actorRef().value());
        assertEquals(3, selectedValue.quantity());
        assertTrue(!actor.actorRef().value().contains(":slot:"));
    }

    @Test
    void currentResolverHasNoSelectedSlotIdentitySurfaceBeyondHotbarRegion() {
        MinecraftContainerResolver resolver = new MinecraftContainerResolver((playerId, region) -> Optional.of(List.of()));

        ActorDeclaration actor = new ActorDeclaration(
                new ActorRef("player:" + PLAYER_ID + ":inventory:hotbar"),
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND,
                new OpaqueObject(Map.of(
                        "selectionKind", "main_hand_hotbar",
                        "selectedSlot", 2)));

        TransferableValueContainer resolved = resolver.resolve(actor, null, "minecraft-inventory").orElseThrow();
        MinecraftPlayerInventoryContainer container =
                assertInstanceOf(MinecraftPlayerInventoryContainer.class, resolved);

        assertEquals("player:" + PLAYER_ID + ":inventory:hotbar", container.containerId());
        assertEquals(MinecraftPlayerInventoryContainer.Region.HOTBAR, container.region());
    }

    @Test
    void emptySelectedSnapshotHasNoCurrentAuthorityValueWithoutInventingMaterial() {
        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured captured =
                assertInstanceOf(
                        CofferMinecraftSelectedInventoryCapture.SelectedInventoryCaptureResult.Captured.class,
                        CofferMinecraftSelectedInventoryCapture.capture(
                                PLAYER_ID,
                                1,
                                List.of(
                                        observed("minecraft:dirt", 1),
                                        CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                                        CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                                        CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                                        CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                                        CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                                        CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                                        CofferMinecraftSelectedInventoryCapture.ObservedStack.empty(),
                                        CofferMinecraftSelectedInventoryCapture.ObservedStack.empty()),
                                CofferMinecraftSelectedInventoryCapture.ObservedStack.empty()));

        assertTrue(captured.snapshot().selectedValue().isEmpty());
    }

    @Test
    void currentDescriptorFlowCarriesOnlyItemMaterialAndNotSelectedBoundary() {
        MinecraftDescriptorFactory factory = new MinecraftDescriptorFactory();
        MinecraftItemDescriptor descriptor = new MinecraftItemDescriptor("minecraft:stone", 3, Optional.empty());

        Optional<TransferableValueDescriptor> created = factory.create(
                new ValueDeclaration(
                        new ValueRef("selected-value"),
                        org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority.AUTHORITY_ID,
                        new OpaqueObject(Map.of(
                                MinecraftDescriptorFactory.ITEM_ID, descriptor.itemId(),
                                MinecraftDescriptorFactory.QUANTITY, descriptor.quantity(),
                                "selectionKind", "main_hand_hotbar",
                                "selectedSlot", 2))),
                "minecraft-inventory");

        MinecraftItemDescriptor createdDescriptor =
                assertInstanceOf(MinecraftItemDescriptor.class, created.orElseThrow());
        assertEquals("minecraft:stone", createdDescriptor.itemId());
        assertEquals(3, createdDescriptor.quantity());
        assertTrue(createdDescriptor.nbtPayload().isEmpty());
    }

    private static CofferMinecraftSelectedInventoryCapture.ObservedStack observed(String itemId, int count) {
        return new CofferMinecraftSelectedInventoryCapture.ObservedStack(itemId, count, Optional.empty());
    }
}

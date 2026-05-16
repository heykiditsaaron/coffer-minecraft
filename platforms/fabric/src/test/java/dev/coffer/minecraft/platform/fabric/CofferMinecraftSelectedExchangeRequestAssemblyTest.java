package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.junit.jupiter.api.Test;

class CofferMinecraftSelectedExchangeRequestAssemblyTest {
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000911");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000912");

    @Test
    void assemblyPreservesSelectedAuthoredValuesIntoMinimalCoreFacingConstruction() {
        CofferMinecraftSelectedExchangeRequestAssembly assembly =
                new CofferMinecraftSelectedExchangeRequestAssembly(
                        TransferableValueExchangePayloadConstruction::constructAtomicSwap);

        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                participant(
                                        new ActorRef("player:" + FIRST_PLAYER_ID + ":inventory:hotbar"),
                                        "offer-first",
                                        capturedSnapshot(
                                                FIRST_PLAYER_ID,
                                                2,
                                                "minecraft:iron_sword",
                                                1,
                                                "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}")),
                                participant(
                                        new ActorRef("player:" + SECOND_PLAYER_ID + ":inventory:hotbar"),
                                        "offer-second",
                                        capturedSnapshot(
                                                SECOND_PLAYER_ID,
                                                4,
                                                "minecraft:shield",
                                                1,
                                                "{BlockEntityTag:{Base:11}}")),
                                "minecraft-inventory"));

        assertEquals("player:" + FIRST_PLAYER_ID + ":inventory:hotbar",
                prepared.construction().firstActor().actorRef().value());
        assertEquals("player:" + SECOND_PLAYER_ID + ":inventory:hotbar",
                prepared.construction().secondActor().actorRef().value());
        assertEquals("minecraft-inventory", prepared.construction().bindingId());
        assertEquals("offer-first", prepared.construction().firstOfferRef().value());
        assertEquals("offer-second", prepared.construction().secondOfferRef().value());
        assertEquals(1, prepared.construction().firstValues().size());
        assertEquals(1, prepared.construction().secondValues().size());

        Map<String, Object> firstDescriptor = prepared.construction().firstValues().get(0).descriptor().values();
        Map<String, Object> secondDescriptor = prepared.construction().secondValues().get(0).descriptor().values();
        assertEquals("minecraft:iron_sword", firstDescriptor.get("itemId"));
        assertEquals(1L, ((Number) firstDescriptor.get("quantity")).longValue());
        assertEquals(
                "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}",
                firstDescriptor.get("nbtPayload"));
        assertEquals("minecraft:shield", secondDescriptor.get("itemId"));
        assertEquals("{BlockEntityTag:{Base:11}}", secondDescriptor.get("nbtPayload"));
        assertFalse(firstDescriptor.equals(secondDescriptor));
        assertEquals("selected-exchange-payload-1", prepared.payload().payloadId().value());
    }

    @Test
    void assemblyDoesNotCarrySelectedSlotBoundaryIntoCoreFacingActorOrValueShape() {
        CofferMinecraftSelectedExchangeRequestAssembly assembly =
                new CofferMinecraftSelectedExchangeRequestAssembly(
                        TransferableValueExchangePayloadConstruction::constructAtomicSwap);

        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                participant(
                                        new ActorRef("player:" + FIRST_PLAYER_ID + ":inventory:hotbar"),
                                        "offer-first",
                                        capturedSnapshot(
                                                FIRST_PLAYER_ID,
                                                2,
                                                "minecraft:iron_sword",
                                                1,
                                                "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}")),
                                participant(
                                        new ActorRef("player:" + SECOND_PLAYER_ID + ":inventory:hotbar"),
                                        "offer-second",
                                        capturedSnapshot(
                                                SECOND_PLAYER_ID,
                                                4,
                                                "minecraft:shield",
                                                1,
                                                "{BlockEntityTag:{Base:11}}")),
                                "minecraft-inventory"));

        Map<String, Object> firstValueDescriptor = prepared.construction().firstValues().get(0).descriptor().values();

        assertEquals("player:" + FIRST_PLAYER_ID + ":inventory:hotbar",
                prepared.construction().firstActor().actorRef().value());
        assertFalse(prepared.construction().firstActor().actorRef().value().contains(":slot:"));
        assertFalse(firstValueDescriptor.containsKey("selectionKind"));
        assertFalse(firstValueDescriptor.containsKey("selectedSlot"));
    }

    @Test
    void emptySelectedCaptureIsRefusedBeforeCoreFacingPayloadConstruction() {
        AtomicInteger constructionCalls = new AtomicInteger();
        CofferMinecraftSelectedExchangeRequestAssembly assembly =
                new CofferMinecraftSelectedExchangeRequestAssembly(construction -> {
                    constructionCalls.incrementAndGet();
                    return TransferableValueExchangePayloadConstruction.constructAtomicSwap(construction);
                });

        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                participant(
                                        new ActorRef("player:" + FIRST_PLAYER_ID + ":inventory:hotbar"),
                                        "offer-first",
                                        emptyCapturedSnapshot(FIRST_PLAYER_ID, 2)),
                                participant(
                                        new ActorRef("player:" + SECOND_PLAYER_ID + ":inventory:hotbar"),
                                        "offer-second",
                                        capturedSnapshot(SECOND_PLAYER_ID, 4, "minecraft:shield", 1, null)),
                                "minecraft-inventory"));

        assertEquals(
                CofferMinecraftSelectedExchangeRequestAssembly.FIRST_SELECTED_VALUE_NOT_MATERIALIZED,
                refused.reasonCode());
        assertEquals(0, constructionCalls.get());
    }

    @Test
    void assemblyUsesConstructionOnlyWithoutRuntimeMutationOrReceiptSurface() {
        AtomicInteger constructionCalls = new AtomicInteger();
        CofferMinecraftSelectedExchangeRequestAssembly assembly =
                new CofferMinecraftSelectedExchangeRequestAssembly(construction -> {
                    constructionCalls.incrementAndGet();
                    return TransferableValueExchangePayloadConstruction.constructAtomicSwap(construction);
                });

        assembly.assemble(
                participant(
                        new ActorRef("player:" + FIRST_PLAYER_ID + ":inventory:hotbar"),
                        "offer-first",
                        capturedSnapshot(FIRST_PLAYER_ID, 2, "minecraft:stone", 3, null)),
                participant(
                        new ActorRef("player:" + SECOND_PLAYER_ID + ":inventory:hotbar"),
                        "offer-second",
                        capturedSnapshot(SECOND_PLAYER_ID, 4, "minecraft:dirt", 2, null)),
                "minecraft-inventory");

        List<String> methodNames = Arrays.stream(CofferMinecraftSelectedExchangeRequestAssembly.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        assertEquals(1, constructionCalls.get());
        assertTrue(methodNames.contains("assemble"));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("submit")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("runtime")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("mutat")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("receipt")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("confirm")));
    }

    private static CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant participant(
            ActorRef actorRef,
            String offerRef,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        return new CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant(
                actorRef,
                new OfferRef(offerRef),
                snapshot);
    }

    private static CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot capturedSnapshot(
            UUID playerId,
            int slotIndex,
            String itemId,
            int count,
            String nbtPayload) {
        return new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                playerId,
                new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                        "main_hand_hotbar",
                        dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        slotIndex),
                Optional.of(new dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor(
                        itemId,
                        count,
                        Optional.ofNullable(nbtPayload))));
    }

    private static CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot emptyCapturedSnapshot(
            UUID playerId,
            int slotIndex) {
        return new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                playerId,
                new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                        "main_hand_hotbar",
                        dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        slotIndex),
                Optional.empty());
    }
}

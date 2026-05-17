package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
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

class CofferMinecraftAdminShopPresetListingRequestAssemblyTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000001161");

    @Test
    void validInfiniteFaucetListingPermitsLawfulExchangeRequestAssembly() {
        CofferMinecraftAdminShopPresetListingRequestAssembly assembly =
                new CofferMinecraftAdminShopPresetListingRequestAssembly(
                        new CofferMinecraftAdminShopPresetListingConstruction(),
                        TransferableValueExchangePayloadConstruction::constructAtomicSwap);

        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(infiniteListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, null)),
                                "minecraft-inventory"));

        assertEquals("listing-emerald-for-diamond", prepared.exchange().listingId());
        assertEquals("admin-shop:listing:listing-emerald-for-diamond",
                prepared.construction().secondActor().actorRef().value());
        assertEquals(CofferMinecraftAdminShopPresetListingRequestAssembly.SHOP_LISTING_ACTOR_KIND,
                prepared.construction().secondActor().kind());
        assertEquals("minecraft-inventory", prepared.construction().bindingId());
    }

    @Test
    void validFiniteFaucetListingPermitsLawfulExchangeRequestAssembly() {
        CofferMinecraftAdminShopPresetListingRequestAssembly assembly =
                new CofferMinecraftAdminShopPresetListingRequestAssembly(
                        new CofferMinecraftAdminShopPresetListingConstruction(),
                        TransferableValueExchangePayloadConstruction::constructAtomicSwap);

        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(finiteListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, null)),
                                "minecraft-inventory"));

        assertEquals("listing-finite-emerald-for-diamond", prepared.exchange().listingId());
        assertEquals("admin-shop:listing:listing-finite-emerald-for-diamond",
                prepared.construction().secondActor().actorRef().value());
        assertEquals("shop-listing-offer-listing-finite-emerald-for-diamond",
                prepared.construction().secondOfferRef().value());
    }

    @Test
    void assemblyPreservesPlayerAndShopTransferableValueIdentity() {
        CofferMinecraftAdminShopPresetListingRequestAssembly assembly =
                new CofferMinecraftAdminShopPresetListingRequestAssembly(
                        new CofferMinecraftAdminShopPresetListingConstruction(),
                        TransferableValueExchangePayloadConstruction::constructAtomicSwap);

        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(finiteListingAcceptingCustomEmerald()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}")),
                                "minecraft-inventory"));

        Map<String, Object> playerDescriptor = prepared.construction().firstValues().get(0).descriptor().values();
        Map<String, Object> shopDescriptor = prepared.construction().secondValues().get(0).descriptor().values();

        assertEquals("minecraft:emerald", playerDescriptor.get("itemId"));
        assertEquals(3L, ((Number) playerDescriptor.get("quantity")).longValue());
        assertEquals("{custom:1b}", playerDescriptor.get("nbtPayload"));
        assertEquals("minecraft:diamond", shopDescriptor.get("itemId"));
        assertEquals(1L, ((Number) shopDescriptor.get("quantity")).longValue());
        assertEquals("{Damage:2}", shopDescriptor.get("nbtPayload"));
    }

    @Test
    void infiniteAndFiniteSupplyModesRemainReconstructableAfterAssemblyWithoutCrossCounterfeitingSemantics() {
        CofferMinecraftAdminShopPresetListingRequestAssembly assembly =
                new CofferMinecraftAdminShopPresetListingRequestAssembly(
                        new CofferMinecraftAdminShopPresetListingConstruction(),
                        TransferableValueExchangePayloadConstruction::constructAtomicSwap);

        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared infinitePrepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(infiniteListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, null)),
                                "minecraft-inventory"));
        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared finitePrepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(finiteListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, null)),
                                "minecraft-inventory"));

        CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet infinite =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet.class,
                        infinitePrepared.exchange().supplyMode());
        CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet finite =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet.class,
                        finitePrepared.exchange().supplyMode());

        assertEquals("spawn-shop-wall-a", infinite.listingSurfaceId());
        assertFalse(infinitePrepared.exchange().supplyMode()
                instanceof CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet);
        assertEquals("shop:spawn:stock:diamond-a", finite.supplyContainerId());
    }

    @Test
    void missingDisabledAndMismatchedListingsRemainPreCoreRefusal() {
        AtomicInteger constructionCalls = new AtomicInteger();
        CofferMinecraftAdminShopPresetListingRequestAssembly assembly =
                new CofferMinecraftAdminShopPresetListingRequestAssembly(
                        new CofferMinecraftAdminShopPresetListingConstruction(),
                        construction -> {
                            constructionCalls.incrementAndGet();
                            return TransferableValueExchangePayloadConstruction.constructAtomicSwap(construction);
                        });

        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Refused missing =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                Optional.empty(),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, null)),
                                "minecraft-inventory"));
        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Refused disabled =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                Optional.of(disabledInfiniteListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, null)),
                                "minecraft-inventory"));
        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Refused mismatched =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                Optional.of(infiniteListing()),
                                playerParticipant(selectedSnapshot("minecraft:diamond", 3, null)),
                                "minecraft-inventory"));

        assertEquals(CofferMinecraftAdminShopPresetListingConstruction.MISSING_LISTING, missing.reasonCode());
        assertEquals(CofferMinecraftAdminShopPresetListingConstruction.LISTING_UNAVAILABLE, disabled.reasonCode());
        assertEquals(CofferMinecraftAdminShopPresetListingConstruction.PLAYER_SELECTED_VALUE_MISMATCH, mismatched.reasonCode());
        assertEquals(0, constructionCalls.get());
    }

    @Test
    void assemblyUsesConstructionOnlyWithoutRuntimeMutationOrDepletionSurface() {
        List<String> methodNames = Arrays.stream(CofferMinecraftAdminShopPresetListingRequestAssembly.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        assertTrue(methodNames.contains("assemble"));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("runtime")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("mutat")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("execut")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("receipt")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("deplet")));
    }

    private static CofferMinecraftAdminShopPresetListingRequestAssembly.SelectedParticipant playerParticipant(
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        return new CofferMinecraftAdminShopPresetListingRequestAssembly.SelectedParticipant(
                new ActorRef("player:" + PLAYER_ID + ":inventory:hotbar"),
                new OfferRef("player-shop-offer"),
                snapshot);
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing infiniteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet("spawn-shop-wall-a"),
                descriptor("minecraft:diamond", 1, null),
                descriptor("minecraft:emerald", 3, null));
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing disabledInfiniteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-disabled",
                false,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet("spawn-shop-wall-b"),
                descriptor("minecraft:diamond", 1, null),
                descriptor("minecraft:emerald", 3, null));
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing finiteListing() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-finite-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet("shop:spawn:stock:diamond-a"),
                descriptor("minecraft:diamond", 1, "{Damage:2}"),
                descriptor("minecraft:emerald", 3, null));
    }

    private static CofferMinecraftAdminShopPresetListingConstruction.PresetListing finiteListingAcceptingCustomEmerald() {
        return new CofferMinecraftAdminShopPresetListingConstruction.PresetListing(
                "listing-finite-custom-emerald-for-diamond",
                true,
                new CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet("shop:spawn:stock:diamond-b"),
                descriptor("minecraft:diamond", 1, "{Damage:2}"),
                descriptor("minecraft:emerald", 3, "{custom:1b}"));
    }

    private static CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot selectedSnapshot(
            String itemId,
            long quantity,
            String nbtPayload) {
        return new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                PLAYER_ID,
                new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                        "main_hand_hotbar",
                        MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        2),
                Optional.of(descriptor(itemId, quantity, nbtPayload)));
    }

    private static MinecraftItemDescriptor descriptor(String itemId, long quantity, String nbtPayload) {
        return new MinecraftItemDescriptor(itemId, quantity, Optional.ofNullable(nbtPayload));
    }
}

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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CofferMinecraftAdminShopPresetListingConstructionTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000001141");

    @Test
    void missingListingRefusesHonestlyBeforeCoreSubmission() {
        CofferMinecraftAdminShopPresetListingConstruction construction =
                new CofferMinecraftAdminShopPresetListingConstruction();

        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(Optional.empty(), selectedSnapshot("minecraft:emerald", 3, null)));

        assertEquals(CofferMinecraftAdminShopPresetListingConstruction.MISSING_LISTING, refused.reasonCode());
    }

    @Test
    void disabledListingRefusesHonestlyBeforeCoreSubmission() {
        CofferMinecraftAdminShopPresetListingConstruction construction =
                new CofferMinecraftAdminShopPresetListingConstruction();

        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(Optional.of(disabledInfiniteListing()), selectedSnapshot("minecraft:emerald", 3, null)));

        assertEquals(CofferMinecraftAdminShopPresetListingConstruction.LISTING_UNAVAILABLE, refused.reasonCode());
    }

    @Test
    void playerSelectedValueMismatchRefusesHonestlyBeforeCoreSubmission() {
        CofferMinecraftAdminShopPresetListingConstruction construction =
                new CofferMinecraftAdminShopPresetListingConstruction();

        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(Optional.of(infiniteListing()), selectedSnapshot("minecraft:diamond", 3, null)));

        assertEquals(CofferMinecraftAdminShopPresetListingConstruction.PLAYER_SELECTED_VALUE_MISMATCH, refused.reasonCode());
    }

    @Test
    void validPlayerSelectedValuePermitsConcreteExchangeConstructionReadiness() {
        CofferMinecraftAdminShopPresetListingConstruction construction =
                new CofferMinecraftAdminShopPresetListingConstruction();

        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Ready ready =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Ready.class,
                        construction.prepare(Optional.of(infiniteListing()), selectedSnapshot("minecraft:emerald", 3, null)));

        assertEquals("listing-emerald-for-diamond", ready.exchange().listingId());
        assertEquals("minecraft:diamond", ready.exchange().offeredValue().itemId());
        assertEquals(1L, ready.exchange().offeredValue().quantity());
        assertEquals("minecraft:emerald", ready.exchange().acceptedCounterOffer().itemId());
        assertEquals(3L, ready.exchange().acceptedCounterOffer().quantity());
        assertEquals("minecraft:emerald", ready.exchange().playerSelectedValue().selectedValue().orElseThrow().itemId());
    }

    @Test
    void infiniteFaucetListingIdentityIsExplicitAndReconstructableWithoutFiniteDepletion() {
        CofferMinecraftAdminShopPresetListingConstruction construction =
                new CofferMinecraftAdminShopPresetListingConstruction();

        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Ready ready =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Ready.class,
                        construction.prepare(Optional.of(infiniteListing()), selectedSnapshot("minecraft:emerald", 3, null)));

        CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet infinite =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet.class,
                        ready.exchange().supplyMode());

        assertEquals("spawn-shop-wall-a", infinite.listingSurfaceId());
        assertFalse(ready.exchange().supplyMode()
                instanceof CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet);
    }

    @Test
    void finiteFaucetListingIdentityIsExplicitAndReconstructable() {
        CofferMinecraftAdminShopPresetListingConstruction construction =
                new CofferMinecraftAdminShopPresetListingConstruction();

        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Ready ready =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.ConstructionResult.Ready.class,
                        construction.prepare(Optional.of(finiteListing()), selectedSnapshot("minecraft:emerald", 3, null)));

        CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet finite =
                assertInstanceOf(
                        CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet.class,
                        ready.exchange().supplyMode());

        assertEquals("shop:spawn:stock:diamond-a", finite.supplyContainerId());
    }

    @Test
    void constructionSurfaceDoesNotMutateOrEnterRuntime() {
        List<String> methodNames = Arrays.stream(CofferMinecraftAdminShopPresetListingConstruction.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        assertTrue(methodNames.contains("prepare"));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("runtime")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("mutat")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("execut")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("submit")));
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

    private static CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot selectedSnapshot(
            String itemId,
            long quantity,
            String nbtPayload) {
        return new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                PLAYER_ID,
                new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                        "main_hand_hotbar",
                        MinecraftPlayerInventoryContainer.Region.HOTBAR,
                        1),
                Optional.of(descriptor(itemId, quantity, nbtPayload)));
    }

    private static MinecraftItemDescriptor descriptor(String itemId, long quantity, String nbtPayload) {
        return new MinecraftItemDescriptor(itemId, quantity, Optional.ofNullable(nbtPayload));
    }
}

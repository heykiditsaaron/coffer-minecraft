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
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CofferMinecraftAdminShopMixedLedgerListingConstructionTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000001221");

    @Test
    void inventoryOfferedAndLedgerAcceptedListingPermitsConstructionReadiness() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Ready ready =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Ready.class,
                        construction.prepare(Optional.of(inventoryForLedgerListing()), Optional.empty()));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm offeredInventory =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm.class,
                        ready.exchange().offeredTerm());
        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm acceptedLedger =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm.class,
                        ready.exchange().acceptedTerm());

        assertEquals("listing-diamond-for-ledger", ready.exchange().listingId());
        assertEquals("minecraft:diamond", offeredInventory.value().itemId());
        assertEquals(1L, offeredInventory.value().quantity());
        assertEquals("proof-ledger-authority", acceptedLedger.value().authorityId());
        assertEquals("spawn-ledger", acceptedLedger.value().ledgerId());
        assertEquals("player:" + PLAYER_ID + ":wallet", acceptedLedger.value().accountId());
        assertEquals("proof:coin", acceptedLedger.value().unitId());
        assertEquals(125L, acceptedLedger.value().amount());
        assertTrue(ready.exchange().playerSelectedCounterOffer().isEmpty());
    }

    @Test
    void ledgerOfferedAndInventoryAcceptedListingPermitsConstructionReadiness() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Ready ready =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Ready.class,
                        construction.prepare(
                                Optional.of(ledgerForInventoryListing()),
                                Optional.of(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}"))));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm offeredLedger =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm.class,
                        ready.exchange().offeredTerm());
        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm acceptedInventory =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm.class,
                        ready.exchange().acceptedTerm());

        assertEquals("proof-ledger-authority", offeredLedger.value().authorityId());
        assertEquals("spawn-ledger", offeredLedger.value().ledgerId());
        assertEquals("shop:spawn:treasury", offeredLedger.value().accountId());
        assertEquals("proof:coin", offeredLedger.value().unitId());
        assertEquals(125L, offeredLedger.value().amount());
        assertEquals("minecraft:emerald", acceptedInventory.value().itemId());
        assertEquals(3L, acceptedInventory.value().quantity());
        assertEquals("{custom:1b}", acceptedInventory.value().nbtPayload().orElseThrow());
        assertEquals("minecraft:emerald",
                ready.exchange().playerSelectedCounterOffer().orElseThrow().selectedValue().orElseThrow().itemId());
    }

    @Test
    void missingLedgerParticipationRefusesHonestlyBeforeCoreSubmission() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(
                                Optional.of(invalidInventoryOnlyListing()),
                                Optional.of(selectedSnapshot("minecraft:emerald", 3, null))));

        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.MISSING_LEDGER_PARTICIPATION, refused.reasonCode());
    }

    @Test
    void missingLedgerAuthorityIdentityRefusesHonestlyBeforeCoreSubmission() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(Optional.of(listingWithMissingLedgerAuthority()), Optional.empty()));

        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.MISSING_LEDGER_AUTHORITY, refused.reasonCode());
    }

    @Test
    void invalidLedgerAmountRefusesHonestlyBeforeCoreSubmission() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(Optional.of(listingWithInvalidLedgerAmount()), Optional.empty()));

        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.INVALID_LEDGER_AMOUNT, refused.reasonCode());
    }

    @Test
    void unsupportedLedgerUnitRefusesHonestlyBeforeCoreSubmission() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(Optional.of(listingWithUnsupportedLedgerUnit()), Optional.empty()));

        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.UNSUPPORTED_LEDGER_UNIT, refused.reasonCode());
    }

    @Test
    void missingAndDisabledListingRefuseHonestlyBeforeCoreSubmission() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused missing =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(Optional.empty(), Optional.empty()));
        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused disabled =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(Optional.of(disabledInventoryForLedgerListing()), Optional.empty()));

        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.MISSING_LISTING, missing.reasonCode());
        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.LISTING_UNAVAILABLE, disabled.reasonCode());
    }

    @Test
    void playerSelectedInventoryMismatchRefusesHonestlyWhenInventoryIsCounterOffer() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused refused =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Refused.class,
                        construction.prepare(
                                Optional.of(ledgerForInventoryListing()),
                                Optional.of(selectedSnapshot("minecraft:diamond", 3, "{custom:1b}"))));

        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.PLAYER_SELECTED_VALUE_MISMATCH, refused.reasonCode());
    }

    @Test
    void constructionPreservesInventoryLedgerAndOptionalListingLocalValuationIdentityWithoutImplicitExchangeRate() {
        CofferMinecraftAdminShopMixedLedgerListingConstruction construction =
                new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin"));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Ready ready =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ConstructionResult.Ready.class,
                        construction.prepare(Optional.of(ledgerForInventoryListing()), Optional.of(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}"))));

        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm offeredLedger =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm.class,
                        ready.exchange().offeredTerm());
        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm acceptedInventory =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm.class,
                        ready.exchange().acceptedTerm());
        CofferMinecraftAdminShopMixedLedgerListingConstruction.ListingValuationParticipation valuation =
                ready.exchange().listingValuationParticipation().orElseThrow();

        assertEquals("proof-ledger-authority", offeredLedger.value().authorityId());
        assertEquals("spawn-ledger", offeredLedger.value().ledgerId());
        assertEquals("shop:spawn:treasury", offeredLedger.value().accountId());
        assertEquals("proof:coin", offeredLedger.value().unitId());
        assertEquals(125L, offeredLedger.value().amount());
        assertEquals("minecraft:emerald", acceptedInventory.value().itemId());
        assertEquals(3L, acceptedInventory.value().quantity());
        assertEquals("{custom:1b}", acceptedInventory.value().nbtPayload().orElseThrow());
        assertEquals("spawn-shop-valuation-a", valuation.valuationId());
        assertEquals("listing:spawn-shop:valuation-surface-a", valuation.valuationSurfaceId());
    }

    @Test
    void constructionSurfaceDoesNotSubmitEnterRuntimeMutateOrProjectReceipts() {
        List<String> methodNames =
                Arrays.stream(CofferMinecraftAdminShopMixedLedgerListingConstruction.class.getDeclaredMethods())
                        .map(Method::getName)
                        .map(String::toLowerCase)
                        .toList();

        assertTrue(methodNames.contains("prepare"));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("submit")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("assemble")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("runtime")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("mutat")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("receipt")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("project")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("core")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("rate")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("price")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("convert")));
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing inventoryForLedgerListing() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-diamond-for-ledger",
                true,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:diamond", 1, "{Damage:2}")),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("proof-ledger-authority", "spawn-ledger", "player:" + PLAYER_ID + ":wallet", "proof:coin", 125L)),
                Optional.empty());
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing disabledInventoryForLedgerListing() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-disabled-diamond-for-ledger",
                false,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:diamond", 1, null)),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("proof-ledger-authority", "spawn-ledger", "player:" + PLAYER_ID + ":wallet", "proof:coin", 125L)),
                Optional.empty());
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing ledgerForInventoryListing() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-ledger-for-emerald",
                true,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("proof-ledger-authority", "spawn-ledger", "shop:spawn:treasury", "proof:coin", 125L)),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:emerald", 3, "{custom:1b}")),
                Optional.of(new CofferMinecraftAdminShopMixedLedgerListingConstruction.ListingValuationParticipation(
                        "spawn-shop-valuation-a",
                        "listing:spawn-shop:valuation-surface-a")));
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing invalidInventoryOnlyListing() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-inventory-only",
                true,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:diamond", 1, null)),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:emerald", 3, null)),
                Optional.empty());
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing listingWithMissingLedgerAuthority() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-missing-ledger-authority",
                true,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:diamond", 1, null)),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("", "spawn-ledger", "player:" + PLAYER_ID + ":wallet", "proof:coin", 125L)),
                Optional.empty());
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing listingWithInvalidLedgerAmount() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-invalid-ledger-amount",
                true,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:diamond", 1, null)),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("proof-ledger-authority", "spawn-ledger", "player:" + PLAYER_ID + ":wallet", "proof:coin", 0L)),
                Optional.empty());
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing listingWithUnsupportedLedgerUnit() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-unsupported-ledger-unit",
                true,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:diamond", 1, null)),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("proof-ledger-authority", "spawn-ledger", "player:" + PLAYER_ID + ":wallet", "proof:gem", 125L)),
                Optional.empty());
    }

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.LedgerParticipation ledger(
            String authorityId,
            String ledgerId,
            String accountId,
            String unitId,
            long amount) {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.LedgerParticipation(
                authorityId,
                ledgerId,
                accountId,
                unitId,
                amount);
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
                        4),
                Optional.of(descriptor(itemId, quantity, nbtPayload)));
    }

    private static MinecraftItemDescriptor descriptor(String itemId, long quantity, String nbtPayload) {
        return new MinecraftItemDescriptor(itemId, quantity, Optional.ofNullable(nbtPayload));
    }
}

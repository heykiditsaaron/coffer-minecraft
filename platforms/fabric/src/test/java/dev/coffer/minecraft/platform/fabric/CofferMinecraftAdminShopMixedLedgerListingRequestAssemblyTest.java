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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.request.AuthorityRequirement;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.junit.jupiter.api.Test;

class CofferMinecraftAdminShopMixedLedgerListingRequestAssemblyTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000001231");

    @Test
    void inventoryOfferedAndLedgerAcceptedListingAssemblesCoreFacingExchangePayload() {
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly assembly =
                new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
                        new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin")));

        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(inventoryForLedgerListing()),
                                playerParticipant(selectedSnapshot("minecraft:stick", 1, null)),
                                "minecraft-inventory"));

        ExchangePayload payload = prepared.payload();
        assertEquals("admin-shop-mixed-ledger-payload-1", payload.payloadId().value());
        assertEquals(2, payload.actors().size());
        assertEquals(2, payload.offers().size());
        assertEquals(2, payload.authorityRequirements().size());
        assertTrue(payload.mutationRequirements().isEmpty());

        ValueDeclaration playerValue = payload.offers().get(0).values().get(0);
        ValueDeclaration shopValue = payload.offers().get(1).values().get(0);
        assertEquals(CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID, playerValue.authority());
        assertEquals(TransferableValueCoreAuthority.AUTHORITY_ID, shopValue.authority());
        assertEquals("spawn-ledger", playerValue.descriptor().values().get("ledgerId"));
        assertEquals("proof:coin", playerValue.descriptor().values().get("unitId"));
        assertEquals(125L, ((Number) playerValue.descriptor().values().get("amount")).longValue());
        assertEquals("minecraft:diamond", shopValue.descriptor().values().get("itemId"));
        assertEquals(1L, ((Number) shopValue.descriptor().values().get("quantity")).longValue());
    }

    @Test
    void ledgerOfferedAndInventoryAcceptedListingAssemblesCoreFacingExchangePayload() {
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly assembly =
                new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
                        new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin")));

        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(ledgerForInventoryListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}")),
                                "minecraft-inventory"));

        ExchangePayload payload = prepared.payload();
        assertEquals(2, payload.offers().size());
        assertTrue(payload.offers().get(0).values().stream()
                .allMatch(value -> TransferableValueCoreAuthority.AUTHORITY_ID.equals(value.authority())));
        assertTrue(payload.offers().get(1).values().stream()
                .allMatch(value -> CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID.equals(value.authority())));
        assertEquals("minecraft:emerald",
                payload.offers().get(0).values().get(0).descriptor().values().get("itemId"));
        assertEquals("shop:spawn:treasury",
                payload.offers().get(1).values().get(0).descriptor().values().get("accountId"));
    }

    @Test
    void missingDisabledAndMismatchedConstructionRefusalRemainPreCore() {
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly assembly =
                new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
                        new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin")));

        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Refused missing =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                Optional.empty(),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}")),
                                "minecraft-inventory"));
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Refused disabled =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                Optional.of(disabledLedgerForInventoryListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}")),
                                "minecraft-inventory"));
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Refused mismatched =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Refused.class,
                        assembly.assemble(
                                Optional.of(ledgerForInventoryListing()),
                                playerParticipant(selectedSnapshot("minecraft:diamond", 3, "{custom:1b}")),
                                "minecraft-inventory"));

        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.MISSING_LISTING, missing.reasonCode());
        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.LISTING_UNAVAILABLE, disabled.reasonCode());
        assertEquals(CofferMinecraftAdminShopMixedLedgerListingConstruction.PLAYER_SELECTED_VALUE_MISMATCH, mismatched.reasonCode());
    }

    @Test
    void inventoryAndLedgerTruthRequirementsCoexistWithoutSemanticConflict() {
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly assembly =
                new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
                        new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin")));

        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(ledgerForInventoryListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}")),
                                "minecraft-inventory"));

        Map<String, List<AuthorityRequirement>> requirementsByAuthority = prepared.payload().authorityRequirements().stream()
                .collect(Collectors.groupingBy(requirement -> requirement.authority().value()));

        assertEquals(Set.of(
                        TransferableValueCoreAuthority.AUTHORITY_ID.value(),
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID.value()),
                requirementsByAuthority.keySet());
        assertEquals(2, requirementsByAuthority.get(TransferableValueCoreAuthority.AUTHORITY_ID.value())
                .get(0).requiredTruths().size());
        assertEquals(1, requirementsByAuthority.get(CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID.value())
                .get(0).requiredTruths().size());
        assertEquals(
                TransferableValueCoreAuthority.REMOVABLY_OWNED,
                requirementsByAuthority.get(TransferableValueCoreAuthority.AUTHORITY_ID.value())
                        .get(0).requiredTruths().get(0).requirement().values().get("type"));
        assertEquals(
                CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_CAN_DEBIT,
                requirementsByAuthority.get(CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.TEST_LEDGER_AUTHORITY_ID.value())
                        .get(0).requiredTruths().get(0).requirement().values().get("type"));
    }

    @Test
    void listingLocalValuationIdentityRemainsDeclaredContextWithoutImplicitPricingLogic() {
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly assembly =
                new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
                        new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin")));

        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(ledgerForInventoryListing()),
                                playerParticipant(selectedSnapshot("minecraft:emerald", 3, "{custom:1b}")),
                                "minecraft-inventory"));

        Map<String, Object> shopMetadata = prepared.payload().actors().stream()
                .filter(actor -> actor.actorRef().value().startsWith("admin-shop:listing:"))
                .findFirst()
                .orElseThrow()
                .descriptor()
                .values();

        assertEquals("listing-ledger-for-emerald", shopMetadata.get("shopListingId"));
        assertEquals("spawn-shop-valuation-a", shopMetadata.get("shopListingValuationId"));
        assertEquals("listing:spawn-shop:valuation-surface-a", shopMetadata.get("shopListingValuationSurfaceId"));

        assertFalse(prepared.payload().offers().stream()
                .flatMap(offer -> offer.values().stream())
                .flatMap(value -> value.descriptor().values().keySet().stream())
                .anyMatch(key -> key.toLowerCase().contains("price")
                        || key.toLowerCase().contains("rate")
                        || key.toLowerCase().contains("valuation")));
        assertFalse(prepared.payload().authorityRequirements().stream()
                .flatMap(requirement -> requirement.requiredTruths().stream())
                .flatMap(truth -> truth.requirement().values().keySet().stream())
                .anyMatch(key -> key.toLowerCase().contains("price")
                        || key.toLowerCase().contains("rate")
                        || key.toLowerCase().contains("valuation")));
    }

    @Test
    void assemblySurfaceDoesNotAddCoreSubmissionRuntimeMutationOrReceiptProjection() {
        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly assembly =
                new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly(
                        new CofferMinecraftAdminShopMixedLedgerListingConstruction(Set.of("proof:coin")));

        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared prepared =
                assertInstanceOf(
                        CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.AssemblyResult.Prepared.class,
                        assembly.assemble(
                                Optional.of(inventoryForLedgerListing()),
                                playerParticipant(selectedSnapshot("minecraft:stick", 1, null)),
                                "minecraft-inventory"));

        List<String> methodNames = Arrays.stream(CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        assertTrue(methodNames.contains("assemble"));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("submit")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("runtime")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("receipt")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("execut")));
        assertTrue(prepared.payload().mutationRequirements().isEmpty());
    }

    private static CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.SelectedParticipant playerParticipant(
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot) {
        return new CofferMinecraftAdminShopMixedLedgerListingRequestAssembly.SelectedParticipant(
                new ActorRef("player:" + PLAYER_ID + ":inventory:hotbar"),
                new OfferRef("player-mixed-offer"),
                snapshot);
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

    private static CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing disabledLedgerForInventoryListing() {
        return new CofferMinecraftAdminShopMixedLedgerListingConstruction.MixedListing(
                "listing-disabled-ledger-for-emerald",
                false,
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.LedgerTerm(
                        ledger("proof-ledger-authority", "spawn-ledger", "shop:spawn:treasury", "proof:coin", 125L)),
                new CofferMinecraftAdminShopMixedLedgerListingConstruction.ExchangeTerm.InventoryTerm(
                        descriptor("minecraft:emerald", 3, "{custom:1b}")),
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
                        1),
                Optional.of(descriptor(itemId, quantity, nbtPayload)));
    }

    private static MinecraftItemDescriptor descriptor(String itemId, long quantity, String nbtPayload) {
        return new MinecraftItemDescriptor(itemId, quantity, Optional.ofNullable(nbtPayload));
    }
}

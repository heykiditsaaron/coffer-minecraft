package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CofferMinecraftAdminShopPresetListingReceiptProjectionTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;

    @Test
    void coreDenialReceiptRemainsDeniedAndFiniteSupplySemanticsStayReconstructable() {
        CofferMinecraftAdminShopPresetListingReceiptProjection projection =
                new CofferMinecraftAdminShopPresetListingReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "admin-shop-receipt-1", () -> TIMESTAMP);

        CofferMinecraftAdminShopPresetListingReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                List.of(accountability.toJsonLine(
                                        TIMESTAMP,
                                        "admin-shop-receipt-1",
                                        "CER",
                                        "fabric_core_denied",
                                        "fabric_core",
                                        "minecraft.shop.value.not_available")),
                                Optional.of(finiteContext())))
                        .orElseThrow();
        CofferMinecraftAdminShopPresetListingReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                List.of(accountability.toJsonLine(
                                        TIMESTAMP,
                                        "admin-shop-receipt-1",
                                        "CER",
                                        "fabric_core_denied",
                                        "fabric_core",
                                        "minecraft.shop.value.not_available")),
                                Optional.of(finiteContext())))
                        .orElseThrow();

        assertEquals("DENIED", participant.status());
        assertEquals("REVIEW_REQUIRED", participant.actionability());
        assertEquals("minecraft.shop.value.not_available", participant.code());
        assertEquals("finite_faucet", participant.supplyMode());
        assertEquals("DENIED", admin.status());
        assertEquals("listing-finite-emerald-for-diamond", admin.listingId());
        assertEquals("finite_faucet", admin.supplyMode());
    }

    @Test
    void runtimeSuccessReceiptPreservesInfiniteSupplyWithoutImplyingFiniteDepletion() {
        CofferMinecraftAdminShopPresetListingReceiptProjection projection =
                new CofferMinecraftAdminShopPresetListingReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "admin-shop-receipt-2", () -> TIMESTAMP);
        List<String> lines = List.of(
                accountability.toJsonLine(TIMESTAMP, "admin-shop-receipt-2", "CER", "fabric_core_approved", "fabric_core", null),
                accountability.toJsonLine(TIMESTAMP, "admin-shop-receipt-2", "CER", "fabric_runtime_succeeded", "fabric_runtime", null));

        CofferMinecraftAdminShopPresetListingReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                lines,
                                Optional.of(infiniteContext())))
                        .orElseThrow();
        CofferMinecraftAdminShopPresetListingReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                lines,
                                Optional.of(infiniteContext())))
                        .orElseThrow();

        assertEquals("COMPLETED", participant.status());
        assertEquals("NONE", participant.actionability());
        assertEquals("infinite_faucet", participant.supplyMode());
        assertEquals("COMPLETED", admin.status());
        assertEquals("infinite_faucet", admin.supplyMode());
        assertEquals(List.of("fabric_core_approved", "fabric_runtime_succeeded"), admin.stages());
        assertFalse("finite_faucet".equals(admin.supplyMode()));
    }

    @Test
    void runtimeFailureReceiptRemainsDistinctAndPreservesFiniteSupplySemantics() {
        CofferMinecraftAdminShopPresetListingReceiptProjection projection =
                new CofferMinecraftAdminShopPresetListingReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "admin-shop-receipt-3", () -> TIMESTAMP);
        List<String> lines = List.of(
                accountability.toJsonLine(TIMESTAMP, "admin-shop-receipt-3", "CER", "fabric_core_approved", "fabric_core", null),
                accountability.toJsonLine(
                        TIMESTAMP,
                        "admin-shop-receipt-3",
                        "CER",
                        "fabric_runtime_failed",
                        "fabric_runtime",
                        "minecraft.shop.value.not_available"));

        CofferMinecraftAdminShopPresetListingReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                lines,
                                Optional.of(finiteContext())))
                        .orElseThrow();
        CofferMinecraftAdminShopPresetListingReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                lines,
                                Optional.of(finiteContext())))
                        .orElseThrow();

        assertEquals("FAILED", participant.status());
        assertEquals("minecraft.shop.value.not_available", participant.code());
        assertEquals("finite_faucet", participant.supplyMode());
        assertEquals("FAILED", admin.status());
        assertEquals("fabric_runtime_failed", admin.latestStage());
        assertEquals("listing-finite-emerald-for-diamond", admin.listingId());
    }

    @Test
    void runtimeUnknownReceiptRemainsUnknown() {
        CofferMinecraftAdminShopPresetListingReceiptProjection projection =
                new CofferMinecraftAdminShopPresetListingReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "admin-shop-receipt-4", () -> TIMESTAMP);
        List<String> lines = List.of(
                accountability.toJsonLine(TIMESTAMP, "admin-shop-receipt-4", "CER", "fabric_core_approved", "fabric_core", null),
                accountability.toJsonLine(
                        TIMESTAMP,
                        "admin-shop-receipt-4",
                        "CER",
                        "fabric_runtime_unknown",
                        "fabric_runtime",
                        "minecraft.container.unavailable"));

        CofferMinecraftAdminShopPresetListingReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                lines,
                                Optional.of(finiteContext())))
                        .orElseThrow();
        CofferMinecraftAdminShopPresetListingReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                lines,
                                Optional.of(finiteContext())))
                        .orElseThrow();

        assertEquals("UNKNOWN", participant.status());
        assertEquals("minecraft.container.unavailable", participant.code());
        assertEquals("UNKNOWN", admin.status());
        assertEquals("fabric_runtime_unknown", admin.latestStage());
    }

    @Test
    void incompleteOrInterruptedListingInteractionRemainsTemporaryVisibilityOnly() {
        CofferMinecraftAdminShopPresetListingReceiptProjection projection =
                new CofferMinecraftAdminShopPresetListingReceiptProjection();

        CofferMinecraftAdminShopPresetListingReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.TemporaryStateVisibility(
                                "INTERRUPTED",
                                "REVIEW_REQUIRED",
                                "pending_listing_exchange_interrupted",
                                "SERVER_STOPPED",
                                Optional.of(infiniteContext())))
                        .orElseThrow();
        CofferMinecraftAdminShopPresetListingReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.TemporaryStateVisibility(
                                "INTERRUPTED",
                                "REVIEW_REQUIRED",
                                "pending_listing_exchange_interrupted",
                                "SERVER_STOPPED",
                                Optional.of(infiniteContext())))
                        .orElseThrow();

        assertEquals("INTERRUPTED", participant.status());
        assertTrue(participant.temporary());
        assertEquals("infinite_faucet", participant.supplyMode());
        assertEquals("INTERRUPTED", admin.status());
        assertTrue(admin.temporary());
        assertEquals(List.of("pending_listing_exchange_interrupted"), admin.stages());
        assertEquals("listing-emerald-for-diamond", admin.listingId());
    }

    @Test
    void staleInvalidatedConfirmedListingInteractionRemainsTemporaryVisibilityOnly() {
        CofferMinecraftAdminShopPresetListingReceiptProjection projection =
                new CofferMinecraftAdminShopPresetListingReceiptProjection();

        CofferMinecraftAdminShopPresetListingReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.TemporaryStateVisibility(
                                "STALE",
                                "RECONFIRM_REQUIRED",
                                "confirmed_listing_exchange_invalidated",
                                CofferMinecraftAdminShopPresetListingConfirmation.PLAYER_NOT_CONFIRMED,
                                Optional.of(finiteContext())))
                        .orElseThrow();
        CofferMinecraftAdminShopPresetListingReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.TemporaryStateVisibility(
                                "STALE",
                                "RECONFIRM_REQUIRED",
                                "confirmed_listing_exchange_invalidated",
                                CofferMinecraftAdminShopPresetListingConfirmation.PLAYER_NOT_CONFIRMED,
                                Optional.of(finiteContext())))
                        .orElseThrow();

        assertEquals("STALE", participant.status());
        assertTrue(participant.temporary());
        assertEquals("finite_faucet", participant.supplyMode());
        assertEquals("STALE", admin.status());
        assertEquals("confirmed_listing_exchange_invalidated", admin.latestStage());
        assertEquals("finite_faucet", admin.supplyMode());
    }

    @Test
    void emptyTrailDoesNotEmitReceiptByDefault() {
        CofferMinecraftAdminShopPresetListingReceiptProjection projection =
                new CofferMinecraftAdminShopPresetListingReceiptProjection();

        assertTrue(projection.participantReceipt(
                        new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                List.of(),
                                Optional.of(infiniteContext())))
                .isEmpty());
        assertTrue(projection.adminReceipt(
                        new CofferMinecraftAdminShopPresetListingReceiptProjection.ReceiptSource.AccountabilityTrail(
                                List.of(),
                                Optional.of(infiniteContext())))
                .isEmpty());
    }

    private static CofferMinecraftAdminShopPresetListingReceiptProjection.ListingContext infiniteContext() {
        return new CofferMinecraftAdminShopPresetListingReceiptProjection.ListingContext(
                "listing-emerald-for-diamond",
                CofferMinecraftAdminShopPresetListingRequestAssembly.INFINITE_FAUCET);
    }

    private static CofferMinecraftAdminShopPresetListingReceiptProjection.ListingContext finiteContext() {
        return new CofferMinecraftAdminShopPresetListingReceiptProjection.ListingContext(
                "listing-finite-emerald-for-diamond",
                CofferMinecraftAdminShopPresetListingRequestAssembly.FINITE_FAUCET);
    }
}

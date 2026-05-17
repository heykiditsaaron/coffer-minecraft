package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CofferMinecraftSelectedExchangeReceiptProjectionTest {
    private static final long TIMESTAMP = 1_700_000_000_000L;

    @Test
    void coreDenialDerivesBoundedParticipantAndAdminReceipts() {
        CofferMinecraftSelectedExchangeReceiptProjection projection = new CofferMinecraftSelectedExchangeReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "receipt-1", () -> TIMESTAMP);

        CofferMinecraftSelectedExchangeReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(
                                List.of(accountability.toJsonLine(
                                        TIMESTAMP,
                                        "receipt-1",
                                        "CER",
                                        "fabric_core_denied",
                                        "fabric_core",
                                        "minecraft.value.not_removable"))))
                        .orElseThrow();
        CofferMinecraftSelectedExchangeReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(
                                List.of(accountability.toJsonLine(
                                        TIMESTAMP,
                                        "receipt-1",
                                        "CER",
                                        "fabric_core_denied",
                                        "fabric_core",
                                        "minecraft.value.not_removable"))))
                        .orElseThrow();

        assertEquals("DENIED", participant.status());
        assertFalse(participant.temporary());
        assertEquals("REVIEW_REQUIRED", participant.actionability());
        assertEquals("minecraft.value.not_removable", participant.code());
        assertEquals("DENIED", admin.status());
        assertEquals("fabric_core_denied", admin.latestStage());
        assertEquals(List.of("fabric_core_denied"), admin.stages());
    }

    @Test
    void runtimeSuccessDerivesCompletionWithoutInflatingAdminDetail() {
        CofferMinecraftSelectedExchangeReceiptProjection projection = new CofferMinecraftSelectedExchangeReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "receipt-2", () -> TIMESTAMP);

        List<String> lines = List.of(
                accountability.toJsonLine(TIMESTAMP, "receipt-2", "CER", "fabric_core_approved", "fabric_core", null),
                accountability.toJsonLine(TIMESTAMP, "receipt-2", "CER", "fabric_runtime_succeeded", "fabric_runtime", null));

        CofferMinecraftSelectedExchangeReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(lines))
                        .orElseThrow();
        CofferMinecraftSelectedExchangeReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(lines))
                        .orElseThrow();

        assertEquals("COMPLETED", participant.status());
        assertFalse(participant.temporary());
        assertEquals("NONE", participant.actionability());
        assertEquals("COMPLETED", admin.status());
        assertEquals("fabric_runtime_succeeded", admin.latestStage());
        assertEquals(List.of("fabric_core_approved", "fabric_runtime_succeeded"), admin.stages());
    }

    @Test
    void runtimeFailureRemainsDistinctFromDenialAndCompletion() {
        CofferMinecraftSelectedExchangeReceiptProjection projection = new CofferMinecraftSelectedExchangeReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "receipt-3", () -> TIMESTAMP);

        List<String> lines = List.of(
                accountability.toJsonLine(TIMESTAMP, "receipt-3", "CER", "fabric_core_approved", "fabric_core", null),
                accountability.toJsonLine(TIMESTAMP, "receipt-3", "CER", "fabric_runtime_failed", "fabric_runtime", "minecraft.value.not_removable"));

        CofferMinecraftSelectedExchangeReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(lines))
                        .orElseThrow();
        CofferMinecraftSelectedExchangeReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(lines))
                        .orElseThrow();

        assertEquals("FAILED", participant.status());
        assertEquals("REVIEW_REQUIRED", participant.actionability());
        assertEquals("minecraft.value.not_removable", participant.code());
        assertEquals("FAILED", admin.status());
        assertEquals("fabric_runtime_failed", admin.latestStage());
        assertFalse(admin.stages().contains("fabric_core_denied"));
    }

    @Test
    void runtimeUnknownRemainsUnknownAndDoesNotCounterfeitCompletion() {
        CofferMinecraftSelectedExchangeReceiptProjection projection = new CofferMinecraftSelectedExchangeReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "receipt-4", () -> TIMESTAMP);

        List<String> lines = List.of(
                accountability.toJsonLine(TIMESTAMP, "receipt-4", "CER", "fabric_core_approved", "fabric_core", null),
                accountability.toJsonLine(TIMESTAMP, "receipt-4", "CER", "fabric_runtime_unknown", "fabric_runtime", "minecraft.container.unavailable"));

        CofferMinecraftSelectedExchangeReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(lines))
                        .orElseThrow();
        CofferMinecraftSelectedExchangeReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(lines))
                        .orElseThrow();

        assertEquals("UNKNOWN", participant.status());
        assertEquals("REVIEW_REQUIRED", participant.actionability());
        assertEquals("minecraft.container.unavailable", participant.code());
        assertEquals("UNKNOWN", admin.status());
        assertEquals("fabric_runtime_unknown", admin.latestStage());
    }

    @Test
    void interruptedIncompleteExchangeUsesExplicitTemporaryVisibilityOnly() {
        CofferMinecraftSelectedExchangeReceiptProjection projection = new CofferMinecraftSelectedExchangeReceiptProjection();

        CofferMinecraftSelectedExchangeReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.TemporaryStateVisibility(
                                "INTERRUPTED",
                                "REVIEW_REQUIRED",
                                "pending_exchange_interrupted",
                                "SERVER_STOPPED"))
                        .orElseThrow();
        CofferMinecraftSelectedExchangeReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.TemporaryStateVisibility(
                                "INTERRUPTED",
                                "REVIEW_REQUIRED",
                                "pending_exchange_interrupted",
                                "SERVER_STOPPED"))
                        .orElseThrow();

        assertEquals("INTERRUPTED", participant.status());
        assertTrue(participant.temporary());
        assertEquals("REVIEW_REQUIRED", participant.actionability());
        assertEquals("SERVER_STOPPED", participant.code());
        assertEquals("INTERRUPTED", admin.status());
        assertTrue(admin.temporary());
        assertEquals(List.of("pending_exchange_interrupted"), admin.stages());
    }

    @Test
    void staleInvalidatedConfirmedExchangeUsesExplicitTemporaryVisibilityOnly() {
        CofferMinecraftSelectedExchangeReceiptProjection projection = new CofferMinecraftSelectedExchangeReceiptProjection();

        CofferMinecraftSelectedExchangeReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.TemporaryStateVisibility(
                                "STALE",
                                "RECONFIRM_REQUIRED",
                                "confirmed_exchange_invalidated",
                                "FIRST_PARTY_NOT_CONFIRMED"))
                        .orElseThrow();
        CofferMinecraftSelectedExchangeReceiptProjection.AdminReceipt admin =
                projection.adminReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.TemporaryStateVisibility(
                                "STALE",
                                "RECONFIRM_REQUIRED",
                                "confirmed_exchange_invalidated",
                                "FIRST_PARTY_NOT_CONFIRMED"))
                        .orElseThrow();

        assertEquals("STALE", participant.status());
        assertTrue(participant.temporary());
        assertEquals("RECONFIRM_REQUIRED", participant.actionability());
        assertEquals("FIRST_PARTY_NOT_CONFIRMED", participant.code());
        assertEquals("STALE", admin.status());
        assertTrue(admin.temporary());
        assertEquals("confirmed_exchange_invalidated", admin.latestStage());
    }

    @Test
    void mereSelectionOrIncompleteOfferFormationDoesNotEmitReceiptByDefault() {
        CofferMinecraftSelectedExchangeReceiptProjection projection = new CofferMinecraftSelectedExchangeReceiptProjection();

        assertTrue(projection.participantReceipt(
                        new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(List.of()))
                .isEmpty());
        assertTrue(projection.adminReceipt(
                        new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(List.of()))
                .isEmpty());
    }

    @Test
    void coreApprovalReceiptDoesNotBecomeCompletion() {
        CofferMinecraftSelectedExchangeReceiptProjection projection = new CofferMinecraftSelectedExchangeReceiptProjection();
        CofferMinecraftLifecycleAccountability accountability =
                new CofferMinecraftLifecycleAccountability(() -> "receipt-5", () -> TIMESTAMP);

        CofferMinecraftSelectedExchangeReceiptProjection.ParticipantReceipt participant =
                projection.participantReceipt(new CofferMinecraftSelectedExchangeReceiptProjection.ReceiptSource.AccountabilityTrail(
                                List.of(accountability.toJsonLine(
                                        TIMESTAMP,
                                        "receipt-5",
                                        "CER",
                                        "fabric_core_approved",
                                        "fabric_core",
                                        null))))
                        .orElseThrow();

        assertEquals("AUTHORIZED", participant.status());
        assertEquals("WAIT_RUNTIME", participant.actionability());
        assertFalse("COMPLETED".equals(participant.status()));
    }
}

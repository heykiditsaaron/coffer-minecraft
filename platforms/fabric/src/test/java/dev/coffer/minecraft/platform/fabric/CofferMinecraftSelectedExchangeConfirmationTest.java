package dev.coffer.minecraft.platform.fabric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coffer.minecraft.bindings.inventory.MinecraftPlayerInventoryContainer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.OfferRef;
import org.junit.jupiter.api.Test;

class CofferMinecraftSelectedExchangeConfirmationTest {
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000961");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000962");

    @Test
    void initiatorSelectedValueAloneIsNotEnough() {
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();

        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady notReady =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady.class,
                        confirmation.readiness(
                                state(
                                        participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                                        participant(SECOND_PLAYER_ID, "offer-second", 6)),
                                CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty()));

        assertEquals(
                CofferMinecraftSelectedExchangeConfirmation.SECOND_SELECTED_VALUE_NOT_MATERIALIZED,
                notReady.reasonCode());
    }

    @Test
    void recipientSelectedValueAloneIsNotEnough() {
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();

        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady notReady =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady.class,
                        confirmation.readiness(
                                state(
                                        participant(FIRST_PLAYER_ID, "offer-first", 2),
                                        participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null)),
                                CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty()));

        assertEquals(
                CofferMinecraftSelectedExchangeConfirmation.FIRST_SELECTED_VALUE_NOT_MATERIALIZED,
                notReady.reasonCode());
    }

    @Test
    void oneSidedConfirmationIsNotEnough() {
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));

        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.first().actorRef(), CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty());

        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady notReady =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady.class,
                        confirmation.readiness(state, ledger));

        assertEquals(CofferMinecraftSelectedExchangeConfirmation.SECOND_PARTY_NOT_CONFIRMED, notReady.reasonCode());
    }

    @Test
    void dualConfirmationOverSameStatePermitsSubmissionReadiness() {
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));

        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(state, state.first().actorRef(), CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty());
        ledger = confirmation.confirm(state, state.second().actorRef(), ledger);

        assertInstanceOf(CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.Ready.class, confirmation.readiness(state, ledger));
    }

    @Test
    void anySelectedStateChangeInvalidatesPriorConfirmation() {
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState originalState = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState changedState = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 4, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));

        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(originalState, originalState.first().actorRef(), CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty());
        ledger = confirmation.confirm(originalState, originalState.second().actorRef(), ledger);

        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady notReady =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady.class,
                        confirmation.readiness(changedState, ledger));

        assertEquals(CofferMinecraftSelectedExchangeConfirmation.FIRST_PARTY_NOT_CONFIRMED, notReady.reasonCode());
    }

    @Test
    void staleConfirmationCannotBeReusedAfterRecipientStateChange() {
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState originalState = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState changedState = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 1, null));

        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger ledger =
                confirmation.confirm(originalState, originalState.first().actorRef(), CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty());
        ledger = confirmation.confirm(originalState, originalState.second().actorRef(), ledger);

        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady notReady =
                assertInstanceOf(
                        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady.class,
                        confirmation.readiness(changedState, ledger));

        assertEquals(CofferMinecraftSelectedExchangeConfirmation.FIRST_PARTY_NOT_CONFIRMED, notReady.reasonCode());
        assertFalse(originalState.fingerprint().equals(changedState.fingerprint()));
    }

    @Test
    void outsiderCannotConfirmExchangeState() {
        CofferMinecraftSelectedExchangeConfirmation confirmation = new CofferMinecraftSelectedExchangeConfirmation();
        CofferMinecraftSelectedExchangeConfirmation.ExchangeState state = state(
                participant(FIRST_PLAYER_ID, "offer-first", 2, "minecraft:stone", 3, null),
                participant(SECOND_PLAYER_ID, "offer-second", 6, "minecraft:dirt", 2, null));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> confirmation.confirm(
                        state,
                        new ActorRef("player:00000000-0000-0000-0000-000000000999:inventory:hotbar"),
                        CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger.empty()));

        assertTrue(error.getMessage().contains("confirmer"));
    }

    @Test
    void confirmationSurfaceDoesNotExposeRuntimeMutationOrSubmission() {
        java.util.List<String> methodNames = Arrays.stream(CofferMinecraftSelectedExchangeConfirmation.class.getDeclaredMethods())
                .map(Method::getName)
                .map(String::toLowerCase)
                .toList();

        assertTrue(methodNames.contains("confirm"));
        assertTrue(methodNames.contains("readiness"));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("runtime")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("mutat")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("submit")));
        assertFalse(methodNames.stream().anyMatch(name -> name.contains("execute")));
    }

    private static CofferMinecraftSelectedExchangeConfirmation.ExchangeState state(
            CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant first,
            CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant second) {
        return new CofferMinecraftSelectedExchangeConfirmation.ExchangeState(first, second, "minecraft-inventory");
    }

    private static CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant participant(
            UUID playerId,
            String offerRef,
            int slotIndex,
            String itemId,
            int quantity,
            String nbtPayload) {
        return new CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant(
                new ActorRef("player:" + playerId + ":inventory:hotbar"),
                new OfferRef(offerRef),
                new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                        playerId,
                        new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                                "main_hand_hotbar",
                                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                                slotIndex),
                        Optional.of(new dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor(
                                itemId,
                                quantity,
                                Optional.ofNullable(nbtPayload)))));
    }

    private static CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant participant(
            UUID playerId,
            String offerRef,
            int slotIndex) {
        return new CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant(
                new ActorRef("player:" + playerId + ":inventory:hotbar"),
                new OfferRef(offerRef),
                new CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot(
                        playerId,
                        new CofferMinecraftSelectedInventoryCapture.SelectedValueBoundary(
                                "main_hand_hotbar",
                                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                                slotIndex),
                        Optional.empty()));
    }
}

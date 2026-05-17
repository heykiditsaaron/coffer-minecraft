package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.coffer.core.model.id.ActorRef;

final class CofferMinecraftSelectedExchangeConfirmation {
    static final String FIRST_SELECTED_VALUE_NOT_MATERIALIZED = "FIRST_SELECTED_VALUE_NOT_MATERIALIZED";
    static final String SECOND_SELECTED_VALUE_NOT_MATERIALIZED = "SECOND_SELECTED_VALUE_NOT_MATERIALIZED";
    static final String FIRST_PARTY_NOT_CONFIRMED = "FIRST_PARTY_NOT_CONFIRMED";
    static final String SECOND_PARTY_NOT_CONFIRMED = "SECOND_PARTY_NOT_CONFIRMED";

    ConfirmationLedger confirm(ExchangeState state, ActorRef confirmer, ConfirmationLedger ledger) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(confirmer, "confirmer");
        Objects.requireNonNull(ledger, "ledger");

        if (!state.involves(confirmer)) {
            throw new IllegalArgumentException("confirmer must belong to the exchange state");
        }

        Map<String, String> confirmations = new LinkedHashMap<>(ledger.confirmationsByActorRef());
        confirmations.put(confirmer.value(), state.fingerprint());
        return new ConfirmationLedger(Map.copyOf(confirmations));
    }

    SubmissionReadiness readiness(ExchangeState state, ConfirmationLedger ledger) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(ledger, "ledger");

        if (state.first().snapshot().selectedValue().isEmpty()) {
            return new SubmissionReadiness.NotReady(FIRST_SELECTED_VALUE_NOT_MATERIALIZED);
        }
        if (state.second().snapshot().selectedValue().isEmpty()) {
            return new SubmissionReadiness.NotReady(SECOND_SELECTED_VALUE_NOT_MATERIALIZED);
        }

        String fingerprint = state.fingerprint();
        if (!fingerprint.equals(ledger.confirmationsByActorRef().get(state.first().actorRef().value()))) {
            return new SubmissionReadiness.NotReady(FIRST_PARTY_NOT_CONFIRMED);
        }
        if (!fingerprint.equals(ledger.confirmationsByActorRef().get(state.second().actorRef().value()))) {
            return new SubmissionReadiness.NotReady(SECOND_PARTY_NOT_CONFIRMED);
        }
        return new SubmissionReadiness.Ready();
    }

    record ExchangeState(
            CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant first,
            CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant second,
            String bindingId) {
        ExchangeState {
            Objects.requireNonNull(first, "first");
            Objects.requireNonNull(second, "second");
            if (bindingId == null || bindingId.isBlank()) {
                throw new IllegalArgumentException("bindingId must not be null or blank");
            }
        }

        boolean involves(ActorRef actorRef) {
            return first.actorRef().equals(actorRef) || second.actorRef().equals(actorRef);
        }

        String fingerprint() {
            return fingerprint(first) + "|" + fingerprint(second) + "|binding:" + bindingId;
        }

        private static String fingerprint(CofferMinecraftSelectedExchangeRequestAssembly.SelectedParticipant participant) {
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot = participant.snapshot();
            return participant.actorRef().value()
                    + "|offer:" + participant.offerRef().value()
                    + "|selectionKind:" + snapshot.boundary().selectionKind()
                    + "|region:" + snapshot.boundary().region().name()
                    + "|slot:" + snapshot.boundary().slotIndex()
                    + "|value:" + fingerprint(snapshot.selectedValue());
        }

        private static String fingerprint(Optional<MinecraftItemDescriptor> descriptor) {
            if (descriptor.isEmpty()) {
                return "EMPTY";
            }
            MinecraftItemDescriptor value = descriptor.orElseThrow();
            return value.itemId()
                    + "#"
                    + value.quantity()
                    + "#"
                    + value.nbtPayload().orElse("");
        }
    }

    record ConfirmationLedger(Map<String, String> confirmationsByActorRef) {
        ConfirmationLedger {
            confirmationsByActorRef = Map.copyOf(Objects.requireNonNull(confirmationsByActorRef, "confirmationsByActorRef"));
        }

        static ConfirmationLedger empty() {
            return new ConfirmationLedger(Map.of());
        }
    }

    sealed interface SubmissionReadiness permits SubmissionReadiness.Ready, SubmissionReadiness.NotReady {
        record Ready() implements SubmissionReadiness {
        }

        record NotReady(String reasonCode) implements SubmissionReadiness {
            public NotReady {
                if (reasonCode == null || reasonCode.isBlank()) {
                    throw new IllegalArgumentException("reasonCode must not be null or blank");
                }
            }
        }
    }
}

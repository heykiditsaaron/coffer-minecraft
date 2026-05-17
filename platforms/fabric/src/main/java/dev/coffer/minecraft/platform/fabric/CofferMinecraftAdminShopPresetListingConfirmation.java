package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.coffer.core.model.id.ActorRef;

final class CofferMinecraftAdminShopPresetListingConfirmation {
    static final String PLAYER_SELECTED_VALUE_NOT_MATERIALIZED = "PLAYER_SELECTED_VALUE_NOT_MATERIALIZED";
    static final String PLAYER_NOT_CONFIRMED = "PLAYER_NOT_CONFIRMED";

    ConfirmationLedger confirm(ExchangeState state, ActorRef confirmer, ConfirmationLedger ledger) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(confirmer, "confirmer");
        Objects.requireNonNull(ledger, "ledger");

        if (!state.player().actorRef().equals(confirmer)) {
            throw new IllegalArgumentException("confirmer must belong to the player side of the exchange state");
        }

        Map<String, String> confirmations = new LinkedHashMap<>(ledger.confirmationsByActorRef());
        confirmations.put(confirmer.value(), state.fingerprint());
        return new ConfirmationLedger(Map.copyOf(confirmations));
    }

    SubmissionReadiness readiness(ExchangeState state, ConfirmationLedger ledger) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(ledger, "ledger");

        if (state.player().snapshot().selectedValue().isEmpty()) {
            return new SubmissionReadiness.NotReady(PLAYER_SELECTED_VALUE_NOT_MATERIALIZED);
        }

        String fingerprint = state.fingerprint();
        if (!fingerprint.equals(ledger.confirmationsByActorRef().get(state.player().actorRef().value()))) {
            return new SubmissionReadiness.NotReady(PLAYER_NOT_CONFIRMED);
        }

        return new SubmissionReadiness.Ready();
    }

    record ExchangeState(
            CofferMinecraftAdminShopPresetListingRequestAssembly.SelectedParticipant player,
            Optional<CofferMinecraftAdminShopPresetListingConstruction.PresetListing> listing,
            String bindingId) {
        ExchangeState {
            Objects.requireNonNull(player, "player");
            listing = Objects.requireNonNull(listing, "listing");
            if (bindingId == null || bindingId.isBlank()) {
                throw new IllegalArgumentException("bindingId must not be null or blank");
            }
        }

        String fingerprint() {
            return fingerprintPlayer(player)
                    + "|listing:" + fingerprintListing(listing)
                    + "|binding:" + bindingId;
        }

        private static String fingerprintPlayer(CofferMinecraftAdminShopPresetListingRequestAssembly.SelectedParticipant participant) {
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot snapshot = participant.snapshot();
            return participant.actorRef().value()
                    + "|offer:" + participant.offerRef().value()
                    + "|selectionKind:" + snapshot.boundary().selectionKind()
                    + "|region:" + snapshot.boundary().region().name()
                    + "|slot:" + snapshot.boundary().slotIndex()
                    + "|value:" + fingerprintDescriptor(snapshot.selectedValue());
        }

        private static String fingerprintListing(Optional<CofferMinecraftAdminShopPresetListingConstruction.PresetListing> listing) {
            if (listing.isEmpty()) {
                return "MISSING";
            }
            CofferMinecraftAdminShopPresetListingConstruction.PresetListing resolved = listing.orElseThrow();
            return resolved.listingId()
                    + "|enabled:" + resolved.enabled()
                    + "|supply:" + fingerprintSupplyMode(resolved.supplyMode())
                    + "|offered:" + fingerprintDescriptor(Optional.of(resolved.offeredValue()))
                    + "|accepted:" + fingerprintDescriptor(Optional.of(resolved.acceptedCounterOffer()));
        }

        private static String fingerprintSupplyMode(CofferMinecraftAdminShopPresetListingConstruction.SupplyMode supplyMode) {
            if (supplyMode instanceof CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.InfiniteFaucet infinite) {
                return "INFINITE#" + infinite.listingSurfaceId();
            }
            CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet finite =
                    (CofferMinecraftAdminShopPresetListingConstruction.SupplyMode.FiniteFaucet) supplyMode;
            return "FINITE#" + finite.supplyContainerId();
        }

        private static String fingerprintDescriptor(Optional<MinecraftItemDescriptor> descriptor) {
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

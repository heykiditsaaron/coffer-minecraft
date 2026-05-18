package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class CofferMinecraftAdminShopMixedLedgerListingConstruction {
    static final String MISSING_LISTING = "MISSING_LISTING";
    static final String LISTING_UNAVAILABLE = "LISTING_UNAVAILABLE";
    static final String MISSING_LEDGER_PARTICIPATION = "MISSING_LEDGER_PARTICIPATION";
    static final String MISSING_LEDGER_AUTHORITY = "MISSING_LEDGER_AUTHORITY";
    static final String INVALID_LEDGER_AMOUNT = "INVALID_LEDGER_AMOUNT";
    static final String UNSUPPORTED_LEDGER_UNIT = "UNSUPPORTED_LEDGER_UNIT";
    static final String PLAYER_SELECTED_VALUE_NOT_MATERIALIZED = "PLAYER_SELECTED_VALUE_NOT_MATERIALIZED";
    static final String PLAYER_SELECTED_VALUE_MISMATCH = "PLAYER_SELECTED_VALUE_MISMATCH";

    private final Set<String> supportedLedgerUnits;

    CofferMinecraftAdminShopMixedLedgerListingConstruction() {
        this(Set.of("proof:coin", "proof:credit"));
    }

    CofferMinecraftAdminShopMixedLedgerListingConstruction(Set<String> supportedLedgerUnits) {
        Objects.requireNonNull(supportedLedgerUnits, "supportedLedgerUnits");
        this.supportedLedgerUnits = Set.copyOf(supportedLedgerUnits);
    }

    ConstructionResult prepare(
            Optional<MixedListing> listing,
            Optional<CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot> playerSelectedCounterOffer) {
        listing = Objects.requireNonNull(listing, "listing");
        playerSelectedCounterOffer = Objects.requireNonNull(playerSelectedCounterOffer, "playerSelectedCounterOffer");

        if (listing.isEmpty()) {
            return new ConstructionResult.Refused(MISSING_LISTING);
        }

        MixedListing resolvedListing = listing.orElseThrow();
        if (!resolvedListing.enabled()) {
            return new ConstructionResult.Refused(LISTING_UNAVAILABLE);
        }

        if (!containsLedgerParticipation(resolvedListing.offeredTerm())
                && !containsLedgerParticipation(resolvedListing.acceptedTerm())) {
            return new ConstructionResult.Refused(MISSING_LEDGER_PARTICIPATION);
        }

        Optional<String> offeredLedgerRefusal = validateLedgerParticipation(resolvedListing.offeredTerm());
        if (offeredLedgerRefusal.isPresent()) {
            return new ConstructionResult.Refused(offeredLedgerRefusal.orElseThrow());
        }

        Optional<String> acceptedLedgerRefusal = validateLedgerParticipation(resolvedListing.acceptedTerm());
        if (acceptedLedgerRefusal.isPresent()) {
            return new ConstructionResult.Refused(acceptedLedgerRefusal.orElseThrow());
        }

        if (resolvedListing.acceptedTerm() instanceof ExchangeTerm.InventoryTerm acceptedInventory) {
            if (playerSelectedCounterOffer.isEmpty()
                    || playerSelectedCounterOffer.orElseThrow().selectedValue().isEmpty()) {
                return new ConstructionResult.Refused(PLAYER_SELECTED_VALUE_NOT_MATERIALIZED);
            }

            MinecraftItemDescriptor selectedValue =
                    playerSelectedCounterOffer.orElseThrow().selectedValue().orElseThrow();
            if (!matches(selectedValue, acceptedInventory.value())) {
                return new ConstructionResult.Refused(PLAYER_SELECTED_VALUE_MISMATCH);
            }
        }

        return new ConstructionResult.Ready(new ConcreteExchange(
                resolvedListing.listingId(),
                resolvedListing.offeredTerm(),
                resolvedListing.acceptedTerm(),
                resolvedListing.listingValuationParticipation(),
                playerSelectedCounterOffer));
    }

    private static boolean containsLedgerParticipation(ExchangeTerm term) {
        return term instanceof ExchangeTerm.LedgerTerm;
    }

    private Optional<String> validateLedgerParticipation(ExchangeTerm term) {
        if (!(term instanceof ExchangeTerm.LedgerTerm ledgerTerm)) {
            return Optional.empty();
        }

        LedgerParticipation ledger = ledgerTerm.value();
        if (ledger.authorityId() == null || ledger.authorityId().isBlank()) {
            return Optional.of(MISSING_LEDGER_AUTHORITY);
        }
        if (ledger.amount() <= 0) {
            return Optional.of(INVALID_LEDGER_AMOUNT);
        }
        if (!supportedLedgerUnits.contains(ledger.unitId())) {
            return Optional.of(UNSUPPORTED_LEDGER_UNIT);
        }
        return Optional.empty();
    }

    private static boolean matches(MinecraftItemDescriptor left, MinecraftItemDescriptor right) {
        return left.itemId().equals(right.itemId())
                && left.quantity() == right.quantity()
                && left.nbtPayload().equals(right.nbtPayload());
    }

    record MixedListing(
            String listingId,
            boolean enabled,
            ExchangeTerm offeredTerm,
            ExchangeTerm acceptedTerm,
            Optional<ListingValuationParticipation> listingValuationParticipation) {
        MixedListing {
            if (listingId == null || listingId.isBlank()) {
                throw new IllegalArgumentException("listingId must not be null or blank");
            }
            Objects.requireNonNull(offeredTerm, "offeredTerm");
            Objects.requireNonNull(acceptedTerm, "acceptedTerm");
            listingValuationParticipation =
                    Objects.requireNonNull(listingValuationParticipation, "listingValuationParticipation");
        }
    }

    sealed interface ExchangeTerm permits ExchangeTerm.InventoryTerm, ExchangeTerm.LedgerTerm {
        record InventoryTerm(MinecraftItemDescriptor value) implements ExchangeTerm {
            public InventoryTerm {
                Objects.requireNonNull(value, "value");
            }
        }

        record LedgerTerm(LedgerParticipation value) implements ExchangeTerm {
            public LedgerTerm {
                Objects.requireNonNull(value, "value");
            }
        }
    }

    record LedgerParticipation(String authorityId, String ledgerId, String accountId, String unitId, long amount) {
        LedgerParticipation {
            if (ledgerId == null || ledgerId.isBlank()) {
                throw new IllegalArgumentException("ledgerId must not be null or blank");
            }
            if (accountId == null || accountId.isBlank()) {
                throw new IllegalArgumentException("accountId must not be null or blank");
            }
            if (unitId == null || unitId.isBlank()) {
                throw new IllegalArgumentException("unitId must not be null or blank");
            }
        }
    }

    record ListingValuationParticipation(String valuationId, String valuationSurfaceId) {
        ListingValuationParticipation {
            if (valuationId == null || valuationId.isBlank()) {
                throw new IllegalArgumentException("valuationId must not be null or blank");
            }
            if (valuationSurfaceId == null || valuationSurfaceId.isBlank()) {
                throw new IllegalArgumentException("valuationSurfaceId must not be null or blank");
            }
        }
    }

    record ConcreteExchange(
            String listingId,
            ExchangeTerm offeredTerm,
            ExchangeTerm acceptedTerm,
            Optional<ListingValuationParticipation> listingValuationParticipation,
            Optional<CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot> playerSelectedCounterOffer) {
        ConcreteExchange {
            if (listingId == null || listingId.isBlank()) {
                throw new IllegalArgumentException("listingId must not be null or blank");
            }
            Objects.requireNonNull(offeredTerm, "offeredTerm");
            Objects.requireNonNull(acceptedTerm, "acceptedTerm");
            listingValuationParticipation =
                    Objects.requireNonNull(listingValuationParticipation, "listingValuationParticipation");
            playerSelectedCounterOffer =
                    Objects.requireNonNull(playerSelectedCounterOffer, "playerSelectedCounterOffer");
        }
    }

    sealed interface ConstructionResult permits ConstructionResult.Ready, ConstructionResult.Refused {
        record Ready(ConcreteExchange exchange) implements ConstructionResult {
            public Ready {
                Objects.requireNonNull(exchange, "exchange");
            }
        }

        record Refused(String reasonCode) implements ConstructionResult {
            public Refused {
                if (reasonCode == null || reasonCode.isBlank()) {
                    throw new IllegalArgumentException("reasonCode must not be null or blank");
                }
            }
        }
    }
}

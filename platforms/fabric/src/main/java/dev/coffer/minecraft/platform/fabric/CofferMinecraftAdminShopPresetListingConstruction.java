package dev.coffer.minecraft.platform.fabric;

import dev.coffer.minecraft.bindings.inventory.MinecraftItemDescriptor;
import java.util.Objects;
import java.util.Optional;

final class CofferMinecraftAdminShopPresetListingConstruction {
    static final String MISSING_LISTING = "MISSING_LISTING";
    static final String LISTING_UNAVAILABLE = "LISTING_UNAVAILABLE";
    static final String PLAYER_SELECTED_VALUE_NOT_MATERIALIZED = "PLAYER_SELECTED_VALUE_NOT_MATERIALIZED";
    static final String PLAYER_SELECTED_VALUE_MISMATCH = "PLAYER_SELECTED_VALUE_MISMATCH";

    ConstructionResult prepare(
            Optional<PresetListing> listing,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot playerSelectedValue) {
        listing = Objects.requireNonNull(listing, "listing");
        Objects.requireNonNull(playerSelectedValue, "playerSelectedValue");

        if (listing.isEmpty()) {
            return new ConstructionResult.Refused(MISSING_LISTING);
        }

        PresetListing resolvedListing = listing.orElseThrow();
        if (!resolvedListing.enabled()) {
            return new ConstructionResult.Refused(LISTING_UNAVAILABLE);
        }

        if (playerSelectedValue.selectedValue().isEmpty()) {
            return new ConstructionResult.Refused(PLAYER_SELECTED_VALUE_NOT_MATERIALIZED);
        }

        MinecraftItemDescriptor selectedValue = playerSelectedValue.selectedValue().orElseThrow();
        if (!matches(selectedValue, resolvedListing.acceptedCounterOffer())) {
            return new ConstructionResult.Refused(PLAYER_SELECTED_VALUE_MISMATCH);
        }

        return new ConstructionResult.Ready(new ConcreteExchange(
                resolvedListing.listingId(),
                resolvedListing.supplyMode(),
                resolvedListing.offeredValue(),
                resolvedListing.acceptedCounterOffer(),
                playerSelectedValue));
    }

    private static boolean matches(MinecraftItemDescriptor left, MinecraftItemDescriptor right) {
        return left.itemId().equals(right.itemId())
                && left.quantity() == right.quantity()
                && left.nbtPayload().equals(right.nbtPayload());
    }

    record PresetListing(
            String listingId,
            boolean enabled,
            SupplyMode supplyMode,
            MinecraftItemDescriptor offeredValue,
            MinecraftItemDescriptor acceptedCounterOffer) {
        PresetListing {
            if (listingId == null || listingId.isBlank()) {
                throw new IllegalArgumentException("listingId must not be null or blank");
            }
            Objects.requireNonNull(supplyMode, "supplyMode");
            Objects.requireNonNull(offeredValue, "offeredValue");
            Objects.requireNonNull(acceptedCounterOffer, "acceptedCounterOffer");
        }
    }

    sealed interface SupplyMode permits SupplyMode.InfiniteFaucet, SupplyMode.FiniteFaucet {
        record InfiniteFaucet(String listingSurfaceId) implements SupplyMode {
            public InfiniteFaucet {
                if (listingSurfaceId == null || listingSurfaceId.isBlank()) {
                    throw new IllegalArgumentException("listingSurfaceId must not be null or blank");
                }
            }
        }

        record FiniteFaucet(String supplyContainerId) implements SupplyMode {
            public FiniteFaucet {
                if (supplyContainerId == null || supplyContainerId.isBlank()) {
                    throw new IllegalArgumentException("supplyContainerId must not be null or blank");
                }
            }
        }
    }

    record ConcreteExchange(
            String listingId,
            SupplyMode supplyMode,
            MinecraftItemDescriptor offeredValue,
            MinecraftItemDescriptor acceptedCounterOffer,
            CofferMinecraftSelectedInventoryCapture.SelectedInventorySnapshot playerSelectedValue) {
        ConcreteExchange {
            if (listingId == null || listingId.isBlank()) {
                throw new IllegalArgumentException("listingId must not be null or blank");
            }
            Objects.requireNonNull(supplyMode, "supplyMode");
            Objects.requireNonNull(offeredValue, "offeredValue");
            Objects.requireNonNull(acceptedCounterOffer, "acceptedCounterOffer");
            Objects.requireNonNull(playerSelectedValue, "playerSelectedValue");
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

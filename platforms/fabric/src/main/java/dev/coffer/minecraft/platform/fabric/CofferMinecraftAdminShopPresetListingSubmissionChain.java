package dev.coffer.minecraft.platform.fabric;

import java.nio.file.Path;
import java.util.Objects;

final class CofferMinecraftAdminShopPresetListingSubmissionChain {
    private final CofferMinecraftAdminShopPresetListingConfirmation confirmation;
    private final CofferMinecraftAdminShopPresetListingRequestAssembly assembly;
    private final CofferMinecraftSelectedExchangeRuntimeParticipation participation;
    private final CofferMinecraftAdminShopPresetListingAccountabilityProjection accountabilityProjection;

    CofferMinecraftAdminShopPresetListingSubmissionChain(
            CofferMinecraftAdminShopPresetListingConfirmation confirmation,
            CofferMinecraftAdminShopPresetListingRequestAssembly assembly,
            CofferMinecraftSelectedExchangeRuntimeParticipation participation,
            CofferMinecraftAdminShopPresetListingAccountabilityProjection accountabilityProjection) {
        this.confirmation = Objects.requireNonNull(confirmation, "confirmation");
        this.assembly = Objects.requireNonNull(assembly, "assembly");
        this.participation = Objects.requireNonNull(participation, "participation");
        this.accountabilityProjection = Objects.requireNonNull(accountabilityProjection, "accountabilityProjection");
    }

    SubmissionResult submit(
            Path runDirectory,
            CofferMinecraftAdminShopPresetListingConfirmation.ExchangeState state,
            CofferMinecraftAdminShopPresetListingConfirmation.ConfirmationLedger ledger) {
        Objects.requireNonNull(runDirectory, "runDirectory");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(ledger, "ledger");

        CofferMinecraftAdminShopPresetListingConfirmation.SubmissionReadiness readiness =
                confirmation.readiness(state, ledger);
        if (readiness instanceof CofferMinecraftAdminShopPresetListingConfirmation.SubmissionReadiness.NotReady notReady) {
            return new SubmissionResult.NotSubmitted(notReady.reasonCode());
        }

        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult assemblyResult =
                assembly.assemble(state.listing(), state.player(), state.bindingId());
        accountabilityProjection.recordAssembly(runDirectory, assemblyResult);
        if (assemblyResult instanceof CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Refused refused) {
            return new SubmissionResult.NotSubmitted(refused.reasonCode());
        }

        CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared =
                (CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared) assemblyResult;
        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult participationResult =
                participation.participate(prepared.payload());
        accountabilityProjection.recordParticipation(runDirectory, participationResult);
        return new SubmissionResult.Submitted(prepared, participationResult);
    }

    sealed interface SubmissionResult permits SubmissionResult.NotSubmitted, SubmissionResult.Submitted {
        record NotSubmitted(String reasonCode) implements SubmissionResult {
            public NotSubmitted {
                if (reasonCode == null || reasonCode.isBlank()) {
                    throw new IllegalArgumentException("reasonCode must not be null or blank");
                }
            }
        }

        record Submitted(
                CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Prepared prepared,
                CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult participation)
                implements SubmissionResult {
            public Submitted {
                Objects.requireNonNull(prepared, "prepared");
                Objects.requireNonNull(participation, "participation");
            }
        }
    }
}

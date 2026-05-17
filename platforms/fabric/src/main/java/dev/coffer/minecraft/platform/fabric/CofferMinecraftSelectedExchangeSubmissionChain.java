package dev.coffer.minecraft.platform.fabric;

import java.nio.file.Path;
import java.util.Objects;

final class CofferMinecraftSelectedExchangeSubmissionChain {
    private final CofferMinecraftSelectedExchangeConfirmation confirmation;
    private final CofferMinecraftSelectedExchangeRequestAssembly assembly;
    private final CofferMinecraftSelectedExchangeRuntimeParticipation participation;
    private final CofferMinecraftSelectedExchangeAccountabilityProjection accountabilityProjection;

    CofferMinecraftSelectedExchangeSubmissionChain(
            CofferMinecraftSelectedExchangeConfirmation confirmation,
            CofferMinecraftSelectedExchangeRequestAssembly assembly,
            CofferMinecraftSelectedExchangeRuntimeParticipation participation,
            CofferMinecraftSelectedExchangeAccountabilityProjection accountabilityProjection) {
        this.confirmation = Objects.requireNonNull(confirmation, "confirmation");
        this.assembly = Objects.requireNonNull(assembly, "assembly");
        this.participation = Objects.requireNonNull(participation, "participation");
        this.accountabilityProjection = Objects.requireNonNull(accountabilityProjection, "accountabilityProjection");
    }

    SubmissionResult submit(
            Path runDirectory,
            CofferMinecraftSelectedExchangeConfirmation.ExchangeState state,
            CofferMinecraftSelectedExchangeConfirmation.ConfirmationLedger ledger) {
        Objects.requireNonNull(runDirectory, "runDirectory");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(ledger, "ledger");

        CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness readiness =
                confirmation.readiness(state, ledger);
        if (readiness instanceof CofferMinecraftSelectedExchangeConfirmation.SubmissionReadiness.NotReady notReady) {
            return new SubmissionResult.NotSubmitted(notReady.reasonCode());
        }

        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult assemblyResult =
                assembly.assemble(state.first(), state.second(), state.bindingId());
        accountabilityProjection.recordAssembly(runDirectory, assemblyResult);
        if (assemblyResult instanceof CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Refused refused) {
            return new SubmissionResult.NotSubmitted(refused.reasonCode());
        }

        CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared prepared =
                (CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared) assemblyResult;
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
                CofferMinecraftSelectedExchangeRequestAssembly.AssemblyResult.Prepared prepared,
                CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult participation)
                implements SubmissionResult {
            public Submitted {
                Objects.requireNonNull(prepared, "prepared");
                Objects.requireNonNull(participation, "participation");
            }
        }
    }
}

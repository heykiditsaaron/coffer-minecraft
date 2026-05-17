package dev.coffer.minecraft.platform.fabric;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import org.coffer.runtime.model.execution.MutationExecutionResult;
import org.coffer.runtime.model.execution.MutationExecutionStatus;

final class CofferMinecraftAdminShopPresetListingAccountabilityProjection {
    private static final String REASON_CODE = "reasonCode";

    private final CofferMinecraftLifecycleAccountability accountability;

    CofferMinecraftAdminShopPresetListingAccountabilityProjection(CofferMinecraftLifecycleAccountability accountability) {
        this.accountability = Objects.requireNonNull(accountability, "accountability");
    }

    void recordAssembly(Path runDirectory, CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult assembly) {
        Objects.requireNonNull(runDirectory, "runDirectory");
        Objects.requireNonNull(assembly, "assembly");

        if (assembly instanceof CofferMinecraftAdminShopPresetListingRequestAssembly.AssemblyResult.Refused refused) {
            accountability.recordConstructionRefused(runDirectory, refused.reasonCode());
        }
    }

    void recordParticipation(
            Path runDirectory,
            CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult participation) {
        Objects.requireNonNull(runDirectory, "runDirectory");
        Objects.requireNonNull(participation, "participation");

        if (participation instanceof CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.CoreDenied denied) {
            accountability.recordCoreDenied(runDirectory, arbitrationReasonCode(denied.arbitration()));
            return;
        }

        CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated runtime =
                (CofferMinecraftSelectedExchangeRuntimeParticipation.ParticipationResult.RuntimeParticipated) participation;
        accountability.recordCoreApproved(runDirectory);

        MutationExecutionResult mutationResult = runtime.execution().mutationResults().isEmpty()
                ? null
                : runtime.execution().mutationResults().get(0);
        if (mutationResult == null) {
            accountability.recordRuntimeUnknown(runDirectory, null);
            return;
        }
        if (mutationResult.status() == MutationExecutionStatus.MUTATION_SUCCEEDED) {
            accountability.recordRuntimeSucceeded(runDirectory);
            return;
        }
        if (mutationResult.status() == MutationExecutionStatus.MUTATION_FAILED) {
            accountability.recordRuntimeFailed(runDirectory, executionReasonCode(mutationResult));
            return;
        }
        accountability.recordRuntimeUnknown(runDirectory, executionReasonCode(mutationResult));
    }

    private static String arbitrationReasonCode(org.coffer.core.arbitration.ArbitrationResult arbitration) {
        if (arbitration.outcome().reasons().isEmpty()) {
            return null;
        }
        return reasonCode(arbitration.outcome().reasons().get(0).detail().values());
    }

    private static String executionReasonCode(MutationExecutionResult mutationResult) {
        return reasonCode(mutationResult.detail().values());
    }

    private static String reasonCode(Map<String, Object> detailValues) {
        Object reasonCode = detailValues.get(REASON_CODE);
        return reasonCode == null ? null : String.valueOf(reasonCode);
    }
}

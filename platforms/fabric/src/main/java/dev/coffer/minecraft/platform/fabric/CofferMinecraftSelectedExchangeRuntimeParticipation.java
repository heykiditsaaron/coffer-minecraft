package dev.coffer.minecraft.platform.fabric;

import java.util.Objects;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.model.mutation.MutationPlan;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.runtime.model.execution.ExecutionResult;

final class CofferMinecraftSelectedExchangeRuntimeParticipation {
    private final CoreGateway coreGateway;
    private final RuntimeGateway runtimeGateway;

    CofferMinecraftSelectedExchangeRuntimeParticipation(CoreGateway coreGateway, RuntimeGateway runtimeGateway) {
        this.coreGateway = Objects.requireNonNull(coreGateway, "coreGateway");
        this.runtimeGateway = Objects.requireNonNull(runtimeGateway, "runtimeGateway");
    }

    ParticipationResult participate(ExchangePayload payload) {
        Objects.requireNonNull(payload, "payload");

        ArbitrationResult arbitration = coreGateway.arbitrate(payload);
        if (arbitration.outcome().decision() != Decision.APPROVED || arbitration.mutationPlan() == null) {
            return new ParticipationResult.CoreDenied(arbitration);
        }

        MutationPlan mutationPlan = arbitration.mutationPlan();
        ExecutionResult execution = runtimeGateway.execute(mutationPlan);
        return new ParticipationResult.RuntimeParticipated(arbitration, execution);
    }

    sealed interface ParticipationResult
            permits ParticipationResult.CoreDenied, ParticipationResult.RuntimeParticipated {
        record CoreDenied(ArbitrationResult arbitration) implements ParticipationResult {
            public CoreDenied {
                Objects.requireNonNull(arbitration, "arbitration");
            }
        }

        record RuntimeParticipated(ArbitrationResult arbitration, ExecutionResult execution)
                implements ParticipationResult {
            public RuntimeParticipated {
                Objects.requireNonNull(arbitration, "arbitration");
                Objects.requireNonNull(execution, "execution");
            }
        }
    }

    @FunctionalInterface
    interface CoreGateway {
        ArbitrationResult arbitrate(ExchangePayload payload);
    }

    @FunctionalInterface
    interface RuntimeGateway {
        ExecutionResult execute(MutationPlan mutationPlan);
    }
}

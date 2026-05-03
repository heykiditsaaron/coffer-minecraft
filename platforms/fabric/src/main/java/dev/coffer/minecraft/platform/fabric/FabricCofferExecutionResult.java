package dev.coffer.minecraft.platform.fabric;

import java.util.Objects;
import org.coffer.core.model.outcome.Outcome;
import org.coffer.runtime.model.execution.ExecutionResult;

sealed interface FabricCofferExecutionResult
        permits FabricCofferExecutionResult.Denied,
                FabricCofferExecutionResult.Executed,
                FabricCofferExecutionResult.Unavailable {

    record Denied(Outcome outcome) implements FabricCofferExecutionResult {
        public Denied {
            Objects.requireNonNull(outcome, "outcome");
        }
    }

    record Executed(ExecutionResult result) implements FabricCofferExecutionResult {
        public Executed {
            Objects.requireNonNull(result, "result");
        }
    }

    record Unavailable(String reasonCode) implements FabricCofferExecutionResult {
        public Unavailable {
            Objects.requireNonNull(reasonCode, "reasonCode");
        }
    }
}

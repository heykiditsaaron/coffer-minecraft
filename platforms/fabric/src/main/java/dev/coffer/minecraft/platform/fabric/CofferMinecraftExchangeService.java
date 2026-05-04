package dev.coffer.minecraft.platform.fabric;

import java.util.concurrent.CompletableFuture;
import org.coffer.core.model.request.ExchangeRequest;

public interface CofferMinecraftExchangeService {
    CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangeRequest request);
}

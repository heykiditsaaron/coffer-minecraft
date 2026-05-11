package dev.coffer.minecraft.platform.fabric;

import java.util.concurrent.CompletableFuture;
import org.coffer.core.model.request.ExchangePayload;

public interface CofferMinecraftExchangeService {
    CompletableFuture<FabricCofferExecutionResult> submitExchange(ExchangePayload request);
}

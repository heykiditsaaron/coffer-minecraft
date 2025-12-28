package dev.coffer.adapter.fabric.execution.ui;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.core.ExchangeEvaluationResult;
import dev.coffer.core.ValuationSnapshot;

import java.time.Instant;
import java.util.UUID;

public record SellPreviewToken(
        UUID tokenId,
        UUID playerId,
        DeclaredExchangeRequest declaredRequest,
        ExchangeEvaluationResult evaluationResult,
        ValuationSnapshot valuationSnapshot,
        Instant createdAt
) {
}

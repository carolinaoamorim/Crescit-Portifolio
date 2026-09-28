package com.acessorinvestimentos.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PortfolioResponse(
        UUID id,
        String userId,
        BigDecimal totalInvested,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

package com.acessorinvestimentos.portfolio.dto;

import com.acessorinvestimentos.portfolio.model.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InvestmentRequest(
        @NotBlank @Size(max = 20) @Pattern(regexp = "[A-Za-z0-9.-]+", message = "must contain only letters, numbers, dot or hyphen") String ticker,
        @NotBlank @Size(max = 255) String assetName,
        @NotNull AssetType assetType) {
}

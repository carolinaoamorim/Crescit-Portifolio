package com.acessorinvestimentos.portfolio.dto;

import com.acessorinvestimentos.portfolio.model.AssetType;
import com.acessorinvestimentos.portfolio.model.RateType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvestmentRequest(
        @NotBlank @Size(max = 20) @Pattern(regexp = "[A-Za-z0-9.-]+", message = "must contain only letters, numbers, dot or hyphen") String ticker,
        @NotBlank @Size(max = 255) String assetName,
        @NotNull AssetType assetType,
        @Size(max = 60) String institution,
        RateType rateType,
        @DecimalMin(value = "0", inclusive = false) @DecimalMax("1000") BigDecimal rateValue,
        LocalDate maturityDate) {

    /** Formato original, sem instituição nem rentabilidade. */
    public InvestmentRequest(String ticker, String assetName, AssetType assetType) {
        this(ticker, assetName, assetType, null, null, null, null);
    }
}

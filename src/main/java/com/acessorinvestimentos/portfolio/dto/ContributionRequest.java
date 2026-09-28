package com.acessorinvestimentos.portfolio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContributionRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 11, fraction = 8) BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 11, fraction = 8) BigDecimal unitPrice,
        @NotNull @PastOrPresent LocalDate contributionDate) {
}

package com.homebudget.income.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record IncomeRequest(
        @NotNull @PositiveOrZero Integer amount // em centavos
) {}

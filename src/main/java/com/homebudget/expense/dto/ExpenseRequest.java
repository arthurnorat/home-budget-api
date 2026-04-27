package com.homebudget.expense.dto;

import com.homebudget.expense.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

// record = classe imutável de dados (sem setters, construtor gerado automaticamente)
public record ExpenseRequest(
        @NotBlank String description,
        @NotNull @Positive Integer amount, // em centavos
        @NotNull LocalDate date,
        @NotNull Category category
) {}

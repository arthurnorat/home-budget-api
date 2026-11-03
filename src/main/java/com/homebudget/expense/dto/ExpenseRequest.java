package com.homebudget.expense.dto;

import com.homebudget.expense.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank String description,
        @NotNull @Positive Integer amount, // in cents
        @NotNull LocalDate date,
        @NotNull Category category
) {}

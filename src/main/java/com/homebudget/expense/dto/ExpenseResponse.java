package com.homebudget.expense.dto;

import com.homebudget.expense.Category;
import com.homebudget.expense.Expense;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID expenseId,
        String description,
        Integer amount,   // em centavos
        LocalDate date,
        Category category,
        Instant createdAt
) {
    // Método factory: converte uma entidade Expense em ExpenseResponse
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getExpenseId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getDate(),
                expense.getCategory(),
                expense.getCreatedAt()
        );
    }
}

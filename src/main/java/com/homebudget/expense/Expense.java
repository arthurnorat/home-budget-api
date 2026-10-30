package com.homebudget.expense;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Expense {

    private UUID expenseId;
    private String description;
    private Integer amount; // stored in cents (e.g.: R$ 1,500.00 = 150000)
    private LocalDate date;
    private Category category;
    private Instant createdAt;

    public UUID getExpenseId() { return expenseId; }
    public void setExpenseId(UUID expenseId) { this.expenseId = expenseId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

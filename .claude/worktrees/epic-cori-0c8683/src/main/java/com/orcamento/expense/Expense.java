package com.orcamento.expense;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "expense_id")
    private UUID expenseId;

    @NotBlank
    private String description;

    @NotNull
    @Positive
    private Integer amount; // armazenado em centavos (ex: R$ 1.500,00 = 150000)

    @NotNull
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Category category;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

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
}

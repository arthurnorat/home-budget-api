package com.homebudget.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // SELECT * FROM expenses WHERE date BETWEEN :start AND :end ORDER BY date DESC
    List<Expense> findByDateBetweenOrderByDateDesc(LocalDate start, LocalDate end);

    // SELECT * FROM expenses WHERE category = :category AND date BETWEEN :start AND :end ORDER BY date DESC
    List<Expense> findByCategoryAndDateBetweenOrderByDateDesc(Category category, LocalDate start, LocalDate end);
}

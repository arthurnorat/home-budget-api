package com.orcamento.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // Spring Data JPA gera o SQL automaticamente a partir do nome do método:
    // SELECT * FROM expenses WHERE date BETWEEN :start AND :end ORDER BY date DESC
    List<Expense> findByDateBetweenOrderByDateDesc(LocalDate start, LocalDate end);
}

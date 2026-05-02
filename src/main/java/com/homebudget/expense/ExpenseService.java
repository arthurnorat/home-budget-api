package com.homebudget.expense;

import com.homebudget.exception.ExpenseNotFoundException;
import com.homebudget.expense.dto.ExpenseRequest;
import com.homebudget.expense.dto.ExpenseResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    // Injeção de dependência via construtor (preferível ao @Autowired)
    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setCategory(request.category());
        return ExpenseResponse.from(repository.save(expense));
    }

    public List<ExpenseResponse> listByMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return repository.findByDateBetweenOrderByDateDesc(start, end)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    public ExpenseResponse update(UUID id, ExpenseRequest request) {
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setCategory(request.category());
        return ExpenseResponse.from(repository.save(expense));
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ExpenseNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public List<ExpenseResponse> importFixed(YearMonth month) {
        YearMonth previousMonth = month.minusMonths(1);
        LocalDate start = previousMonth.atDay(1);
        LocalDate end = previousMonth.atEndOfMonth();

        List<Expense> fixed = repository.findByCategoryAndDateBetweenOrderByDateDesc(
                Category.FIXED, start, end);

        LocalDate importDate = month.atDay(1);
        List<Expense> copies = fixed.stream().map(source -> {
            Expense copy = new Expense();
            copy.setDescription(source.getDescription());
            copy.setAmount(source.getAmount());
            copy.setDate(importDate);
            copy.setCategory(Category.FIXED);
            return copy;
        }).toList();

        return repository.saveAll(copies).stream()
                .map(ExpenseResponse::from)
                .toList();
    }
}

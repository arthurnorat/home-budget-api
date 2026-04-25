package com.orcamento.expense;

import com.orcamento.exception.ExpenseNotFoundException;
import com.orcamento.expense.dto.ExpenseRequest;
import com.orcamento.expense.dto.ExpenseResponse;
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

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ExpenseNotFoundException(id);
        }
        repository.deleteById(id);
    }
}

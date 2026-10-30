package com.homebudget.expense;

import com.homebudget.expense.dto.ExpenseRequest;
import com.homebudget.expense.dto.ExpenseResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@RequestBody @Valid ExpenseRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return service.listByMonth(month != null ? month : YearMonth.now());
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable UUID id, @RequestBody @Valid ExpenseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/import-fixed")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ExpenseResponse> importFixed(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.importFixed(month);
    }
}

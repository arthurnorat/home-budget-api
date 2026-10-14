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

    // POST /expenses  →  creates a new expense
    // @Valid triggers the validations defined in ExpenseRequest (@NotBlank, @NotNull, etc.)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // returns 201 instead of 200
    public ExpenseResponse create(@RequestBody @Valid ExpenseRequest request) {
        return service.create(request);
    }

    // GET /expenses           →  lists expenses for the current month
    // GET /expenses?month=2026-02  →  lists expenses for February 2026
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return service.listByMonth(month != null ? month : YearMonth.now());
    }

    // PUT /expenses/{id}  →  updates an existing expense
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable UUID id, @RequestBody @Valid ExpenseRequest request) {
        return service.update(id, request);
    }

    // DELETE /expenses/{id}  →  deletes an expense
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // returns 204 instead of 200
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    // POST /expenses/import-fixed?month=YYYY-MM  →  copies fixed expenses from the previous month
    @PostMapping("/import-fixed")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ExpenseResponse> importFixed(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.importFixed(month);
    }
}

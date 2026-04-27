package com.orcamento.expense;

import com.orcamento.expense.dto.ExpenseRequest;
import com.orcamento.expense.dto.ExpenseResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    // POST /expenses  →  cria um novo gasto
    // @Valid aciona as validações definidas no ExpenseRequest (@NotBlank, @NotNull, etc.)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // retorna 201 em vez de 200
    public ExpenseResponse create(@RequestBody @Valid ExpenseRequest request) {
        return service.create(request);
    }

    // GET /expenses           →  lista gastos do mês atual
    // GET /expenses?month=2026-02  →  lista gastos de fevereiro de 2026
    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return service.listByMonth(month != null ? month : YearMonth.now());
    }
}

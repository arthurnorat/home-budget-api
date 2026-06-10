package com.homebudget.income;

import com.homebudget.income.dto.IncomeRequest;
import com.homebudget.income.dto.IncomeResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/income")
public class MonthlyIncomeController {

    private final MonthlyIncomeService service;

    public MonthlyIncomeController(MonthlyIncomeService service) {
        this.service = service;
    }

    // GET /income?month=yyyy-MM  →  retorna a entrada do mês (0 se não registrada)
    @GetMapping
    public IncomeResponse get(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return service.get(month != null ? month : YearMonth.now());
    }

    // PUT /income?month=yyyy-MM  →  salva ou atualiza a entrada do mês
    @PutMapping
    public IncomeResponse save(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestBody @Valid IncomeRequest request
    ) {
        return service.save(month != null ? month : YearMonth.now(), request.amount());
    }
}

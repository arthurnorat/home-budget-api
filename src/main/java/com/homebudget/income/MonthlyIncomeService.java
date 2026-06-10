package com.homebudget.income;

import com.homebudget.income.dto.IncomeResponse;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
public class MonthlyIncomeService {

    private final MonthlyIncomeRepository repository;

    public MonthlyIncomeService(MonthlyIncomeRepository repository) {
        this.repository = repository;
    }

    public IncomeResponse get(YearMonth month) {
        return repository.findById(month.toString())
                .map(IncomeResponse::from)
                .orElse(new IncomeResponse(month.toString(), 0));
    }

    public IncomeResponse save(YearMonth month, int amount) {
        MonthlyIncome income = repository.findById(month.toString())
                .orElse(new MonthlyIncome(month.toString(), 0));
        income.setAmount(amount);
        return IncomeResponse.from(repository.save(income));
    }
}

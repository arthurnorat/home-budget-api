package com.homebudget.income.dto;

import com.homebudget.income.MonthlyIncome;

public record IncomeResponse(
        String month,
        Integer amount // in cents
) {
    public static IncomeResponse from(MonthlyIncome income) {
        return new IncomeResponse(income.getMonth(), income.getAmount());
    }
}

package com.homebudget.income;

public class MonthlyIncome {

    private String month; // format yyyy-MM
    private Integer amount; // in cents

    public MonthlyIncome() {}

    public MonthlyIncome(String month, Integer amount) {
        this.month = month;
        this.amount = amount;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}

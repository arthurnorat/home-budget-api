package com.homebudget.income;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "monthly_income")
public class MonthlyIncome {

    @Id
    private String month; // formato yyyy-MM

    @Column(nullable = false)
    private Integer amount; // em centavos

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

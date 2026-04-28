package com.ru.fintracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;

    // Подавляем предупреждение, методы используются Thymeleaf
    @SuppressWarnings("unused")
    public BigDecimal getSafeIncome() {
        return totalIncome != null ? totalIncome : BigDecimal.ZERO;
    }

    @SuppressWarnings("unused")
    public BigDecimal getSafeExpense() {
        return totalExpense != null ? totalExpense : BigDecimal.ZERO;
    }

    @SuppressWarnings("unused")
    public BigDecimal getSafeBalance() {
        return balance != null ? balance : BigDecimal.ZERO;
    }
}
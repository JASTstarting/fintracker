package com.ru.fintracker.service;

import com.ru.fintracker.model.FinancialSummary;
import com.ru.fintracker.model.Transaction;
import com.ru.fintracker.repository.CustomQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CustomQueries customQueries;

    /**
     * Получить финансовый итог за период (по умолчанию — текущий месяц)
     */
    public FinancialSummary getFinancialSummary(LocalDate from, LocalDate to, String currency) {
        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = LocalDate.now();
        return customQueries.getFinancialSummary(from, to, currency);
    }

    /**
     * Получить последние транзакции для дашборда
     */
    public List<Transaction> getLastTransactions(int limit) {
        return customQueries.getRecentTransactions(limit);
    }
}
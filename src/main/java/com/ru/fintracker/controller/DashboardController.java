package com.ru.fintracker.controller;

import com.ru.fintracker.model.FinancialSummary;
import com.ru.fintracker.model.Transaction;
import com.ru.fintracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String currency,
            Model model
    ) {
        String effectiveCurrency = (currency != null && !currency.isBlank()) ? currency : null;

        FinancialSummary summary = dashboardService.getFinancialSummary(from, to, effectiveCurrency);
        model.addAttribute("summary", summary);
        model.addAttribute("periodFrom", from != null ? from : LocalDate.now().withDayOfMonth(1));
        model.addAttribute("periodTo", to != null ? to : LocalDate.now());
        model.addAttribute("selectedCurrency", effectiveCurrency);

        List<Transaction> lastTransactions = dashboardService.getLastTransactions(5);
        model.addAttribute("lastTransactions", lastTransactions);

        model.addAttribute("pageContent", "dashboard");
        model.addAttribute("pageTitle", "Главная");

        return "layout";
    }
}
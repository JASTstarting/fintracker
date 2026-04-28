package com.ru.fintracker.controller;

import com.ru.fintracker.dto.FilterParams;
import com.ru.fintracker.dto.TransactionForm;
import com.ru.fintracker.exception.BusinessValidationException;
import com.ru.fintracker.model.Transaction;
import com.ru.fintracker.service.CategoryService;
import com.ru.fintracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @GetMapping
    public String listTransactions(@ModelAttribute FilterParams params, Model model) {
        log.debug(">>> Запрос списка: filters={}, page={}, size={}", params, params.getPage(), params.getSize());

        var page = transactionService.getTransactionsWithFilters(params, params.getOffset(), params.getSize());

        model.addAttribute("transactions", page.content());
        model.addAttribute("currentPage", params.getPage());
        model.addAttribute("totalPages", page.totalPages());
        model.addAttribute("hasNext", page.hasNext());
        model.addAttribute("hasPrevious", page.hasPrevious());
        model.addAttribute("filters", params);
        model.addAttribute("categories", categoryService.getActiveCategories());

        model.addAttribute("pageContent", "transaction-list");
        model.addAttribute("pageTitle", "Список операций");

        return "layout";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) String type, Model model) {
        TransactionForm form = new TransactionForm();
        if (type != null && List.of("INCOME", "EXPENSE").contains(type.toUpperCase())) {
            form.setType(type.toUpperCase());
        }

        model.addAttribute("form", form);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("pageTitle", "Новая операция");

        model.addAttribute("pageContent", "transaction-form");

        return "layout";
    }

    @PostMapping
    public String createTransaction(
            @Valid @ModelAttribute("form") TransactionForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            log.warn("Ошибки валидации: {}", result.getFieldErrors());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("pageContent", "transaction-form");
            model.addAttribute("pageTitle", "Новая операция");
            return "layout";
        }

        try {
            transactionService.createTransaction(form);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Операция успешно создана");
            return "redirect:/transactions";

        } catch (BusinessValidationException e) {
            log.warn("Business validation error: {}", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("globalError", e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("pageContent", "transaction-form");
            model.addAttribute("pageTitle", "Новая операция");
            return "layout";

        } catch (Exception e) {
            log.error("Unexpected error during creation", e);
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
            return "redirect:/transactions/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Transaction transaction = transactionService.getActiveTransactionById(id);

            TransactionForm form = new TransactionForm();
            form.setType(transaction.getType());
            form.setAmount(transaction.getAmount());
            form.setCurrency(transaction.getCurrency());
            form.setTransactionDate(transaction.getTransactionDate());
            form.setCategoryId(transaction.getCategoryId());
            form.setTransactionComment(transaction.getTransactionComment());

            model.addAttribute("form", form);
            model.addAttribute("transactionId", id);
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("pageTitle", "Редактирование операции");

            model.addAttribute("pageContent", "transaction-form");

            return "layout";

        } catch (Exception e) {
            log.error("Ошибка при загрузке #{}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
            return "redirect:/transactions";
        }
    }

    @PostMapping("/{id}/update")
    public String updateTransaction(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") TransactionForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("transactionId", id);
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("pageContent", "transaction-form");
            model.addAttribute("pageTitle", "Редактирование операции");
            return "layout";
        }

        try {
            transactionService.updateTransaction(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Операция обновлена");
            return "redirect:/transactions";

        } catch (BusinessValidationException e) {
            log.warn("Business validation error: {}", e.getMessage());
            model.addAttribute("form", form);
            model.addAttribute("transactionId", id);
            model.addAttribute("globalError", e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("pageContent", "transaction-form");
            model.addAttribute("pageTitle", "Редактирование операции");
            return "layout";

        } catch (Exception e) {
            log.error("Ошибка при обновлении #{}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
            return "redirect:/transactions/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/archive")
    public String archiveTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            transactionService.archiveTransaction(id);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Операция архивирована");
        } catch (Exception e) {
            log.error("Ошибка при архивации #{}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/transactions";
    }
}
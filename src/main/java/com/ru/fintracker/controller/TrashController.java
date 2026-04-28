package com.ru.fintracker.controller;

import com.ru.fintracker.model.Transaction;
import com.ru.fintracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/trash")
@RequiredArgsConstructor
@Slf4j
public class TrashController {

    private final TransactionService transactionService;

    @GetMapping
    public String showTrash(Model model) {
        List<Transaction> archivedTransactions = transactionService.getAllArchivedTransactions();
        model.addAttribute("transactions", archivedTransactions);

        model.addAttribute("pageContent", "trash");
        model.addAttribute("pageTitle", "Корзина");

        return "layout";
    }

    @PostMapping("/{id}/restore")
    public String restoreTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            transactionService.restoreTransaction(id);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Операция восстановлена");
        } catch (Exception e) {
            log.error("Ошибка при восстановлении #{}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/trash";
    }

    @PostMapping("/{id}/delete")
    public String permanentDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            transactionService.permanentDeleteTransaction(id);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Операция удалена навсегда");
        } catch (Exception e) {
            log.error("Ошибка при удалении #{}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/trash";
    }
}
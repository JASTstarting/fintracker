package com.ru.fintracker.exception;

import com.ru.fintracker.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final CategoryService categoryService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationErrors(
            MethodArgumentNotValidException ex,
            Model model
    ) {
        BindingResult result = ex.getBindingResult();

        result.getFieldErrors().forEach(error ->
                log.warn("Validation error on field '{}': {}", error.getField(), error.getDefaultMessage())
        );

        Object target = result.getTarget();
        if (target != null) {
            model.addAttribute("form", target);
        }

        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("pageTitle", model.containsAttribute("transactionId")
                ? "Редактирование операции"
                : "Новая операция");
        model.addAttribute("pageContent", "transaction-form");

        return "layout";
    }

    @ExceptionHandler(BusinessValidationException.class)
    public String handleBusinessError(
            BusinessValidationException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        log.warn("Business validation error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "❌ " + ex.getMessage());

        String referer = request.getHeader("Referer");
        if (isValidReferer(referer, request)) {
            return "redirect:" + referer;
        }
        return "redirect:/transactions";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericError(
            Exception ex,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        log.error("Unexpected error occurred", ex);

        if (redirectAttributes != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Произошла непредвиденная ошибка");
            return "redirect:/transactions";
        }

        model.addAttribute("errorMessage", "❌ Произошла непредвиденная ошибка");
        model.addAttribute("pageTitle", "Ошибка");
        model.addAttribute("pageContent", "error");
        return "layout";
    }

    private boolean isValidReferer(String referer, HttpServletRequest request) {
        if (referer == null || referer.isEmpty()) {
            return false;
        }
        try {
            java.net.URI refererUri = java.net.URI.create(referer);

            String serverName = request.getServerName();
            int serverPort = request.getServerPort();

            return refererUri.getHost() != null
                    && refererUri.getHost().equals(serverName)
                    && refererUri.getPort() == serverPort;

        } catch (IllegalArgumentException e) {
            log.warn("Invalid URI in Referer: {}", referer);
            return false;
        }
    }
}
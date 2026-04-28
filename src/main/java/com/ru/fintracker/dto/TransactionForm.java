package com.ru.fintracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionForm {

    @NotNull(message = "Тип операции обязателен")
    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Недопустимый тип операции")
    private String type;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    @Pattern(regexp = "^(RUB|USD|EUR)$", message = "Неподдерживаемая валюта")
    private String currency = "RUB";

    @NotNull(message = "Дата обязательна")
    @PastOrPresent(message = "Дата не может быть в будущем")
    private LocalDate transactionDate;

    @NotNull(message = "Категория обязательна")
    private Long categoryId;

    @Size(max = 500, message = "Комментарий не более 500 символов")
    private String transactionComment;

    public boolean isIncome() {
        return "INCOME".equalsIgnoreCase(type);
    }

    public boolean isExpense() {
        return "EXPENSE".equalsIgnoreCase(type);
    }
}
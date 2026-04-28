package com.ru.fintracker.exception;

/**
 * Кастомное исключение для бизнес-ошибок валидации.
 * Используется, когда данные прошли синтаксическую проверку,
 * но нарушают бизнес-правила (например, категория неактивна).
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
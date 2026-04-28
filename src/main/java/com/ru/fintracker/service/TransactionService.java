package com.ru.fintracker.service;

import com.ru.fintracker.dto.FilterParams;
import com.ru.fintracker.dto.TransactionForm;
import com.ru.fintracker.exception.BusinessValidationException;
import com.ru.fintracker.model.Category;
import com.ru.fintracker.model.Transaction;
import com.ru.fintracker.repository.CategoryRepository;
import com.ru.fintracker.repository.CustomQueries;
import com.ru.fintracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CustomQueries customQueries;

    /**
     * Получить транзакции с пагинацией, фильтрацией и подставленными именами категорий
     */
    public CustomQueries.TransactionPage getTransactionsWithFilters(FilterParams params, int offset, int limit) {
        log.debug("Getting transactions with filters: type={}, categoryId={}, from={}, to={}",
                params.getType(), params.getCategoryId(), params.getFrom(), params.getTo());

        CustomQueries.TransactionPage page = customQueries.findWithFilters(params, offset, limit);
        List<Transaction> enriched = enrichWithCategoryNames(page.content());

        return new CustomQueries.TransactionPage(enriched, page.total(), offset, limit);
    }

    /**
     * Подставить имена категорий в список транзакций
     */
    private List<Transaction> enrichWithCategoryNames(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return transactions;
        }

        // Загружаем все активные категории один раз (один запрос к БД)
        Map<Long, Category> categoryMap = categoryRepository.findByIsActive("Y")
                .stream()
                .collect(Collectors.toMap(Category::getId, cat -> cat));

        // Для каждой транзакции подставляем имя категории
        for (Transaction transaction : transactions) {
            Category category = categoryMap.get(transaction.getCategoryId());
            transaction.setCategoryName(category != null ? category.getName() : "❌ Категория удалена");
        }

        return transactions;
    }

    /**
     * Получить транзакцию по ID (с проверкой, что она активна)
     */
    public Transaction getActiveTransactionById(Long id) {
        return transactionRepository.findById(id)
                .filter(t -> "ACTIVE".equals(t.getStatus()))
                .orElseThrow(() -> new BusinessValidationException("Операция не найдена или архивирована"));
    }

    /**
     * Валидация транзакции (общая логика для создания и обновления)
     */
    private void validateTransaction(TransactionForm form, Category category) {
        if (!category.getType().equals(form.getType())) {
            throw new BusinessValidationException(
                    String.format("Категория '%s' не подходит для типа операции '%s'",
                            category.getName(),
                            "INCOME".equals(form.getType()) ? "дохода" : "расхода")
            );
        }

        if (form.getTransactionDate().isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Дата операции не может быть в будущем");
        }

        if (form.getAmount() == null || form.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Сумма должна быть больше 0");
        }
    }

    /**
     * Создание новой транзакции
     */
    @Transactional
    public void createTransaction(TransactionForm form) {
        log.info("Creating new transaction: amount={}, type={}", form.getAmount(), form.getType());

        Category category = categoryRepository.findById(form.getCategoryId())
                .filter(c -> "Y".equals(c.getIsActive()))
                .orElseThrow(() -> new BusinessValidationException("Категория не найдена или неактивна"));

        validateTransaction(form, category);

        Long newId = customQueries.nextTransactionId();

        // 3. Маппинг DTO → Entity
        Transaction transaction = new Transaction();
        transaction.setId(newId);
        transaction.setAmount(form.getAmount());
        transaction.setCurrency(form.getCurrency() != null ? form.getCurrency() : "RUB");
        transaction.setTransactionDate(form.getTransactionDate());
        transaction.setCategoryId(form.getCategoryId());
        transaction.setType(form.getType());
        transaction.setTransactionComment(form.getTransactionComment());
        transaction.setStatus("ACTIVE");

        LocalDateTime now = LocalDateTime.now();
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        customQueries.insertTransaction(transaction);
        log.info("Transaction created with id: {}", transaction.getId());
    }

    /**
     * Обновление существующей транзакции
     */
    @Transactional
    public void updateTransaction(Long id, TransactionForm form) {
        log.info("Updating transaction id: {}", id);

        Transaction transaction = getActiveTransactionById(id);

        Category category = categoryRepository.findById(form.getCategoryId())
                .filter(c -> "Y".equals(c.getIsActive()))
                .orElseThrow(() -> new BusinessValidationException("Категория не найдена или неактивна"));

        validateTransaction(form, category);

        transaction.setAmount(form.getAmount());
        transaction.setCurrency(form.getCurrency() != null ? form.getCurrency() : "RUB");
        transaction.setTransactionDate(form.getTransactionDate());
        transaction.setCategoryId(form.getCategoryId());
        transaction.setType(form.getType());
        transaction.setTransactionComment(form.getTransactionComment());

        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        log.info("Transaction {} updated", id);
    }

    /**
     * Мягкое удаление (архивация) — запись не удаляется, только меняет статус
     */
    @Transactional
    public void archiveTransaction(Long id) {
        log.info("Archiving transaction id: {}", id);
        Transaction transaction = getActiveTransactionById(id);
        transaction.setStatus("ARCHIVED");
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        log.info("Transaction {} archived", id);
    }

    /**
     * Получить все архивированные транзакции (для корзины)
     */
    public List<Transaction> getAllArchivedTransactions() {
        List<Transaction> transactions = transactionRepository.findByStatusOrderByTransactionDateDesc("ARCHIVED");
        return enrichWithCategoryNames(transactions);
    }

    /**
     * Восстановить транзакцию из архива
     */
    @Transactional
    public void restoreTransaction(Long id) {
        log.info("Restoring transaction id: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> "ARCHIVED".equals(t.getStatus()))
                .orElseThrow(() -> new BusinessValidationException("Операция не найдена или не в архиве"));
        transaction.setStatus("ACTIVE");
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        log.info("Transaction {} restored", id);
    }

    /**
     * Полное физическое удаление транзакции (НЕОБРАТИМО!)
     */
    @Transactional
    public void permanentDeleteTransaction(Long id) {
        log.warn("PERMANENTLY deleting transaction id: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new BusinessValidationException("Операция не найдена"));
        transactionRepository.delete(transaction);
        log.warn("Transaction {} permanently deleted", id);
    }
}
package com.ru.fintracker.service;

import com.ru.fintracker.dto.TransactionForm;
import com.ru.fintracker.exception.BusinessValidationException;
import com.ru.fintracker.model.Category;
import com.ru.fintracker.model.Transaction;
import com.ru.fintracker.repository.CategoryRepository;
import com.ru.fintracker.repository.CustomQueries;
import com.ru.fintracker.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService — юнит-тесты бизнес-логики")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CustomQueries customQueries;
    @Mock private JdbcClient jdbcClient;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("createTransaction выбрасывает ошибку при несовпадении типа категории")
    void createTransaction_throwsWhenCategoryTypeMismatch() {
        // Arrange
        TransactionForm form = new TransactionForm();
        form.setType("INCOME");
        form.setAmount(BigDecimal.valueOf(100));
        form.setTransactionDate(LocalDate.now());
        form.setCategoryId(1L);

        Category category = new Category();
        category.setId(1L);
        category.setName("Продукты");
        category.setType("EXPENSE");
        category.setIsActive("Y");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act & Assert
        assertThatThrownBy(() -> transactionService.createTransaction(form))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("не подходит для типа операции");
    }

    @Test
    @DisplayName("createTransaction выбрасывает ошибку при дате в будущем")
    void createTransaction_throwsWhenDateInFuture() {
        TransactionForm form = new TransactionForm();
        form.setType("EXPENSE");
        form.setAmount(BigDecimal.valueOf(50));
        form.setTransactionDate(LocalDate.now().plusDays(1));
        form.setCategoryId(3L);

        Category category = new Category();
        category.setId(3L);
        category.setType("EXPENSE");
        category.setIsActive("Y");

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> transactionService.createTransaction(form))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Дата операции не может быть в будущем");
    }

    @Test
    @DisplayName("archiveTransaction меняет статус на ARCHIVED")
    void archiveTransaction_changesStatusToArchived() {
        // Arrange
        Long id = 42L;
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setStatus("ACTIVE");

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        // Act
        transactionService.archiveTransaction(id);

        // Assert
        assertThat(transaction.getStatus()).isEqualTo("ARCHIVED");
        verify(transactionRepository).save(transaction);
    }
}
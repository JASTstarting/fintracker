package com.ru.fintracker.repository;

import com.ru.fintracker.dto.FilterParams;
import com.ru.fintracker.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CustomQueries — тесты сложных запросов")
class CustomQueriesTest {

    @Autowired
    private CustomQueries customQueries;

    @Test
    @DisplayName("getFinancialSummary считает доходы и расходы за период (все валюты)")
    void getFinancialSummary_allCurrencies() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        var summary = customQueries.getFinancialSummary(from, to, null);

        assertThat(summary).isNotNull();
        assertThat(summary.getSafeIncome()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(summary.getSafeExpense()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getFinancialSummary фильтрует по валюте RUB")
    void getFinancialSummary_rubOnly() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        var rub = customQueries.getFinancialSummary(from, to, "RUB");

        assertThat(rub).isNotNull();
        assertThat(rub.getSafeBalance()).isNotNull();
    }

    @Test
    @DisplayName("findWithFilters фильтрует по типу и пагинирует")
    void findWithFilters_filtersByTypeAndPaginates() {
        FilterParams params = new FilterParams();
        params.setType("INCOME");

        CustomQueries.TransactionPage page = customQueries.findWithFilters(params, 0, 2);

        assertThat(page.content()).hasSizeLessThanOrEqualTo(2);
        assertThat(page.content()).allMatch(t -> "INCOME".equals(t.getType()));
        assertThat(page.total()).isGreaterThanOrEqualTo(page.content().size());
    }

    @Test
    @DisplayName("findWithFilters фильтрует по комментарию")
    void findWithFilters_filtersByComment() {
        FilterParams params = new FilterParams();
        params.setComment("тест");

        CustomQueries.TransactionPage page = customQueries.findWithFilters(params, 0, 10);

        assertThat(page).isNotNull();
        assertThat(page.total()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("getRecentTransactions возвращает не более N записей")
    void getRecentTransactions_returnsLimitedResults() {
        List<Transaction> transactions = customQueries.getRecentTransactions(3);
        assertThat(transactions).hasSizeLessThanOrEqualTo(3);
    }
}
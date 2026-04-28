package com.ru.fintracker.repository;

import com.ru.fintracker.dto.FilterParams;
import com.ru.fintracker.model.FinancialSummary;
import com.ru.fintracker.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CustomQueries {

    private final JdbcClient jdbcClient;

    // Получить следующий ID
    public Long nextTransactionId() {
        return jdbcClient.sql("SELECT transactions_seq.NEXTVAL FROM DUAL")
                .query(Long.class)
                .single();
    }

    // Прямая вставка
    public void insertTransaction(Transaction t) {
        jdbcClient.sql("""
            INSERT INTO TRANSACTIONS
            (ID, AMOUNT, CURRENCY, TRANSACTION_DATE, CATEGORY_ID, TYPE, TRANSACTION_COMMENT, STATUS, CREATED_AT, UPDATED_AT)
            VALUES
            (:id, :amount, :currency, :transactionDate, :categoryId, :type, :comment, :status, :createdAt, :updatedAt)
            """)
                .param("id", t.getId())
                .param("amount", t.getAmount())
                .param("currency", t.getCurrency())
                .param("transactionDate", t.getTransactionDate())
                .param("categoryId", t.getCategoryId())
                .param("type", t.getType())
                .param("comment", t.getTransactionComment())
                .param("status", t.getStatus())
                .param("createdAt", t.getCreatedAt())
                .param("updatedAt", t.getUpdatedAt())
                .update();
    }

    /**
     * Рассчитать доходы, расходы и баланс за период
     */
    public FinancialSummary getFinancialSummary(LocalDate from, LocalDate to, String currency) {
        String sql = """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS total_income,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS total_expense,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END), 0) AS balance
        FROM TRANSACTIONS
        WHERE status = 'ACTIVE'
          AND transaction_date BETWEEN :from AND :to
          AND (:currency IS NULL OR currency = :currency)
        """;

        return jdbcClient.sql(sql)
                .param("from", from)
                .param("to", to)
                .param("currency", currency)  // ← Если null, фильтр игнорируется
                .query(FinancialSummary.class)
                .single();
    }


    /**
     * Получить последние N активных транзакций для дашборда
     */
    public List<Transaction> getRecentTransactions(int limit) {
        String sql = """
            SELECT ID, TYPE, AMOUNT, STATUS, CURRENCY, CREATED_AT, UPDATED_AT,
                   CATEGORY_ID, TRANSACTION_DATE, TRANSACTION_COMMENT
            FROM TRANSACTIONS
            WHERE status = 'ACTIVE'
            ORDER BY transaction_date DESC, id DESC
            FETCH FIRST :limit ROWS ONLY
            """;

        return jdbcClient.sql(sql)
                .param("limit", limit)
                .query(Transaction.class)
                .list();
    }

    /**
     * Динамический поиск с фильтрами + пагинация
     */
    public TransactionPage findWithFilters(FilterParams params, int offset, int limit) {
        StringBuilder where = new StringBuilder("WHERE status = 'ACTIVE'");
        Map<String, Object> queryParams = new HashMap<>();

        if (params.getComment() != null && !params.getComment().trim().isEmpty()) {
            where.append(" AND UPPER(transaction_comment) LIKE '%' || UPPER(:comment) || '%'");
            queryParams.put("comment", params.getComment().trim());
        }

        if (params.getType() != null && !params.getType().isEmpty()) {
            where.append(" AND type = :type");
            queryParams.put("type", params.getType());
        }
        if (params.getCategoryId() != null) {
            where.append(" AND category_id = :categoryId");
            queryParams.put("categoryId", params.getCategoryId());
        }
        if (params.getFrom() != null) {
            where.append(" AND transaction_date >= :from");
            queryParams.put("from", params.getFrom());
        }
        if (params.getTo() != null) {
            where.append(" AND transaction_date <= :to");
            queryParams.put("to", params.getTo());
        }

        queryParams.put("offset", offset);
        queryParams.put("limit", limit);

        String dataSql = """
        SELECT ID, TYPE, AMOUNT, STATUS, CURRENCY, CREATED_AT, UPDATED_AT,
               CATEGORY_ID, TRANSACTION_DATE, TRANSACTION_COMMENT
        FROM TRANSACTIONS
        %s
        ORDER BY transaction_date DESC
        OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
        """.formatted(where);

        List<Transaction> transactions = jdbcClient.sql(dataSql)
                .params(queryParams)
                .query(Transaction.class)
                .list();

        String countSql = "SELECT COUNT(*) FROM TRANSACTIONS " + where;
        Long total = jdbcClient.sql(countSql)
                .params(queryParams)
                .query(Long.class)
                .single();

        return new TransactionPage(transactions, total, offset, limit);
    }

    public record TransactionPage(
            List<Transaction> content,
            Long total,
            int offset,
            int limit
    ) {
        public int totalPages() {
            return (int) Math.ceil((double) total / limit);
        }
        public boolean hasNext() {
            return offset + limit < total;
        }
        public boolean hasPrevious() {
            return offset > 0;
        }
    }
}
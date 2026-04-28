package com.ru.fintracker.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("TRANSACTIONS")
public class Transaction {

    @Id
    private Long id;

    private BigDecimal amount;

    @Column("CURRENCY")
    private String currency;

    @Column("TRANSACTION_DATE")
    private LocalDate transactionDate;

    @Column("CATEGORY_ID")
    private Long categoryId;

    @Column("TYPE")
    private String type;

    @Column("TRANSACTION_COMMENT")
    private String transactionComment;

    @Column("STATUS")
    private String status;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;

    @Transient  // Не сохраняется в БД, только для отображения
    private String categoryName;
}
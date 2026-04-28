package com.ru.fintracker.repository;

import com.ru.fintracker.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findByStatusOrderByTransactionDateDesc(String status);

}
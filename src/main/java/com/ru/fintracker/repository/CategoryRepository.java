package com.ru.fintracker.repository;

import com.ru.fintracker.model.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {

    List<Category> findByIsActive(String isActive);
}
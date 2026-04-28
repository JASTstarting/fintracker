package com.ru.fintracker.service;

import com.ru.fintracker.model.Category;
import com.ru.fintracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getActiveCategories() {
        // Spring Data JDBC возвращает Iterable, конвертируем в List
        return new ArrayList<>(categoryRepository.findByIsActive("Y"));
    }
}
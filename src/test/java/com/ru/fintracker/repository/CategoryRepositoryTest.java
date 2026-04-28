package com.ru.fintracker.repository;

import com.ru.fintracker.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CategoryRepository — тесты справочника категорий")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("findByIsActive('Y') возвращает только активные категории")
    void findByIsActive_returnsOnlyActiveCategories() {
        List<Category> active = categoryRepository.findByIsActive("Y");

        assertThat(active)
                .isNotEmpty()
                .allMatch(cat -> "Y".equals(cat.getIsActive()));
    }

    @Test
    @DisplayName("findById возвращает категорию по ID")
    void findById_returnsCategory() {
        Category category = categoryRepository.findById(1L).orElse(null);

        assertThat(category)
                .isNotNull()
                .satisfies(cat -> {
                    assertThat(cat.getName()).isEqualTo("Зарплата");
                    assertThat(cat.getType()).isEqualTo("INCOME");
                });
    }
}
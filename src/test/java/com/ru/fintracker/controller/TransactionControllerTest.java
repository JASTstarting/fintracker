package com.ru.fintracker.controller;

import com.ru.fintracker.service.CategoryService;
import com.ru.fintracker.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TransactionController — тесты HTTP-эндпоинтов")
class TransactionControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TransactionService transactionService = mock(TransactionService.class);
        CategoryService categoryService = mock(CategoryService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TransactionController(transactionService, categoryService))
                .build();
    }

    @Test
    @DisplayName("GET /transactions/new возвращает форму с кодом 200")
    void showCreateForm_returnsOk() throws Exception {
        mockMvc.perform(get("/transactions/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"))
                .andExpect(model().attribute("pageContent", "transaction-form"));
    }

    @Test
    @DisplayName("GET /transactions/new?type=INCOME предустанавливает тип в форме")
    void showCreateForm_withTypeParam_prefillsType() throws Exception {
        mockMvc.perform(get("/transactions/new").param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("form"));
    }

    @Test
    @DisplayName("POST /transactions с ошибкой валидации возвращает форму с ошибками")
    void createTransaction_withValidationError_returnsForm() throws Exception {
        mockMvc.perform(post("/transactions")
                        .param("amount", "-100")
                        .param("type", "EXPENSE")
                        .param("transactionDate", "2024-01-01")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("layout"));
    }
}
package com.ru.fintracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FintrackerApplication — smoke-тест загрузки контекста")
class FintrackerApplicationTests {

	@Test
	void contextLoads() {
	}
}
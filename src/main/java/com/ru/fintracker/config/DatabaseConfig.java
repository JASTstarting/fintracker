package com.ru.fintracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

@Configuration
@EnableJdbcRepositories(basePackages = "com.ru.fintracker.repository")
public class DatabaseConfig {

    @Bean
    public JdbcClient jdbcClient(DataSource dataSource) {
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        return JdbcClient.create(namedJdbcTemplate);
    }
}
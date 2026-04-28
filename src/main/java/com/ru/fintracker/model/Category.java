package com.ru.fintracker.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("CATEGORIES")
public class Category {

    @Id
    private Long id;

    @Column("NAME")
    private String name;

    @Column("TYPE")
    private String type;

    @Column("IS_ACTIVE")
    private String isActive;
}
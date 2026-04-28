package com.ru.fintracker.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FilterParams {
    private String comment;
    private String type;
    private Long categoryId;
    private LocalDate from;
    private LocalDate to;
    private int page = 0;
    private int size = 10;

    public int getOffset() {
        return page * size;
    }
}
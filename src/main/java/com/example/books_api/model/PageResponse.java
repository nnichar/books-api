package com.example.books_api.model;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,   // current page
        int size,     // page size
        boolean first,
        boolean last,
        String sort
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        String sortStr = page.getSort().isSorted() ? page.getSort().toString() : "";
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast(),
                sortStr
        );
    }
}
package com.moru.backend.global.common.dto;

import java.util.List;

public record ScrollResponse<T>(
        List<T> content,
        boolean hasNext
) {
    public static <T> ScrollResponse<T> of(List<T> content, boolean hasNext) {
        return new ScrollResponse<T>(content, hasNext);
    }
}

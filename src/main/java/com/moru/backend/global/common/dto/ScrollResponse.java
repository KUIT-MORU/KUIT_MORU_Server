package com.moru.backend.global.common.dto;

import java.util.List;

public record ScrollResponse<T, C>(
        List<T> content,
        boolean hasNext,
        C nextCursor
) {
    public static <T, C> ScrollResponse<T,C> of(List<T> content, boolean hasNext, C nextCursor) {
        return new ScrollResponse<T, C>(content, hasNext, nextCursor);
    }
}

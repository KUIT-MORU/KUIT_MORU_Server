package com.moru.backend.domain.routine.dto.request;

import com.moru.backend.domain.routine.domain.search.SortType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "루틴 검색 요청")
public class RoutineSearchRequest {
    @Schema(description = "루틴명 검색어", example = "아침")
    @Size(max = 5, message = "검색어는 5자를 초과할 수 없습니다.")
    private String titleKeyword;

    @Schema(description = "태그명 검색어 목록", example = "[\"운동\", \"건강\"]")
    private List<String> tagNames;

    @Schema(description = "정렬 방식 (LATEST : 최신순, POPULAR : 인기순)", example = "LATEST")
    private SortType sortType = SortType.LATEST;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;
}

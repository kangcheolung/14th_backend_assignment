package com.cotato.backend14th.global.common.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 목록 API 의 페이지 응답 형식. 모든 목록이 같은 필드(content, page, size, totalElements, totalPages)를 쓰도록 맞춘다.
 * Spring 의 {@code Page} 를 그대로 내리면 불필요한 필드가 많아서 필요한 값만 옮긴다.
 */
public record PageResponse<T>(
	@Schema(description = "현재 페이지의 항목")
	List<T> content,

	@Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
	int page,

	@Schema(description = "페이지 크기", example = "10")
	int size,

	@Schema(description = "전체 항목 수", example = "1")
	long totalElements,

	@Schema(description = "전체 페이지 수", example = "1")
	int totalPages
) {

	public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
		return new PageResponse<>(
			content,
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}
}

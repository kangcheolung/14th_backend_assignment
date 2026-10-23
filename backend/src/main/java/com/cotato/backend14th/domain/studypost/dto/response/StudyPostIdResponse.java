package com.cotato.backend14th.domain.studypost.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 모집글 작성·수정 응답. 대상 글의 id 만 내려준다.
 */
public record StudyPostIdResponse(
	@Schema(description = "모집글 id", example = "10")
	Long postId
) {
}

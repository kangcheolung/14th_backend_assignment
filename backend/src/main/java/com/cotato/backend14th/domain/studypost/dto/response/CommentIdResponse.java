package com.cotato.backend14th.domain.studypost.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 댓글 작성·수정 응답. 대상 댓글의 id 만 내려준다.
 */
public record CommentIdResponse(
	@Schema(description = "댓글 id", example = "5")
	Long commentId
) {
}

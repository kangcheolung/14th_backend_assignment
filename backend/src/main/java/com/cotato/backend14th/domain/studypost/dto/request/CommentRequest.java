package com.cotato.backend14th.domain.studypost.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 댓글(스터디 신청) 작성·수정 요청. 내용 1~500자는 API 명세와 같다.
 */
public record CommentRequest(
	@Schema(description = "댓글 내용 (1~500자)", example = "안녕하세요! 알고리즘 입문 중인데 참여하고 싶어요.")
	@NotBlank(message = "내용을 입력해 주세요.")
	@Size(max = 500, message = "내용은 500자 이하로 입력해 주세요.")
	String content
) {
}

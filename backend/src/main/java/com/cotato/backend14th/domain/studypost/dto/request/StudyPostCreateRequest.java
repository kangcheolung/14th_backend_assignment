package com.cotato.backend14th.domain.studypost.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 모집글 작성 요청. 제목 1~50자, 내용 1~2000자는 API 명세와 같다.
 */
public record StudyPostCreateRequest(
	@Schema(description = "제목 (1~50자)", example = "알고리즘 스터디 모집")
	@NotBlank(message = "제목을 입력해 주세요.")
	@Size(max = 50, message = "제목은 50자 이하로 입력해 주세요.")
	String title,

	@Schema(description = "내용 (1~2000자)", example = "주 2회 백준 풀이...")
	@NotBlank(message = "내용을 입력해 주세요.")
	@Size(max = 2000, message = "내용은 2000자 이하로 입력해 주세요.")
	String content
) {
}

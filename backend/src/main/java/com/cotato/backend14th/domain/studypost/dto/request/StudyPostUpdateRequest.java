package com.cotato.backend14th.domain.studypost.dto.request;

import com.cotato.backend14th.domain.studypost.entity.StudyStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 모집글 수정 요청. 바꿀 필드만 보내고, 보내지 않은 필드는 그대로 둔다.
 * 보낸 필드는 작성 요청과 같은 제약(제목 1~50자, 내용 1~2000자)을 받는다.
 */
public record StudyPostUpdateRequest(
	@Schema(description = "제목 (1~50자, 생략하면 그대로)", example = "수정된 제목")
	@Size(max = 50, message = "제목은 50자 이하로 입력해 주세요.")
	@Pattern(regexp = "(?s).*\\S.*", message = "제목을 입력해 주세요.")
	String title,

	@Schema(description = "내용 (1~2000자, 생략하면 그대로)", example = "수정된 내용")
	@Size(max = 2000, message = "내용은 2000자 이하로 입력해 주세요.")
	@Pattern(regexp = "(?s).*\\S.*", message = "내용을 입력해 주세요.")
	String content,

	@Schema(description = "모집 상태 (RECRUITING 또는 CLOSED, 생략하면 그대로)", example = "CLOSED")
	StudyStatus studyStatus
) {
}

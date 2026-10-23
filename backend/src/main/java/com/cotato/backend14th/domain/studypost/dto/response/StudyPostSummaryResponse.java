package com.cotato.backend14th.domain.studypost.dto.response;

import java.time.LocalDateTime;

import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.user.dto.response.AuthorResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 모집글 목록의 한 줄. 본문은 상세에서만 내려준다.
 */
public record StudyPostSummaryResponse(
	@Schema(description = "모집글 id", example = "10")
	Long postId,

	@Schema(description = "제목", example = "알고리즘 스터디 모집")
	String title,

	@Schema(description = "모집 상태", example = "RECRUITING")
	StudyStatus studyStatus,

	@Schema(description = "작성자")
	AuthorResponse author,

	@Schema(description = "댓글(신청) 수", example = "3")
	long commentCount,

	@Schema(description = "작성 시각")
	LocalDateTime createdAt
) {

	public static StudyPostSummaryResponse of(StudyPost post, long commentCount) {
		return new StudyPostSummaryResponse(
			post.getId(),
			post.getTitle(),
			post.getStudyStatus(),
			AuthorResponse.from(post.getUser()),
			commentCount,
			post.getCreatedAt()
		);
	}
}

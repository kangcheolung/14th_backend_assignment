package com.cotato.backend14th.domain.studypost.dto.response;

import java.time.LocalDateTime;

import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.user.dto.response.AuthorResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 모집글 상세. 로그인하지 않았으면 {@code isAuthor} 는 항상 false 이다.
 */
public record StudyPostDetailResponse(
	@Schema(description = "모집글 id", example = "10")
	Long postId,

	@Schema(description = "제목", example = "알고리즘 스터디 모집")
	String title,

	@Schema(description = "내용", example = "주 2회 백준 풀이...")
	String content,

	@Schema(description = "모집 상태", example = "RECRUITING")
	StudyStatus studyStatus,

	@Schema(description = "작성자")
	AuthorResponse author,

	@Schema(description = "내가 쓴 글인지 여부", example = "true")
	@JsonProperty("isAuthor") // 명세의 필드 이름. 지정하지 않으면 boolean 접근자 규칙 때문에 author 로 나갈 수 있다
	boolean isAuthor,

	@Schema(description = "작성 시각")
	LocalDateTime createdAt,

	@Schema(description = "수정 시각")
	LocalDateTime updatedAt
) {

	public static StudyPostDetailResponse of(StudyPost post, Long currentUserId) {
		return new StudyPostDetailResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getStudyStatus(),
			AuthorResponse.from(post.getUser()),
			post.isWrittenBy(currentUserId),
			post.getCreatedAt(),
			post.getUpdatedAt()
		);
	}
}

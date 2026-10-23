package com.cotato.backend14th.domain.studypost.dto.response;

import java.time.LocalDateTime;

import com.cotato.backend14th.domain.studypost.entity.StudyPostComment;
import com.cotato.backend14th.domain.user.dto.response.AuthorResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 댓글 목록의 한 줄. 로그인하지 않았으면 {@code isAuthor} 는 항상 false 이다.
 */
public record CommentResponse(
	@Schema(description = "댓글 id", example = "5")
	Long commentId,

	@Schema(description = "댓글 내용", example = "안녕하세요! 참여하고 싶어요.")
	String content,

	@Schema(description = "작성자")
	AuthorResponse author,

	@Schema(description = "내가 쓴 댓글인지 여부", example = "false")
	@JsonProperty("isAuthor") // 명세의 필드 이름. 지정하지 않으면 boolean 접근자 규칙 때문에 author 로 나갈 수 있다
	boolean isAuthor,

	@Schema(description = "작성 시각")
	LocalDateTime createdAt
) {

	public static CommentResponse of(StudyPostComment comment, Long currentUserId) {
		return new CommentResponse(
			comment.getId(),
			comment.getContent(),
			AuthorResponse.from(comment.getUser()),
			comment.isWrittenBy(currentUserId),
			comment.getCreatedAt()
		);
	}
}

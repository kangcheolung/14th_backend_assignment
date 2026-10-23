package com.cotato.backend14th.domain.studypost.entity;

import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모집글에 달린 댓글. 이 서비스에서는 댓글이 곧 스터디 신청이다.
 * 글을 지우면 댓글도 함께 지워지지만, 그 순서는 Service 가 정한다.
 */
@Entity
@Table(name = "study_post_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPostComment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "study_post_id", nullable = false)
	private StudyPost studyPost;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 500)
	private String content;

	private StudyPostComment(StudyPost studyPost, User user, String content) {
		this.studyPost = studyPost;
		this.user = user;
		this.content = content;
	}

	public static StudyPostComment create(StudyPost studyPost, User user, String content) {
		return new StudyPostComment(studyPost, user, content);
	}

	public void changeContent(String content) {
		this.content = content;
	}

	public boolean isWrittenBy(Long userId) {
		return userId != null && user.getId().equals(userId);
	}
}

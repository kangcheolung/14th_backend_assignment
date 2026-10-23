package com.cotato.backend14th.domain.studypost.entity;

import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 스터디 모집글. 작성자, 제목, 내용, 모집 상태를 가진다.
 * 작성자 확인 같은 권한 판단은 Service 의 몫이고, 여기서는 작성자가 누구인지만 답한다.
 */
@Entity
@Table(name = "study_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPost extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 50)
	private String title;

	@Column(nullable = false, length = 2000)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "study_status", nullable = false, length = 10)
	private StudyStatus studyStatus;

	private StudyPost(User user, String title, String content) {
		this.user = user;
		this.title = title;
		this.content = content;
		this.studyStatus = StudyStatus.RECRUITING; // 처음 만들면 항상 모집 중이다
	}

	public static StudyPost create(User user, String title, String content) {
		return new StudyPost(user, title, content);
	}

	/**
	 * 보낸 값만 바꾼다. {@code null} 은 "바꾸지 않음"이다.
	 */
	public void edit(String title, String content, StudyStatus studyStatus) {
		if (title != null) {
			this.title = title;
		}
		if (content != null) {
			this.content = content;
		}
		if (studyStatus != null) {
			this.studyStatus = studyStatus;
		}
	}

	public boolean isClosed() {
		return studyStatus == StudyStatus.CLOSED;
	}

	public boolean isWrittenBy(Long userId) {
		return userId != null && user.getId().equals(userId);
	}
}

package com.cotato.backend14th.domain.studypost.repository;

import org.springframework.data.jpa.domain.Specification;

import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.user.entity.Part;

/**
 * 모집글 목록 검색 조건. 조건마다 하나씩 만들어 {@code and} 로 이어 붙이면, 값이 없는 조건은 건너뛰고
 * 있는 조건은 모두 만족하는 글만 나온다. 값이 없을 때 {@code null} 을 돌려주면 Spring 이 그 조건을 무시한다.
 */
public final class StudyPostSpecifications {

	private StudyPostSpecifications() {
	}

	/** 제목 또는 내용에 키워드가 들어 있는 글. */
	public static Specification<StudyPost> keywordContains(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}
		String pattern = "%" + keyword.trim() + "%";
		return (root, query, builder) -> builder.or(
			builder.like(root.get("title"), pattern),
			builder.like(root.get("content"), pattern)
		);
	}

	public static Specification<StudyPost> statusEquals(StudyStatus status) {
		if (status == null) {
			return null;
		}
		return (root, query, builder) -> builder.equal(root.get("studyStatus"), status);
	}

	/** 글 작성자의 파트가 같은 글. 작성자(User)와 조인해서 거른다. */
	public static Specification<StudyPost> authorPartEquals(Part part) {
		if (part == null) {
			return null;
		}
		return (root, query, builder) -> builder.equal(root.join("user").get("part"), part);
	}
}

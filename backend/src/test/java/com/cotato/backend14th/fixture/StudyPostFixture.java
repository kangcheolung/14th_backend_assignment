package com.cotato.backend14th.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyPostComment;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.user.entity.User;

/**
 * 모집글·댓글 테스트 데이터. 엔티티의 id 는 DB 가 채우는 값이라 리플렉션으로 넣는다.
 */
public class StudyPostFixture {

	public static final String TITLE = "알고리즘 스터디 모집";
	public static final String CONTENT = "주 2회 백준 풀이...";

	public static StudyPost post(Long id, User author) {
		StudyPost post = StudyPost.create(author, TITLE, CONTENT);
		ReflectionTestUtils.setField(post, "id", id);
		return post;
	}

	public static StudyPost closedPost(Long id, User author) {
		StudyPost post = post(id, author);
		post.edit(null, null, StudyStatus.CLOSED);
		return post;
	}

	public static StudyPostComment comment(Long id, StudyPost post, User author, String content) {
		StudyPostComment comment = StudyPostComment.create(post, author, content);
		ReflectionTestUtils.setField(comment, "id", id);
		return comment;
	}
}

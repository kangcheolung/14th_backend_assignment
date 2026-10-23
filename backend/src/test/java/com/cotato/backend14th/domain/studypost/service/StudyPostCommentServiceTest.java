package com.cotato.backend14th.domain.studypost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.cotato.backend14th.domain.studypost.dto.request.CommentRequest;
import com.cotato.backend14th.domain.studypost.dto.response.CommentIdResponse;
import com.cotato.backend14th.domain.studypost.dto.response.CommentResponse;
import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyPostComment;
import com.cotato.backend14th.domain.studypost.repository.StudyPostCommentRepository;
import com.cotato.backend14th.domain.studypost.repository.StudyPostRepository;
import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.domain.user.service.UserService;
import com.cotato.backend14th.fixture.StudyPostFixture;
import com.cotato.backend14th.fixture.UserFixture;
import com.cotato.backend14th.global.common.response.PageResponse;
import com.cotato.backend14th.global.exception.Backend14thException;
import com.cotato.backend14th.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class StudyPostCommentServiceTest {

	private static final Long POST_ID = 10L;
	private static final Long COMMENT_ID = 5L;
	private static final Long POST_AUTHOR_ID = 1L;
	private static final Long COMMENTER_ID = 2L;
	private static final Long OTHER_USER_ID = 3L;

	@Mock
	private StudyPostRepository studyPostRepository;

	@Mock
	private StudyPostCommentRepository commentRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private StudyPostCommentService commentService;

	private final User postAuthor = UserFixture.user(POST_AUTHOR_ID);
	private final User commenter = UserFixture.user(COMMENTER_ID);
	private final StudyPost post = StudyPostFixture.post(POST_ID, postAuthor);

	@Test
	@DisplayName("모집 중인 글에 댓글을 달면 저장하고 댓글 id 를 돌려준다")
	void create() {
		// Given
		given(studyPostRepository.findById(POST_ID)).willReturn(Optional.of(post));
		given(userService.getUser(COMMENTER_ID)).willReturn(commenter);
		given(commentRepository.save(any(StudyPostComment.class)))
			.willReturn(StudyPostFixture.comment(COMMENT_ID, post, commenter, "참여하고 싶어요"));

		// When
		CommentIdResponse response = commentService.create(POST_ID, COMMENTER_ID, new CommentRequest("참여하고 싶어요"));

		// Then
		assertThat(response.commentId()).isEqualTo(COMMENT_ID);
		ArgumentCaptor<StudyPostComment> saved = ArgumentCaptor.forClass(StudyPostComment.class);
		verify(commentRepository).save(saved.capture());
		assertThat(saved.getValue().getStudyPost()).isSameAs(post);
		assertThat(saved.getValue().getContent()).isEqualTo("참여하고 싶어요");
	}

	@Test
	@DisplayName("모집이 마감된 글에 댓글을 달면 STUDY_POST_CLOSED 이고 저장하지 않는다")
	void createOnClosedPost() {
		// Given
		given(studyPostRepository.findById(POST_ID))
			.willReturn(Optional.of(StudyPostFixture.closedPost(POST_ID, postAuthor)));

		// When & Then
		assertThatThrownBy(() -> commentService.create(POST_ID, COMMENTER_ID, new CommentRequest("신청합니다")))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_POST_CLOSED);
		verify(commentRepository, never()).save(any());
	}

	@Test
	@DisplayName("없는 글에 댓글을 달면 STUDY_POST_NOT_FOUND 이다")
	void createOnMissingPost() {
		// Given
		given(studyPostRepository.findById(POST_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> commentService.create(POST_ID, COMMENTER_ID, new CommentRequest("신청합니다")))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_POST_NOT_FOUND);
	}

	@Test
	@DisplayName("댓글 목록은 오래된 순으로 요청하고 내가 쓴 댓글에만 isAuthor 가 true 이다")
	void getList() {
		// Given
		StudyPostComment mine = StudyPostFixture.comment(1L, post, commenter, "내 댓글");
		StudyPostComment others = StudyPostFixture.comment(2L, post, postAuthor, "남의 댓글");
		given(studyPostRepository.existsById(POST_ID)).willReturn(true);
		given(commentRepository.findByStudyPostId(any(), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of(mine, others), PageRequest.of(0, 20), 2));

		// When
		PageResponse<CommentResponse> response = commentService.getList(POST_ID, COMMENTER_ID, 0, 20);

		// Then
		assertThat(response.content()).extracting(CommentResponse::commentId).containsExactly(1L, 2L);
		assertThat(response.content()).extracting(CommentResponse::isAuthor).containsExactly(true, false);
		assertThat(response.content().get(0).author().nickname()).isEqualTo("길동이");
		assertThat(response.size()).isEqualTo(20);
		assertThat(response.totalElements()).isEqualTo(2);

		ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
		verify(commentRepository).findByStudyPostId(any(), pageable.capture());
		assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isAscending()).isTrue();
	}

	@Test
	@DisplayName("로그인하지 않고 댓글 목록을 보면 모든 댓글의 isAuthor 가 false 이다")
	void getListAnonymous() {
		// Given
		given(studyPostRepository.existsById(POST_ID)).willReturn(true);
		given(commentRepository.findByStudyPostId(any(), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of(StudyPostFixture.comment(1L, post, commenter, "댓글")),
				PageRequest.of(0, 20), 1));

		// When
		PageResponse<CommentResponse> response = commentService.getList(POST_ID, null, 0, 20);

		// Then
		assertThat(response.content()).extracting(CommentResponse::isAuthor).containsExactly(false);
	}

	@Test
	@DisplayName("없는 글의 댓글 목록을 조회하면 STUDY_POST_NOT_FOUND 이다")
	void getListMissingPost() {
		// Given
		given(studyPostRepository.existsById(POST_ID)).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> commentService.getList(POST_ID, null, 0, 20))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_POST_NOT_FOUND);
	}

	@Test
	@DisplayName("댓글 목록의 페이지가 음수이거나 크기가 0 또는 100 초과면 INVALID_INPUT 이다")
	void getListInvalidPaging() {
		assertInvalidInput(() -> commentService.getList(POST_ID, null, -1, 20));
		assertInvalidInput(() -> commentService.getList(POST_ID, null, 0, 0));
		assertInvalidInput(() -> commentService.getList(POST_ID, null, 0, 101));
	}

	@Test
	@DisplayName("댓글 작성자가 수정하면 내용이 바뀐다")
	void update() {
		// Given
		StudyPostComment comment = StudyPostFixture.comment(COMMENT_ID, post, commenter, "처음 신청 메시지");
		given(commentRepository.findWithUserById(COMMENT_ID)).willReturn(Optional.of(comment));

		// When
		CommentIdResponse response = commentService.update(COMMENT_ID, COMMENTER_ID, new CommentRequest("수정한 신청 메시지"));

		// Then
		assertThat(response.commentId()).isEqualTo(COMMENT_ID);
		assertThat(comment.getContent()).isEqualTo("수정한 신청 메시지");
	}

	@Test
	@DisplayName("댓글 작성자가 아니면 수정할 때 NOT_COMMENT_AUTHOR 이고 내용은 그대로다")
	void updateByOther() {
		// Given
		StudyPostComment comment = StudyPostFixture.comment(COMMENT_ID, post, commenter, "처음 신청 메시지");
		given(commentRepository.findWithUserById(COMMENT_ID)).willReturn(Optional.of(comment));

		// When & Then
		assertThatThrownBy(() -> commentService.update(COMMENT_ID, OTHER_USER_ID, new CommentRequest("가로챈 내용")))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_AUTHOR);
		assertThat(comment.getContent()).isEqualTo("처음 신청 메시지");
	}

	@Test
	@DisplayName("없는 댓글을 수정하면 COMMENT_NOT_FOUND 이다")
	void updateNotFound() {
		// Given
		given(commentRepository.findWithUserById(COMMENT_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> commentService.update(COMMENT_ID, COMMENTER_ID, new CommentRequest("내용")))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
	}

	@Test
	@DisplayName("댓글 작성자가 삭제하면 댓글을 지운다")
	void delete() {
		// Given
		StudyPostComment comment = StudyPostFixture.comment(COMMENT_ID, post, commenter, "댓글");
		given(commentRepository.findWithUserById(COMMENT_ID)).willReturn(Optional.of(comment));

		// When
		commentService.delete(COMMENT_ID, COMMENTER_ID);

		// Then
		verify(commentRepository).delete(comment);
	}

	@Test
	@DisplayName("댓글 작성자가 아니면 삭제할 때 NOT_COMMENT_AUTHOR 이고 지우지 않는다")
	void deleteByOther() {
		// Given
		given(commentRepository.findWithUserById(COMMENT_ID))
			.willReturn(Optional.of(StudyPostFixture.comment(COMMENT_ID, post, commenter, "댓글")));

		// When & Then
		assertThatThrownBy(() -> commentService.delete(COMMENT_ID, OTHER_USER_ID))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_COMMENT_AUTHOR);
		verify(commentRepository, never()).delete(any(StudyPostComment.class));
	}

	private static void assertInvalidInput(Runnable action) {
		assertThatThrownBy(action::run)
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
	}
}

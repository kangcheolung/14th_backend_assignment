package com.cotato.backend14th.domain.studypost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.cotato.backend14th.domain.studypost.dto.request.StudyPostCreateRequest;
import com.cotato.backend14th.domain.studypost.dto.request.StudyPostUpdateRequest;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostDetailResponse;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostIdResponse;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostSummaryResponse;
import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.studypost.repository.StudyPostCommentRepository;
import com.cotato.backend14th.domain.studypost.repository.StudyPostRepository;
import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.domain.user.service.UserService;
import com.cotato.backend14th.fixture.StudyPostFixture;
import com.cotato.backend14th.fixture.UserFixture;
import com.cotato.backend14th.global.common.response.PageResponse;
import com.cotato.backend14th.global.exception.Backend14thException;
import com.cotato.backend14th.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class StudyPostServiceTest {

	private static final Long AUTHOR_ID = 1L;
	private static final Long OTHER_USER_ID = 2L;
	private static final Long POST_ID = 10L;

	@Mock
	private StudyPostRepository studyPostRepository;

	@Mock
	private StudyPostCommentRepository commentRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private StudyPostService studyPostService;

	private final User author = UserFixture.user(AUTHOR_ID);

	@Test
	@DisplayName("모집글을 작성하면 모집 중 상태로 저장하고 id 를 돌려준다")
	void create() {
		// Given
		given(userService.getUser(AUTHOR_ID)).willReturn(author);
		given(studyPostRepository.save(any(StudyPost.class)))
			.willReturn(StudyPostFixture.post(POST_ID, author));

		// When
		StudyPostIdResponse response = studyPostService.create(
			AUTHOR_ID, new StudyPostCreateRequest(StudyPostFixture.TITLE, StudyPostFixture.CONTENT));

		// Then
		assertThat(response.postId()).isEqualTo(POST_ID);
		ArgumentCaptor<StudyPost> saved = ArgumentCaptor.forClass(StudyPost.class);
		verify(studyPostRepository).save(saved.capture());
		assertThat(saved.getValue().getStudyStatus()).isEqualTo(StudyStatus.RECRUITING);
		assertThat(saved.getValue().getUser()).isSameAs(author);
	}

	@Test
	@DisplayName("목록을 조회하면 최신순으로 요청하고 글마다 댓글 수와 작성자를 담아 돌려준다")
	void getList() {
		// Given
		StudyPost first = StudyPostFixture.post(11L, author);
		StudyPost second = StudyPostFixture.post(10L, author);
		given(studyPostRepository.findAll(any(Specification.class), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 10), 2));
		given(commentRepository.countByStudyPostIds(anyCollection()))
			.willReturn(List.of(commentCount(11L, 3L)));

		// When
		PageResponse<StudyPostSummaryResponse> response =
			studyPostService.getList("알고리즘", StudyStatus.RECRUITING, Part.BE, 0, 10);

		// Then
		assertThat(response.content()).extracting(StudyPostSummaryResponse::postId).containsExactly(11L, 10L);
		assertThat(response.content()).extracting(StudyPostSummaryResponse::commentCount).containsExactly(3L, 0L);
		assertThat(response.content().get(0).author().nickname()).isEqualTo("길동이");
		assertThat(response.page()).isZero();
		assertThat(response.size()).isEqualTo(10);
		assertThat(response.totalElements()).isEqualTo(2);
		assertThat(response.totalPages()).isEqualTo(1);

		ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
		verify(studyPostRepository).findAll(any(Specification.class), pageable.capture());
		assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();
	}

	@Test
	@DisplayName("조건에 맞는 글이 없으면 빈 목록을 돌려주고 댓글 수는 조회하지 않는다")
	void getListEmpty() {
		// Given
		given(studyPostRepository.findAll(any(Specification.class), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

		// When
		PageResponse<StudyPostSummaryResponse> response = studyPostService.getList(null, null, null, 0, 10);

		// Then
		assertThat(response.content()).isEmpty();
		assertThat(response.totalElements()).isZero();
		verify(commentRepository, never()).countByStudyPostIds(anyCollection());
	}

	@Test
	@DisplayName("페이지가 음수이거나 크기가 0 또는 100 초과면 INVALID_INPUT 이다")
	void getListInvalidPaging() {
		assertInvalidInput(() -> studyPostService.getList(null, null, null, -1, 10));
		assertInvalidInput(() -> studyPostService.getList(null, null, null, 0, 0));
		assertInvalidInput(() -> studyPostService.getList(null, null, null, 0, 101));
	}

	@Test
	@DisplayName("상세를 조회하면 작성자 본인에게만 isAuthor 가 true 이다")
	void getIsAuthor() {
		// Given
		given(studyPostRepository.findWithUserById(POST_ID))
			.willReturn(Optional.of(StudyPostFixture.post(POST_ID, author)));

		// When & Then
		assertThat(studyPostService.get(POST_ID, AUTHOR_ID).isAuthor()).isTrue();
		assertThat(studyPostService.get(POST_ID, OTHER_USER_ID).isAuthor()).isFalse();
		assertThat(studyPostService.get(POST_ID, null).isAuthor()).isFalse();
	}

	@Test
	@DisplayName("상세를 조회하면 제목, 내용, 모집 상태, 작성자의 기수와 파트를 돌려준다")
	void getDetail() {
		// Given
		given(studyPostRepository.findWithUserById(POST_ID))
			.willReturn(Optional.of(StudyPostFixture.post(POST_ID, author)));

		// When
		StudyPostDetailResponse response = studyPostService.get(POST_ID, null);

		// Then
		assertThat(response.title()).isEqualTo(StudyPostFixture.TITLE);
		assertThat(response.content()).isEqualTo(StudyPostFixture.CONTENT);
		assertThat(response.studyStatus()).isEqualTo(StudyStatus.RECRUITING);
		assertThat(response.author().generation()).isEqualTo(14);
		assertThat(response.author().part()).isEqualTo(Part.BE);
	}

	@Test
	@DisplayName("없는 글을 조회하면 STUDY_POST_NOT_FOUND 이다")
	void getNotFound() {
		// Given
		given(studyPostRepository.findWithUserById(POST_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> studyPostService.get(POST_ID, null))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_POST_NOT_FOUND);
	}

	@Test
	@DisplayName("작성자가 일부 필드만 보내 수정하면 보낸 필드만 바뀐다")
	void updatePartially() {
		// Given
		StudyPost post = StudyPostFixture.post(POST_ID, author);
		given(studyPostRepository.findWithUserById(POST_ID)).willReturn(Optional.of(post));

		// When
		StudyPostIdResponse response = studyPostService.update(
			POST_ID, AUTHOR_ID, new StudyPostUpdateRequest("수정된 제목", null, null));

		// Then
		assertThat(response.postId()).isEqualTo(POST_ID);
		assertThat(post.getTitle()).isEqualTo("수정된 제목");
		assertThat(post.getContent()).isEqualTo(StudyPostFixture.CONTENT);
		assertThat(post.getStudyStatus()).isEqualTo(StudyStatus.RECRUITING);
	}

	@Test
	@DisplayName("작성자가 모집 상태를 바꾸면 마감과 모집 중을 오갈 수 있다")
	void updateStatus() {
		// Given
		StudyPost post = StudyPostFixture.post(POST_ID, author);
		given(studyPostRepository.findWithUserById(POST_ID)).willReturn(Optional.of(post));

		// When & Then
		studyPostService.update(POST_ID, AUTHOR_ID, new StudyPostUpdateRequest(null, null, StudyStatus.CLOSED));
		assertThat(post.isClosed()).isTrue();
		studyPostService.update(POST_ID, AUTHOR_ID, new StudyPostUpdateRequest(null, null, StudyStatus.RECRUITING));
		assertThat(post.isClosed()).isFalse();
	}

	@Test
	@DisplayName("작성자가 아닌 회원이 수정하면 NOT_POST_AUTHOR 이고 글은 그대로다")
	void updateByOther() {
		// Given
		StudyPost post = StudyPostFixture.post(POST_ID, author);
		given(studyPostRepository.findWithUserById(POST_ID)).willReturn(Optional.of(post));

		// When & Then
		assertThatThrownBy(() -> studyPostService.update(
			POST_ID, OTHER_USER_ID, new StudyPostUpdateRequest("가로챈 제목", null, null)))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_POST_AUTHOR);
		assertThat(post.getTitle()).isEqualTo(StudyPostFixture.TITLE);
	}

	@Test
	@DisplayName("없는 글을 수정하면 STUDY_POST_NOT_FOUND 이다")
	void updateNotFound() {
		// Given
		given(studyPostRepository.findWithUserById(POST_ID)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> studyPostService.update(
			POST_ID, AUTHOR_ID, new StudyPostUpdateRequest("제목", null, null)))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_POST_NOT_FOUND);
	}

	@Test
	@DisplayName("작성자가 삭제하면 댓글을 먼저 지우고 글을 지운다")
	void delete() {
		// Given
		StudyPost post = StudyPostFixture.post(POST_ID, author);
		given(studyPostRepository.findWithUserById(POST_ID)).willReturn(Optional.of(post));

		// When
		studyPostService.delete(POST_ID, AUTHOR_ID);

		// Then
		InOrder inOrder = inOrder(commentRepository, studyPostRepository);
		inOrder.verify(commentRepository).deleteByStudyPostId(POST_ID);
		inOrder.verify(studyPostRepository).delete(post);
	}

	@Test
	@DisplayName("작성자가 아닌 회원이 삭제하면 NOT_POST_AUTHOR 이고 아무것도 지우지 않는다")
	void deleteByOther() {
		// Given
		given(studyPostRepository.findWithUserById(POST_ID))
			.willReturn(Optional.of(StudyPostFixture.post(POST_ID, author)));

		// When & Then
		assertThatThrownBy(() -> studyPostService.delete(POST_ID, OTHER_USER_ID))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_POST_AUTHOR);
		verify(commentRepository, never()).deleteByStudyPostId(any());
		verify(studyPostRepository, never()).delete(any(StudyPost.class));
	}

	private static StudyPostCommentRepository.CommentCount commentCount(Long postId, long count) {
		return new StudyPostCommentRepository.CommentCount() {
			@Override
			public Long getPostId() {
				return postId;
			}

			@Override
			public long getCommentCount() {
				return count;
			}
		};
	}

	private static void assertInvalidInput(Runnable action) {
		assertThatThrownBy(action::run)
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
	}
}

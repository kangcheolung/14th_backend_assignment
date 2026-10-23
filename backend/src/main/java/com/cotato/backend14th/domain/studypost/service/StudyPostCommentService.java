package com.cotato.backend14th.domain.studypost.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.backend14th.domain.studypost.dto.request.CommentRequest;
import com.cotato.backend14th.domain.studypost.dto.response.CommentIdResponse;
import com.cotato.backend14th.domain.studypost.dto.response.CommentResponse;
import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyPostComment;
import com.cotato.backend14th.domain.studypost.repository.StudyPostCommentRepository;
import com.cotato.backend14th.domain.studypost.repository.StudyPostRepository;
import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.domain.user.service.UserService;
import com.cotato.backend14th.global.common.response.PageResponse;
import com.cotato.backend14th.global.exception.Backend14thException;
import com.cotato.backend14th.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 모집글 댓글(= 스터디 신청)의 작성, 조회, 수정, 삭제. 모집이 마감된 글에는 새 댓글을 쓸 수 없고,
 * 수정과 삭제는 댓글 작성자 본인만 할 수 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyPostCommentService {

	private static final int MAX_PAGE_SIZE = 100;

	private final StudyPostRepository studyPostRepository;
	private final StudyPostCommentRepository commentRepository;
	private final UserService userService;

	@Transactional
	public CommentIdResponse create(Long postId, Long userId, CommentRequest request) {
		StudyPost post = studyPostRepository.findById(postId)
			.orElseThrow(() -> new Backend14thException(ErrorCode.STUDY_POST_NOT_FOUND));
		if (post.isClosed()) {
			throw new Backend14thException(ErrorCode.STUDY_POST_CLOSED);
		}

		User author = userService.getUser(userId);
		StudyPostComment comment = commentRepository.save(StudyPostComment.create(post, author, request.content()));
		return new CommentIdResponse(comment.getId());
	}

	/**
	 * 오래된 순 목록.
	 *
	 * @param currentUserId 로그인하지 않았으면 {@code null}. 이때 isAuthor 는 false 이다.
	 */
	public PageResponse<CommentResponse> getList(Long postId, Long currentUserId, int page, int size) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
			throw new Backend14thException(ErrorCode.INVALID_INPUT);
		}
		if (!studyPostRepository.existsById(postId)) {
			throw new Backend14thException(ErrorCode.STUDY_POST_NOT_FOUND);
		}

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));
		Page<StudyPostComment> comments = commentRepository.findByStudyPostId(postId, pageRequest);
		List<CommentResponse> content = comments.getContent().stream()
			.map(comment -> CommentResponse.of(comment, currentUserId))
			.toList();
		return PageResponse.of(content, comments);
	}

	@Transactional
	public CommentIdResponse update(Long commentId, Long userId, CommentRequest request) {
		StudyPostComment comment = findCommentWrittenBy(commentId, userId);
		comment.changeContent(request.content()); // 변경 감지로 저장된다
		return new CommentIdResponse(comment.getId());
	}

	@Transactional
	public void delete(Long commentId, Long userId) {
		commentRepository.delete(findCommentWrittenBy(commentId, userId));
	}

	private StudyPostComment findCommentWrittenBy(Long commentId, Long userId) {
		StudyPostComment comment = commentRepository.findWithUserById(commentId)
			.orElseThrow(() -> new Backend14thException(ErrorCode.COMMENT_NOT_FOUND));
		if (!comment.isWrittenBy(userId)) {
			throw new Backend14thException(ErrorCode.NOT_COMMENT_AUTHOR);
		}
		return comment;
	}
}

package com.cotato.backend14th.domain.studypost.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.backend14th.domain.studypost.dto.request.StudyPostCreateRequest;
import com.cotato.backend14th.domain.studypost.dto.request.StudyPostUpdateRequest;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostDetailResponse;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostIdResponse;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostSummaryResponse;
import com.cotato.backend14th.domain.studypost.entity.StudyPost;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.studypost.repository.StudyPostCommentRepository;
import com.cotato.backend14th.domain.studypost.repository.StudyPostRepository;
import com.cotato.backend14th.domain.studypost.repository.StudyPostSpecifications;
import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.domain.user.service.UserService;
import com.cotato.backend14th.global.common.response.PageResponse;
import com.cotato.backend14th.global.exception.Backend14thException;
import com.cotato.backend14th.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 스터디 모집글의 작성, 조회(검색), 수정, 삭제. 댓글의 작성·수정·삭제는 {@link StudyPostCommentService} 가 맡는다.
 * 수정과 삭제는 작성자 본인만 할 수 있고, 그 확인은 여기서 한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyPostService {

	private static final int MAX_PAGE_SIZE = 100;

	private final StudyPostRepository studyPostRepository;
	private final StudyPostCommentRepository commentRepository;
	private final UserService userService;

	@Transactional
	public StudyPostIdResponse create(Long userId, StudyPostCreateRequest request) {
		User author = userService.getUser(userId);
		StudyPost post = studyPostRepository.save(StudyPost.create(author, request.title(), request.content()));
		return new StudyPostIdResponse(post.getId());
	}

	/**
	 * 최신순 목록. keyword, status, part 는 모두 선택이고, 함께 주면 모두 만족하는 글만 나온다.
	 * part 는 글 작성자의 파트로 거른다.
	 */
	public PageResponse<StudyPostSummaryResponse> getList(
		String keyword, StudyStatus status, Part part, int page, int size
	) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
			throw new Backend14thException(ErrorCode.INVALID_INPUT);
		}

		Specification<StudyPost> spec = Specification.allOf(
			StudyPostSpecifications.keywordContains(keyword),
			StudyPostSpecifications.statusEquals(status),
			StudyPostSpecifications.authorPartEquals(part)
		);
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
		Page<StudyPost> posts = studyPostRepository.findAll(spec, pageRequest);

		Map<Long, Long> commentCounts = countComments(posts.getContent());
		List<StudyPostSummaryResponse> content = posts.getContent().stream()
			.map(post -> StudyPostSummaryResponse.of(post, commentCounts.getOrDefault(post.getId(), 0L)))
			.toList();
		return PageResponse.of(content, posts);
	}

	/**
	 * @param currentUserId 로그인하지 않았으면 {@code null}. 이때 isAuthor 는 false 이다.
	 */
	public StudyPostDetailResponse get(Long postId, Long currentUserId) {
		return StudyPostDetailResponse.of(findPost(postId), currentUserId);
	}

	@Transactional
	public StudyPostIdResponse update(Long postId, Long userId, StudyPostUpdateRequest request) {
		StudyPost post = findPostWrittenBy(postId, userId);
		post.edit(request.title(), request.content(), request.studyStatus()); // 변경 감지로 저장된다
		return new StudyPostIdResponse(post.getId());
	}

	/**
	 * 글을 지우면 달린 댓글도 함께 지운다. 댓글이 글을 참조하므로 댓글을 먼저 지운다.
	 */
	@Transactional
	public void delete(Long postId, Long userId) {
		StudyPost post = findPostWrittenBy(postId, userId);
		commentRepository.deleteByStudyPostId(post.getId());
		studyPostRepository.delete(post);
	}

	private Map<Long, Long> countComments(Collection<StudyPost> posts) {
		if (posts.isEmpty()) {
			return Map.of();
		}
		List<Long> postIds = posts.stream().map(StudyPost::getId).toList();
		// 쿼리가 글 id 로 묶어서 세기 때문에 id 가 겹치지 않는다
		return commentRepository.countByStudyPostIds(postIds).stream()
			.collect(Collectors.toMap(
				StudyPostCommentRepository.CommentCount::getPostId,
				StudyPostCommentRepository.CommentCount::getCommentCount
			));
	}

	private StudyPost findPost(Long postId) {
		return studyPostRepository.findWithUserById(postId)
			.orElseThrow(() -> new Backend14thException(ErrorCode.STUDY_POST_NOT_FOUND));
	}

	private StudyPost findPostWrittenBy(Long postId, Long userId) {
		StudyPost post = findPost(postId);
		if (!post.isWrittenBy(userId)) {
			throw new Backend14thException(ErrorCode.NOT_POST_AUTHOR);
		}
		return post;
	}
}

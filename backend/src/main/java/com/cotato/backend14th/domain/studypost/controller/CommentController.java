package com.cotato.backend14th.domain.studypost.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.backend14th.domain.studypost.dto.request.CommentRequest;
import com.cotato.backend14th.domain.studypost.dto.response.CommentIdResponse;
import com.cotato.backend14th.domain.studypost.dto.response.CommentResponse;
import com.cotato.backend14th.domain.studypost.service.StudyPostCommentService;
import com.cotato.backend14th.global.common.response.ApiResponse;
import com.cotato.backend14th.global.common.response.PageResponse;
import com.cotato.backend14th.global.common.response.ResponseUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Comment API", description = "모집글 댓글 (스터디 신청)")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

	private final StudyPostCommentService commentService;

	@Operation(summary = "댓글(신청) 작성", description = "로그인한 회원이 모집글에 댓글을 달아요. 모집이 마감된(CLOSED) 글에는 쓸 수 없고 409를 돌려줘요.")
	@PostMapping("/study-posts/{postId}/comments")
	public ResponseEntity<ApiResponse<CommentIdResponse>> create(
		@Parameter(description = "모집글 id", example = "10") @PathVariable Long postId,
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody CommentRequest request
	) {
		return ResponseUtils.created(commentService.create(postId, userId, request));
	}

	@Operation(summary = "댓글 목록 조회", description = "오래된 순이에요. 로그인 없이 호출할 수 있고, 로그인하지 않았으면 isAuthor 는 false 예요.")
	@GetMapping("/study-posts/{postId}/comments")
	public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getList(
		@Parameter(description = "모집글 id", example = "10") @PathVariable Long postId,
		@AuthenticationPrincipal Long userId,
		@Parameter(description = "페이지 번호 (0부터)", example = "0")
		@RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지 크기 (1~100)", example = "20")
		@RequestParam(defaultValue = "20") int size
	) {
		return ResponseUtils.ok(commentService.getList(postId, userId, page, size));
	}

	@Operation(summary = "댓글 수정", description = "댓글 작성자 본인만 할 수 있어요.")
	@PatchMapping("/comments/{commentId}")
	public ResponseEntity<ApiResponse<CommentIdResponse>> update(
		@Parameter(description = "댓글 id", example = "5") @PathVariable Long commentId,
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody CommentRequest request
	) {
		return ResponseUtils.ok(commentService.update(commentId, userId, request));
	}

	@Operation(summary = "댓글 삭제", description = "댓글 작성자 본인만 할 수 있어요. 본문 없이 204로 응답해요.")
	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<Void> delete(
		@Parameter(description = "댓글 id", example = "5") @PathVariable Long commentId,
		@AuthenticationPrincipal Long userId
	) {
		commentService.delete(commentId, userId);
		return ResponseEntity.noContent().build();
	}
}

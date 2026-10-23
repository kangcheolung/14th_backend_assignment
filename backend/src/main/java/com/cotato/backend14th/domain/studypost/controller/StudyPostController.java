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

import com.cotato.backend14th.domain.studypost.dto.request.StudyPostCreateRequest;
import com.cotato.backend14th.domain.studypost.dto.request.StudyPostUpdateRequest;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostDetailResponse;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostIdResponse;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostSummaryResponse;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.studypost.service.StudyPostService;
import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.global.common.response.ApiResponse;
import com.cotato.backend14th.global.common.response.PageResponse;
import com.cotato.backend14th.global.common.response.ResponseUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "StudyPost API", description = "스터디 모집글")
@RestController
@RequestMapping("/api/study-posts")
@RequiredArgsConstructor
public class StudyPostController {

	private final StudyPostService studyPostService;

	@Operation(summary = "모집글 작성", description = "로그인한 회원이 모집글을 써요. 처음 만들면 모집 상태는 RECRUITING 이에요.")
	@PostMapping
	public ResponseEntity<ApiResponse<StudyPostIdResponse>> create(
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody StudyPostCreateRequest request
	) {
		return ResponseUtils.created(studyPostService.create(userId, request));
	}

	@Operation(
		summary = "모집글 목록 조회",
		description = "최신순이에요. keyword(제목·내용), status(모집 상태), part(작성자의 파트)로 거를 수 있고 "
			+ "함께 쓰면 모두 만족하는 글만 나와요. 로그인 없이 호출할 수 있어요."
	)
	@GetMapping
	public ResponseEntity<ApiResponse<PageResponse<StudyPostSummaryResponse>>> getList(
		@Parameter(description = "제목 또는 내용에 포함된 키워드", example = "알고리즘")
		@RequestParam(required = false) String keyword,
		@Parameter(description = "모집 상태. 없으면 전체", example = "RECRUITING")
		@RequestParam(required = false) StudyStatus status,
		@Parameter(description = "작성자의 파트. 없으면 전체", example = "BE")
		@RequestParam(required = false) Part part,
		@Parameter(description = "페이지 번호 (0부터)", example = "0")
		@RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지 크기 (1~100)", example = "10")
		@RequestParam(defaultValue = "10") int size
	) {
		return ResponseUtils.ok(studyPostService.getList(keyword, status, part, page, size));
	}

	@Operation(summary = "모집글 상세 조회", description = "로그인 없이 호출할 수 있어요. 로그인하지 않았으면 isAuthor 는 false 예요.")
	@GetMapping("/{postId}")
	public ResponseEntity<ApiResponse<StudyPostDetailResponse>> get(
		@Parameter(description = "모집글 id", example = "10") @PathVariable Long postId,
		@AuthenticationPrincipal Long userId
	) {
		return ResponseUtils.ok(studyPostService.get(postId, userId));
	}

	@Operation(summary = "모집글 수정", description = "작성자 본인만 할 수 있어요. 바꿀 필드만 보내면 되고, 모집 상태(studyStatus)도 여기서 바꿔요.")
	@PatchMapping("/{postId}")
	public ResponseEntity<ApiResponse<StudyPostIdResponse>> update(
		@Parameter(description = "모집글 id", example = "10") @PathVariable Long postId,
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody StudyPostUpdateRequest request
	) {
		return ResponseUtils.ok(studyPostService.update(postId, userId, request));
	}

	@Operation(summary = "모집글 삭제", description = "작성자 본인만 할 수 있어요. 글에 달린 댓글도 함께 삭제되고, 본문 없이 204로 응답해요.")
	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> delete(
		@Parameter(description = "모집글 id", example = "10") @PathVariable Long postId,
		@AuthenticationPrincipal Long userId
	) {
		studyPostService.delete(postId, userId);
		return ResponseEntity.noContent().build();
	}
}

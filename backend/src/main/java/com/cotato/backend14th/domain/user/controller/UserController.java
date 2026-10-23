package com.cotato.backend14th.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.backend14th.domain.user.dto.response.UserMeResponse;
import com.cotato.backend14th.domain.user.service.UserService;
import com.cotato.backend14th.global.common.response.ApiResponse;
import com.cotato.backend14th.global.common.response.ResponseUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "User API", description = "회원 정보")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "내 정보 조회", description = "로그인한 회원의 정보를 기수, 파트와 함께 돌려줘요. 비밀번호는 포함하지 않아요.")
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserMeResponse>> getMe(@AuthenticationPrincipal Long userId) {
		return ResponseUtils.ok(userService.getMe(userId));
	}
}

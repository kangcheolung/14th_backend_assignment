package com.cotato.backend14th.domain.user.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.backend14th.domain.user.dto.request.LoginRequest;
import com.cotato.backend14th.domain.user.dto.request.SignupRequest;
import com.cotato.backend14th.domain.user.dto.response.LoginResponse;
import com.cotato.backend14th.domain.user.dto.response.SignupResponse;
import com.cotato.backend14th.domain.user.service.AuthService;
import com.cotato.backend14th.global.common.response.ApiResponse;
import com.cotato.backend14th.global.common.response.ResponseUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth API", description = "회원가입, 로그인, 로그아웃")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름, 닉네임, 기수, 파트로 가입해요. 이미 가입된 이메일이면 409를 돌려줘요.")
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
		return ResponseUtils.created(authService.signup(request));
	}

	@Operation(summary = "로그인", description = "이메일과 비밀번호가 맞으면 액세스 토큰을 발급해요. 이후 요청에 Authorization: Bearer 토큰 헤더를 붙여요.")
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		return ResponseUtils.ok(authService.login(request));
	}

	@Operation(summary = "로그아웃", description = "사용한 토큰을 만료 시각까지 무효 처리해요. 이후 같은 토큰으로 요청하면 401이에요. 본문 없이 204로 응답해요.")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
		authService.logout(authorization);
		return ResponseEntity.noContent().build();
	}
}

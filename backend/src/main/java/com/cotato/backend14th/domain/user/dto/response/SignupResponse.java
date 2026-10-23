package com.cotato.backend14th.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 회원가입 응답. 가입된 회원의 id 만 내려준다.
 */
public record SignupResponse(
	@Schema(description = "가입된 회원 id", example = "1")
	Long userId
) {
}

package com.cotato.backend14th.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청. 형식 검증은 하지 않고 비어 있는지만 본다.
 * 형식이 틀린 이메일도 "이메일 또는 비밀번호가 올바르지 않아요"로 응답해 어떤 계정이 있는지 드러내지 않는다.
 */
public record LoginRequest(
	@Schema(description = "이메일", example = "user@gmail.com")
	@NotBlank(message = "이메일을 입력해 주세요.")
	String email,

	@Schema(description = "비밀번호", example = "password1234")
	@NotBlank(message = "비밀번호를 입력해 주세요.")
	String password
) {
}

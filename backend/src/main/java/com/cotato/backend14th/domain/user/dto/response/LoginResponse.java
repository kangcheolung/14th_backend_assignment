package com.cotato.backend14th.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 응답. 클라이언트는 이후 요청에 {@code Authorization: Bearer <accessToken>} 헤더를 붙인다.
 */
public record LoginResponse(
	@Schema(description = "액세스 토큰(JWT)", example = "eyJhbGciOi...")
	String accessToken,

	@Schema(description = "토큰 종류", example = "Bearer")
	String tokenType
) {

	private static final String BEARER = "Bearer";

	public static LoginResponse bearer(String accessToken) {
		return new LoginResponse(accessToken, BEARER);
	}
}

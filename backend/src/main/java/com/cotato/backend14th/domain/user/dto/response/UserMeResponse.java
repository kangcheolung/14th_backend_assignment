package com.cotato.backend14th.domain.user.dto.response;

import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.domain.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 내 정보 응답. 비밀번호는 절대 담지 않는다.
 */
public record UserMeResponse(
	@Schema(description = "회원 id", example = "1")
	Long userId,

	@Schema(description = "이메일", example = "user@gmail.com")
	String email,

	@Schema(description = "이름", example = "홍길동")
	String name,

	@Schema(description = "닉네임", example = "길동이")
	String nickname,

	@Schema(description = "기수", example = "14")
	int generation,

	@Schema(description = "파트", example = "BE")
	Part part
) {

	public static UserMeResponse from(User user) {
		return new UserMeResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getNickname(),
			user.getGeneration(),
			user.getPart()
		);
	}
}

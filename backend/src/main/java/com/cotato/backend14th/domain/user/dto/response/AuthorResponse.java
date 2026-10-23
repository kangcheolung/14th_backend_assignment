package com.cotato.backend14th.domain.user.dto.response;

import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.domain.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 글과 댓글에 붙는 작성자 정보. 이메일 같은 개인정보는 내리지 않고 닉네임, 기수, 파트만 보여준다.
 */
public record AuthorResponse(
	@Schema(description = "닉네임", example = "길동이")
	String nickname,

	@Schema(description = "기수", example = "14")
	int generation,

	@Schema(description = "파트", example = "BE")
	Part part
) {

	public static AuthorResponse from(User user) {
		return new AuthorResponse(user.getNickname(), user.getGeneration(), user.getPart());
	}
}

package com.cotato.backend14th.domain.user.dto.request;

import com.cotato.backend14th.domain.user.entity.Part;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청. 필드 제약은 API 명세와 같아야 한다.
 */
public record SignupRequest(
	@Schema(description = "이메일", example = "user@gmail.com")
	@NotBlank(message = "이메일을 입력해 주세요.")
	@Email(message = "이메일 형식이 올바르지 않아요.")
	@Size(max = 100, message = "이메일은 100자 이하로 입력해 주세요.")
	String email,

	@Schema(description = "비밀번호 (8자 이상)", example = "password1234")
	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 해요.")
	String password,

	@Schema(description = "이름", example = "홍길동")
	@NotBlank(message = "이름을 입력해 주세요.")
	@Size(max = 30, message = "이름은 30자 이하로 입력해 주세요.")
	String name,

	@Schema(description = "닉네임 (2~10자)", example = "길동이")
	@NotBlank(message = "닉네임을 입력해 주세요.")
	@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 해요.")
	String nickname,

	@Schema(description = "기수 (1 이상의 정수)", example = "14")
	@NotNull(message = "기수를 입력해 주세요.")
	@Min(value = 1, message = "기수는 1 이상이어야 해요.")
	Integer generation,

	@Schema(description = "파트 (PM, DE, BE, FE 중 하나)", example = "BE")
	@NotNull(message = "파트를 선택해 주세요.")
	Part part
) {
}

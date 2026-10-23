package com.cotato.backend14th.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import com.cotato.backend14th.domain.user.dto.request.LoginRequest;
import com.cotato.backend14th.domain.user.dto.request.SignupRequest;
import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.domain.user.entity.User;

/**
 * 회원 관련 테스트 데이터. 엔티티의 id 는 DB 가 채우는 값이라 리플렉션으로 넣는다.
 */
public class UserFixture {

	public static final String EMAIL = "user@gmail.com";
	public static final String PASSWORD = "password1234";
	public static final String ENCODED_PASSWORD = "{encoded}password1234";

	public static User user(Long id) {
		User user = User.builder()
			.email(EMAIL)
			.password(ENCODED_PASSWORD)
			.name("홍길동")
			.nickname("길동이")
			.generation(14)
			.part(Part.BE)
			.build();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}

	public static SignupRequest signupRequest() {
		return new SignupRequest(EMAIL, PASSWORD, "홍길동", "길동이", 14, Part.BE);
	}

	public static LoginRequest loginRequest() {
		return new LoginRequest(EMAIL, PASSWORD);
	}
}

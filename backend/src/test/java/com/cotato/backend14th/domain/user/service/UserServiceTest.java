package com.cotato.backend14th.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cotato.backend14th.domain.user.dto.response.UserMeResponse;
import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.domain.user.repository.UserRepository;
import com.cotato.backend14th.fixture.UserFixture;
import com.cotato.backend14th.global.exception.Backend14thException;
import com.cotato.backend14th.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	@DisplayName("내 정보를 조회하면 기수와 파트를 포함해 돌려준다")
	void getMe() {
		// Given
		given(userRepository.findById(1L)).willReturn(Optional.of(UserFixture.user(1L)));

		// When
		UserMeResponse response = userService.getMe(1L);

		// Then
		assertThat(response.userId()).isEqualTo(1L);
		assertThat(response.email()).isEqualTo(UserFixture.EMAIL);
		assertThat(response.nickname()).isEqualTo("길동이");
		assertThat(response.generation()).isEqualTo(14);
		assertThat(response.part()).isEqualTo(Part.BE);
	}

	@Test
	@DisplayName("토큰은 유효하지만 회원이 없으면 UNAUTHORIZED 이다")
	void getMeUserMissing() {
		// Given
		given(userRepository.findById(99L)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> userService.getMe(99L))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
	}
}

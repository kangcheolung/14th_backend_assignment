package com.cotato.backend14th.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cotato.backend14th.domain.user.dto.request.LoginRequest;
import com.cotato.backend14th.domain.user.dto.response.LoginResponse;
import com.cotato.backend14th.domain.user.dto.response.SignupResponse;
import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.domain.user.repository.UserRepository;
import com.cotato.backend14th.fixture.UserFixture;
import com.cotato.backend14th.global.exception.Backend14thException;
import com.cotato.backend14th.global.exception.ErrorCode;
import com.cotato.backend14th.global.security.JwtProvider;
import com.cotato.backend14th.global.security.RevokedToken;
import com.cotato.backend14th.global.security.RevokedTokenRepository;
import com.cotato.backend14th.global.security.TokenPayload;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private static final String SECRET = "test-only-secret-key-for-auth-service-0123456789";

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private RevokedTokenRepository revokedTokenRepository;

	// 토큰 발급·검증은 실제 구현으로 확인한다. 가짜로 대신하면 로그아웃 흐름의 의미가 사라진다
	private final JwtProvider jwtProvider = new JwtProvider(SECRET, 3_600_000L);

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(userRepository, passwordEncoder, jwtProvider, revokedTokenRepository);
	}

	@Test
	@DisplayName("회원가입하면 비밀번호를 암호화해 저장하고 회원 id 를 돌려준다")
	void signup() {
		// Given
		given(userRepository.existsByEmail(UserFixture.EMAIL)).willReturn(false);
		given(passwordEncoder.encode(UserFixture.PASSWORD)).willReturn(UserFixture.ENCODED_PASSWORD);
		given(userRepository.save(any(User.class))).willReturn(UserFixture.user(1L));

		// When
		SignupResponse response = authService.signup(UserFixture.signupRequest());

		// Then
		assertThat(response.userId()).isEqualTo(1L);
		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(saved.capture());
		assertThat(saved.getValue().getPassword()).isEqualTo(UserFixture.ENCODED_PASSWORD);
		assertThat(saved.getValue().getGeneration()).isEqualTo(14);
	}

	@Test
	@DisplayName("이미 가입된 이메일로 가입하면 EMAIL_ALREADY_EXISTS 이고 저장하지 않는다")
	void signupDuplicateEmail() {
		// Given
		given(userRepository.existsByEmail(UserFixture.EMAIL)).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> authService.signup(UserFixture.signupRequest()))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);
		verify(userRepository, never()).save(any());
	}

	@Test
	@DisplayName("이메일과 비밀번호가 맞으면 Bearer 토큰을 돌려준다")
	void login() {
		// Given
		given(userRepository.findByEmail(UserFixture.EMAIL)).willReturn(Optional.of(UserFixture.user(1L)));
		given(passwordEncoder.matches(UserFixture.PASSWORD, UserFixture.ENCODED_PASSWORD)).willReturn(true);

		// When
		LoginResponse response = authService.login(UserFixture.loginRequest());

		// Then
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(jwtProvider.parse(response.accessToken()).userId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("가입되지 않은 이메일로 로그인하면 INVALID_CREDENTIALS 이다")
	void loginUnknownEmail() {
		// Given
		given(userRepository.findByEmail(UserFixture.EMAIL)).willReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> authService.login(UserFixture.loginRequest()))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
	}

	@Test
	@DisplayName("비밀번호가 틀리면 이메일이 없을 때와 같은 INVALID_CREDENTIALS 이다")
	void loginWrongPassword() {
		// Given
		given(userRepository.findByEmail(UserFixture.EMAIL)).willReturn(Optional.of(UserFixture.user(1L)));
		given(passwordEncoder.matches("wrong-password", UserFixture.ENCODED_PASSWORD)).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> authService.login(new LoginRequest(UserFixture.EMAIL, "wrong-password")))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
	}

	@Test
	@DisplayName("로그아웃하면 토큰의 jti 를 무효 목록에 저장한다")
	void logout() {
		// Given
		String token = jwtProvider.createToken(1L);
		TokenPayload payload = jwtProvider.parse(token);
		given(revokedTokenRepository.existsByJti(payload.jti())).willReturn(false);

		// When
		authService.logout("Bearer " + token);

		// Then
		ArgumentCaptor<RevokedToken> saved = ArgumentCaptor.forClass(RevokedToken.class);
		verify(revokedTokenRepository).save(saved.capture());
		assertThat(saved.getValue().getJti()).isEqualTo(payload.jti());
		assertThat(saved.getValue().getExpiresAt()).isEqualTo(payload.expiresAt());
	}

	@Test
	@DisplayName("이미 무효 처리된 토큰으로 로그아웃해도 다시 저장하지 않고 끝난다")
	void logoutAlreadyRevoked() {
		// Given
		String token = jwtProvider.createToken(1L);
		given(revokedTokenRepository.existsByJti(jwtProvider.parse(token).jti())).willReturn(true);

		// When
		authService.logout("Bearer " + token);

		// Then
		verify(revokedTokenRepository, never()).save(any());
	}

	@Test
	@DisplayName("Authorization 헤더가 없거나 토큰이 올바르지 않으면 로그아웃은 UNAUTHORIZED 이다")
	void logoutWithoutValidToken() {
		assertThatThrownBy(() -> authService.logout(null))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
		assertThatThrownBy(() -> authService.logout("Bearer not-a-jwt"))
			.isInstanceOf(Backend14thException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
	}
}

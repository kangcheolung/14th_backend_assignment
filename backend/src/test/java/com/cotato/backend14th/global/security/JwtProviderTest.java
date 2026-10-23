package com.cotato.backend14th.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;

class JwtProviderTest {

	private static final String SECRET = "test-only-secret-key-for-jwt-provider-0123456789";
	private static final String OTHER_SECRET = "another-test-only-secret-key-for-jwt-9876543210";
	private static final long ONE_HOUR_MS = 3_600_000L;

	private final JwtProvider jwtProvider = new JwtProvider(SECRET, ONE_HOUR_MS);

	@Test
	@DisplayName("토큰을 만들고 파싱하면 회원 id 와 만료 시각이 그대로 나온다")
	void createAndParse() {
		// Given
		Long userId = 7L;

		// When
		TokenPayload payload = jwtProvider.parse(jwtProvider.createToken(userId));

		// Then
		assertThat(payload.userId()).isEqualTo(userId);
		assertThat(payload.jti()).isNotBlank();
		assertThat(payload.expiresAt()).isAfter(LocalDateTime.now());
	}

	@Test
	@DisplayName("같은 회원이 토큰을 두 번 만들어도 jti 는 서로 다르다")
	void jtiIsUnique() {
		// When
		String first = jwtProvider.parse(jwtProvider.createToken(1L)).jti();
		String second = jwtProvider.parse(jwtProvider.createToken(1L)).jti();

		// Then
		assertThat(first).isNotEqualTo(second);
	}

	@Test
	@DisplayName("다른 비밀키로 서명된 토큰은 파싱에 실패한다")
	void parseWithDifferentKey() {
		// Given
		String token = new JwtProvider(OTHER_SECRET, ONE_HOUR_MS).createToken(1L);

		// When & Then
		assertThatThrownBy(() -> jwtProvider.parse(token)).isInstanceOf(JwtException.class);
	}

	@Test
	@DisplayName("만료된 토큰은 파싱에 실패한다")
	void parseExpiredToken() {
		// Given
		String token = new JwtProvider(SECRET, -1_000L).createToken(1L);

		// When & Then
		assertThatThrownBy(() -> jwtProvider.parse(token)).isInstanceOf(ExpiredJwtException.class);
	}

	@Test
	@DisplayName("JWT 형식이 아닌 문자열은 파싱에 실패한다")
	void parseMalformedToken() {
		assertThatThrownBy(() -> jwtProvider.parse("not-a-jwt")).isInstanceOf(JwtException.class);
	}

	@Test
	@DisplayName("비밀키가 32바이트보다 짧으면 만들 때 거부한다")
	void rejectShortSecret() {
		assertThatThrownBy(() -> new JwtProvider("short", ONE_HOUR_MS)).isInstanceOf(WeakKeyException.class);
	}

	@Test
	@DisplayName("Bearer 헤더에서 토큰만 꺼낸다")
	void extractToken() {
		assertThat(jwtProvider.extractToken("Bearer abc.def.ghi")).contains("abc.def.ghi");
	}

	@Test
	@DisplayName("헤더가 없거나 Bearer 형식이 아니거나 토큰이 비어 있으면 빈 값이다")
	void extractTokenAbsent() {
		assertThat(jwtProvider.extractToken(null)).isEmpty();
		assertThat(jwtProvider.extractToken("Basic abc")).isEmpty();
		assertThat(jwtProvider.extractToken("Bearer ")).isEmpty();
	}
}

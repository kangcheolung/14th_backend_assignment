package com.cotato.backend14th.global.security;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 를 만들고 검증한다. 토큰이 폐기(로그아웃)됐는지는 알지 못하며, 그건 {@link JwtAuthenticationFilter} 의 몫이다.
 *
 * <p>비밀키는 설정(환경변수)으로만 받는다. HS256 은 키가 32바이트 미만이면 jjwt 가 시작 시점에 거부한다.
 */
@Component
public class JwtProvider {

	private static final String BEARER_PREFIX = "Bearer ";

	private final SecretKey key;
	private final long expirationMs;

	public JwtProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.expiration-ms}") long expirationMs
	) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String createToken(Long userId) {
		Date now = new Date();
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.id(UUID.randomUUID().toString()) // 로그아웃한 토큰을 구분할 키
			.issuedAt(now)
			.expiration(new Date(now.getTime() + expirationMs))
			.signWith(key)
			.compact();
	}

	/**
	 * 서명과 만료를 검증하고 값을 꺼낸다.
	 *
	 * @throws JwtException 서명이 틀렸거나, 만료됐거나, 형식이 잘못된 토큰
	 */
	public TokenPayload parse(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();

		LocalDateTime expiresAt = LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
		return new TokenPayload(Long.valueOf(claims.getSubject()), claims.getId(), expiresAt);
	}

	/**
	 * {@code Authorization: Bearer xxx} 헤더 값에서 토큰만 꺼낸다. 헤더가 없거나 형식이 다르면 빈 값이다.
	 */
	public Optional<String> extractToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			return Optional.empty();
		}
		String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
		return token.isEmpty() ? Optional.empty() : Optional.of(token);
	}
}

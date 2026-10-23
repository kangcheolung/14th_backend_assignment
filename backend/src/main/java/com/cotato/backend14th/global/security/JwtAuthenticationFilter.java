package com.cotato.backend14th.global.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 요청마다 Authorization 헤더의 토큰을 검증해 인증 정보를 채운다.
 *
 * <p>토큰이 없거나, 틀렸거나, 만료됐거나, 로그아웃한 토큰이면 인증 정보를 채우지 않고 그대로 넘긴다.
 * 그러면 공개 API 는 익명으로 통과하고, 로그인이 필요한 API 는 {@link AuthEntryPoint} 가 401 로 막는다.
 * 즉 이 필터는 직접 거절하지 않는다. principal 은 회원 id({@code Long}) 이다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;
	private final RevokedTokenRepository revokedTokenRepository;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
	) throws ServletException, IOException {
		jwtProvider.extractToken(request.getHeader(HttpHeaders.AUTHORIZATION))
			.ifPresent(this::authenticate);
		filterChain.doFilter(request, response);
	}

	private void authenticate(String token) {
		try {
			TokenPayload payload = jwtProvider.parse(token);
			if (revokedTokenRepository.existsByJti(payload.jti())) {
				return;
			}
			SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(payload.userId(), null, List.of())
			);
		} catch (JwtException | IllegalArgumentException e) {
			// 잘못된 토큰은 익명으로 취급한다. 거절은 인증이 필요한 API 에서 AuthEntryPoint 가 한다.
			SecurityContextHolder.clearContext();
		}
	}
}

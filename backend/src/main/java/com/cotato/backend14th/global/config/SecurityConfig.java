// SecurityConfig.java
package com.cotato.backend14th.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.cotato.backend14th.global.security.AuthEntryPoint;
import com.cotato.backend14th.global.security.JwtAuthenticationFilter;
import com.cotato.backend14th.global.security.JwtProvider;
import com.cotato.backend14th.global.security.RevokedTokenRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CorsConfigurationSource corsConfigurationSource;
	private final JwtProvider jwtProvider;
	private final RevokedTokenRepository revokedTokenRepository;
	private final AuthEntryPoint authEntryPoint;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); // 비밀번호는 BCrypt로 암호화해서 저장해요
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors(cors -> cors.configurationSource(corsConfigurationSource)) // 앞서 만든 CorsConfig를 그대로 사용해요
			.csrf(AbstractHttpConfigurer::disable) // REST API는 세션 기반이 아니라 CSRF 보호가 필요 없어요
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않아요
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.exceptionHandling(e -> e.authenticationEntryPoint(authEntryPoint)) // 인증 실패는 401 공통 에러로 내려줘요
			.authorizeHttpRequests(auth -> auth
				// 공개: 가입, 로그인
				.requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login").permitAll()
				// 공개: 모집글과 댓글 조회 (목록, 상세, 댓글 목록)
				.requestMatchers(HttpMethod.GET, "/api/study-posts", "/api/study-posts/**").permitAll()
				// 공개: Swagger, 시큐리티 단계에서 막히면 에러 응답이 가려지는 /error, 기존 학습용 API
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
				.requestMatchers("/error", "/hello", "/books/**").permitAll()
				// 나머지(로그아웃, 내 정보, 작성·수정·삭제)는 로그인이 필요해요. 작성자 본인 확인은 Service에서 해요
				.anyRequest().authenticated()
			)
			.addFilterBefore(
				new JwtAuthenticationFilter(jwtProvider, revokedTokenRepository),
				UsernamePasswordAuthenticationFilter.class
			);
		return http.build();
	}
}
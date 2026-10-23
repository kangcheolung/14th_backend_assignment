package com.cotato.backend14th.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.backend14th.domain.user.dto.request.LoginRequest;
import com.cotato.backend14th.domain.user.dto.request.SignupRequest;
import com.cotato.backend14th.domain.user.dto.response.LoginResponse;
import com.cotato.backend14th.domain.user.dto.response.SignupResponse;
import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.domain.user.repository.UserRepository;
import com.cotato.backend14th.global.exception.Backend14thException;
import com.cotato.backend14th.global.exception.ErrorCode;
import com.cotato.backend14th.global.security.JwtProvider;
import com.cotato.backend14th.global.security.RevokedToken;
import com.cotato.backend14th.global.security.RevokedTokenRepository;
import com.cotato.backend14th.global.security.TokenPayload;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

/**
 * 회원가입, 로그인, 로그아웃을 처리한다. 내 정보 조회 같은 회원 정보 읽기는 {@link UserService} 가 맡는다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RevokedTokenRepository revokedTokenRepository;

	@Transactional
	public SignupResponse signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new Backend14thException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		User user = User.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.name(request.name())
			.nickname(request.nickname())
			.generation(request.generation())
			.part(request.part())
			.build();
		return new SignupResponse(userRepository.save(user).getId());
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		// 이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 에러로 돌려, 가입 여부를 추측하지 못하게 한다
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new Backend14thException(ErrorCode.INVALID_CREDENTIALS));
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new Backend14thException(ErrorCode.INVALID_CREDENTIALS);
		}
		return LoginResponse.bearer(jwtProvider.createToken(user.getId()));
	}

	/**
	 * 사용한 토큰을 만료 시각까지 무효 처리한다. 이미 무효 처리된 토큰으로 다시 불러도 오류 없이 끝난다.
	 */
	@Transactional
	public void logout(String authorizationHeader) {
		String token = jwtProvider.extractToken(authorizationHeader)
			.orElseThrow(() -> new Backend14thException(ErrorCode.UNAUTHORIZED));
		TokenPayload payload = parse(token);

		if (!revokedTokenRepository.existsByJti(payload.jti())) {
			revokedTokenRepository.save(RevokedToken.of(payload.jti(), payload.expiresAt()));
		}
	}

	private TokenPayload parse(String token) {
		try {
			return jwtProvider.parse(token);
		} catch (JwtException e) {
			throw new Backend14thException(ErrorCode.UNAUTHORIZED);
		}
	}
}

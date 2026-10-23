package com.cotato.backend14th.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.backend14th.domain.user.dto.response.UserMeResponse;
import com.cotato.backend14th.domain.user.entity.User;
import com.cotato.backend14th.domain.user.repository.UserRepository;
import com.cotato.backend14th.global.exception.Backend14thException;
import com.cotato.backend14th.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 회원 정보를 읽는다. 가입과 로그인은 {@link AuthService} 의 몫이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	public UserMeResponse getMe(Long userId) {
		return UserMeResponse.from(getUser(userId));
	}

	/**
	 * 다른 도메인(모집글, 댓글)이 작성자를 찾을 때 쓴다. 이 도메인의 Repository 를 직접 쓰지 않게 하려는 입구다.
	 */
	public User getUser(Long userId) {
		// 토큰은 유효한데 회원이 없는 경우는 로그인이 풀린 것과 같게 본다
		return userRepository.findById(userId)
			.orElseThrow(() -> new Backend14thException(ErrorCode.UNAUTHORIZED));
	}
}

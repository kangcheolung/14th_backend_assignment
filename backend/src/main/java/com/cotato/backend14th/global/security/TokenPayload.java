package com.cotato.backend14th.global.security;

import java.time.LocalDateTime;

/**
 * 검증이 끝난 JWT 에서 꺼낸 값. jjwt 의 {@code Claims} 를 서비스 계층으로 내보내지 않기 위한 얇은 껍데기다.
 *
 * @param userId    토큰 주인의 회원 id
 * @param jti       토큰 고유 id. 로그아웃(무효화) 대상을 가리키는 키다
 * @param expiresAt 토큰 만료 시각. 무효화 기록은 이 시각까지만 의미가 있다
 */
public record TokenPayload(Long userId, String jti, LocalDateTime expiresAt) {
}

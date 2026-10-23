package com.cotato.backend14th.global.security;

import java.time.LocalDateTime;

import com.cotato.backend14th.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그아웃으로 무효가 된 토큰의 기록. JWT 는 서버가 상태를 갖지 않아 만료 전에는 스스로 끊을 수 없으므로,
 * 토큰 고유 id(jti)를 DB 에 남겨 두고 요청마다 대조한다. DB 라서 서버를 다시 켜도 기록이 남는다.
 *
 * <p>만료 시각이 지난 기록은 더 이상 쓸모가 없지만, 이번 범위에서는 정리 작업을 두지 않는다.
 */
@Entity
@Table(name = "revoked_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevokedToken extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 36)
	private String jti;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	private RevokedToken(String jti, LocalDateTime expiresAt) {
		this.jti = jti;
		this.expiresAt = expiresAt;
	}

	public static RevokedToken of(String jti, LocalDateTime expiresAt) {
		return new RevokedToken(jti, expiresAt);
	}
}

package com.cotato.backend14th.domain.user.entity;

import com.cotato.backend14th.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 스터디 플랫폼 회원. 이메일로 로그인하고 비밀번호는 BCrypt 로 암호화한 값만 저장한다.
 *
 * <p>"user" 는 DB 예약어와 겹칠 수 있어 테이블 이름은 users 로 둔다.
 * 인증 절차(토큰 발급 등)는 알지 못하고 회원 정보만 가진다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 100)
	private String password;

	@Column(nullable = false, length = 30)
	private String name;

	@Column(nullable = false, length = 10)
	private String nickname;

	@Column(nullable = false)
	private int generation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Part part;

	@Builder
	private User(String email, String password, String name, String nickname, int generation, Part part) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.nickname = nickname;
		this.generation = generation;
		this.part = part;
	}
}

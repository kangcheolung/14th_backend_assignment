package com.cotato.backend14th.global.security;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

	boolean existsByJti(String jti);
}

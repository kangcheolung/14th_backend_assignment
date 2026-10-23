package com.cotato.backend14th;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// JWT_SECRET 은 환경 파일에서 오는 값이라 테스트에서는 테스트 전용 임시 키를 쓴다 (실제 비밀키 아님)
@SpringBootTest
@TestPropertySource(properties = "jwt.secret=test-only-secret-key-for-context-loads-0123456789")
class Backend14thApplicationTests {

	@Test
	void contextLoads() {
	}

}

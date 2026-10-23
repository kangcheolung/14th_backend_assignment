package com.cotato.backend14th.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	private static final String BEARER_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("My API")                    // 문서 제목
				.description("스터디 실습용 API 문서")  // 설명
				.version("v1.0.0"))                  // 버전
			// Swagger UI의 Authorize 버튼에 로그인으로 받은 토큰을 넣으면 인증이 필요한 API도 호출해 볼 수 있어요
			.components(new Components().addSecuritySchemes(BEARER_SCHEME,
				new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
			.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
	}
}

package com.cotato.backend14th.global.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cotato.backend14th.domain.studypost.controller.CommentController;
import com.cotato.backend14th.domain.studypost.controller.StudyPostController;
import com.cotato.backend14th.domain.studypost.dto.request.StudyPostCreateRequest;
import com.cotato.backend14th.domain.studypost.dto.response.CommentIdResponse;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostDetailResponse;
import com.cotato.backend14th.domain.studypost.dto.response.StudyPostIdResponse;
import com.cotato.backend14th.domain.studypost.entity.StudyStatus;
import com.cotato.backend14th.domain.studypost.service.StudyPostCommentService;
import com.cotato.backend14th.domain.studypost.service.StudyPostService;
import com.cotato.backend14th.domain.user.controller.AuthController;
import com.cotato.backend14th.domain.user.controller.UserController;
import com.cotato.backend14th.domain.user.dto.response.AuthorResponse;
import com.cotato.backend14th.domain.user.dto.response.SignupResponse;
import com.cotato.backend14th.domain.user.entity.Part;
import com.cotato.backend14th.domain.user.service.AuthService;
import com.cotato.backend14th.domain.user.service.UserService;
import com.cotato.backend14th.global.common.response.PageResponse;
import com.cotato.backend14th.global.config.CorsConfig;
import com.cotato.backend14th.global.config.SecurityConfig;

/**
 * 시큐리티 규칙(공개/로그인 필요), 에러 응답 형식, 명세의 JSON 필드 이름을 DB 없이 확인한다.
 * 서비스는 가짜로 대신하고, 필터·권한 설정·검증·예외 처리는 실제 설정을 그대로 쓴다.
 */
@WebMvcTest(controllers = {
	StudyPostController.class, CommentController.class, AuthController.class, UserController.class
})
@Import({SecurityConfig.class, CorsConfig.class, AuthEntryPoint.class, JwtProvider.class})
@TestPropertySource(properties = {
	"jwt.secret=test-only-secret-key-for-api-security-0123456789",
	"jwt.expiration-ms=3600000"
})
class ApiSecurityTest {

	private static final Long USER_ID = 1L;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtProvider jwtProvider;

	@MockitoBean
	private StudyPostService studyPostService;

	@MockitoBean
	private StudyPostCommentService commentService;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private RevokedTokenRepository revokedTokenRepository;

	// 메인 클래스의 @EnableJpaAuditing 이 웹 계층 테스트에서도 JPA 메타모델을 요구해서 가짜로 채운다
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("모집글 목록은 로그인 없이 조회할 수 있다")
	void listIsPublic() throws Exception {
		given(studyPostService.getList(any(), any(), any(), eq(0), eq(10)))
			.willReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

		mockMvc.perform(get("/api/study-posts"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.page").value(0))
			.andExpect(jsonPath("$.data.totalPages").value(0));
	}

	@Test
	@DisplayName("잘못된 토큰으로도 공개 API 는 익명으로 조회된다")
	void invalidTokenOnPublicApiIsAnonymous() throws Exception {
		given(studyPostService.getList(any(), any(), any(), eq(0), eq(10)))
			.willReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

		mockMvc.perform(get("/api/study-posts").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("모집 상태 값이 올바르지 않으면 400 INVALID_INPUT 이다")
	void invalidStatusParam() throws Exception {
		mockMvc.perform(get("/api/study-posts").param("status", "WRONG"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value("COMMON-003"));
	}

	@Test
	@DisplayName("상세 응답에는 명세의 isAuthor 필드가 있다")
	void detailHasIsAuthorField() throws Exception {
		given(studyPostService.get(10L, USER_ID)).willReturn(new StudyPostDetailResponse(
			10L, "제목", "내용", StudyStatus.RECRUITING, new AuthorResponse("길동이", 14, Part.BE),
			true, LocalDateTime.of(2026, 11, 8, 12, 0), LocalDateTime.of(2026, 11, 8, 12, 0)));

		mockMvc.perform(get("/api/study-posts/10").header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.isAuthor").value(true))
			.andExpect(jsonPath("$.data.author.nickname").value("길동이"))
			.andExpect(jsonPath("$.data.createdAt").value("2026-11-08T12:00:00"));
	}

	@Test
	@DisplayName("로그인 없이 모집글을 쓰면 401 이고 공통 에러 형식으로 내려간다")
	void createWithoutLogin() throws Exception {
		mockMvc.perform(post("/api/study-posts").contentType(MediaType.APPLICATION_JSON).content(validPostBody()))
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.code").value("COMMON-004"))
			.andExpect(jsonPath("$.message").value("로그인이 필요해요."))
			.andExpect(jsonPath("$.path").value("/api/study-posts"));
	}

	@Test
	@DisplayName("로그인한 회원이 모집글을 쓰면 201 이고 토큰 주인의 id 로 서비스를 호출한다")
	void createWithLogin() throws Exception {
		given(studyPostService.create(eq(USER_ID), any(StudyPostCreateRequest.class)))
			.willReturn(new StudyPostIdResponse(10L));

		mockMvc.perform(post("/api/study-posts")
				.header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content(validPostBody()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.postId").value(10));
		verify(studyPostService).create(eq(USER_ID), any(StudyPostCreateRequest.class));
	}

	@Test
	@DisplayName("로그아웃해서 무효가 된 토큰은 401 이다")
	void revokedToken() throws Exception {
		String token = jwtProvider.createToken(USER_ID);
		given(revokedTokenRepository.existsByJti(jwtProvider.parse(token).jti())).willReturn(true);

		mockMvc.perform(post("/api/study-posts")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(validPostBody()))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("COMMON-004"));
	}

	@Test
	@DisplayName("깨진 토큰으로 로그인이 필요한 API 를 부르면 401 이다")
	void garbageTokenOnProtectedApi() throws Exception {
		mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("COMMON-004"));
	}

	@Test
	@DisplayName("제목이 비어 있으면 400 이고 검증 메시지를 그대로 내려준다")
	void createWithBlankTitle() throws Exception {
		mockMvc.perform(post("/api/study-posts")
				.header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"\",\"content\":\"내용\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON-003"))
			.andExpect(jsonPath("$.message").value("제목을 입력해 주세요."));
	}

	@Test
	@DisplayName("제목이 50자를 넘으면 400 이다")
	void createWithTooLongTitle() throws Exception {
		String longTitle = "가".repeat(51);

		mockMvc.perform(post("/api/study-posts")
				.header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"" + longTitle + "\",\"content\":\"내용\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("제목은 50자 이하로 입력해 주세요."));
	}

	@Test
	@DisplayName("모집글을 지우면 본문 없이 204 이다")
	void deleteReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/api/study-posts/10").header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));
		verify(studyPostService).delete(10L, USER_ID);
	}

	@Test
	@DisplayName("로그인 없이 댓글을 수정하거나 지우면 401 이다")
	void commentWriteNeedsLogin() throws Exception {
		mockMvc.perform(patch("/api/comments/5").contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"수정\"}"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(delete("/api/comments/5"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("댓글 목록은 로그인 없이 조회할 수 있다")
	void commentListIsPublic() throws Exception {
		given(commentService.getList(eq(10L), any(), eq(0), eq(20)))
			.willReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

		mockMvc.perform(get("/api/study-posts/10/comments"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.size").value(20));
	}

	@Test
	@DisplayName("댓글을 쓰면 201 이다")
	void createComment() throws Exception {
		given(commentService.create(eq(10L), eq(USER_ID), any())).willReturn(new CommentIdResponse(5L));

		mockMvc.perform(post("/api/study-posts/10/comments")
				.header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"content\":\"참여하고 싶어요\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.commentId").value(5));
	}

	@Test
	@DisplayName("댓글 내용이 500자를 넘으면 400 이다")
	void createCommentTooLong() throws Exception {
		mockMvc.perform(post("/api/study-posts/10/comments")
				.header(HttpHeaders.AUTHORIZATION, bearer(USER_ID))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"content\":\"" + "가".repeat(501) + "\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("내용은 500자 이하로 입력해 주세요."));
	}

	@Test
	@DisplayName("회원가입 요청에 기수가 1 미만이면 400 이고 기수 안내 문구가 나온다")
	void signupWithZeroGeneration() throws Exception {
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signupBody(0, "BE")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON-003"))
			.andExpect(jsonPath("$.message").value("기수는 1 이상이어야 해요."));
	}

	@Test
	@DisplayName("회원가입 요청의 파트가 PM, DE, BE, FE 가 아니면 400 이다")
	void signupWithUnknownPart() throws Exception {
		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signupBody(14, "XX")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON-003"));
	}

	@Test
	@DisplayName("회원가입은 로그인 없이 할 수 있고 201 로 회원 id 를 돌려준다")
	void signupIsPublic() throws Exception {
		given(authService.signup(any())).willReturn(new SignupResponse(1L));

		mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signupBody(14, "BE")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data.userId").value(1));
	}

	@Test
	@DisplayName("로그아웃은 로그인이 필요하고 성공하면 204 이다")
	void logout() throws Exception {
		mockMvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer(USER_ID)))
			.andExpect(status().isNoContent());
	}

	private String bearer(Long userId) {
		return "Bearer " + jwtProvider.createToken(userId);
	}

	private static String validPostBody() {
		return "{\"title\":\"알고리즘 스터디 모집\",\"content\":\"주 2회 백준 풀이...\"}";
	}

	private static String signupBody(int generation, String part) {
		return "{\"email\":\"user@gmail.com\",\"password\":\"password1234\",\"name\":\"홍길동\","
			+ "\"nickname\":\"길동이\",\"generation\":" + generation + ",\"part\":\"" + part + "\"}";
	}
}

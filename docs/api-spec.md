# API 명세

코테이토 스터디 매칭 플랫폼의 API 13개. 구현은 `backend/` 이고, 아래 내용은 컨트롤러·DTO·`ErrorCode` 에서 확인한 값이다.
Swagger UI: `http://localhost:8080/swagger-ui/index.html` (Authorize 버튼에 로그인으로 받은 토큰을 넣으면 인증이 필요한 API 도 호출할 수 있다)

## 공통 규칙

- 모든 주소는 `/api` 로 시작한다.
- 권한은 세 가지다. **공개**(토큰 불필요), **로그인**(토큰 필요), **작성자**(로그인 + 글·댓글 작성자 본인).
- 인증이 필요한 API 는 `Authorization: Bearer {accessToken}` 헤더를 보낸다. 토큰은 발급 후 1시간 유효하다.
- 공개 API 에 잘못된(만료·로그아웃·깨진) 토큰을 보내도 익명으로 처리해서 정상 응답한다.
- 성공 응답은 공통 형식으로 감싼다. 아래 응답 예시는 `data` 안쪽만 적은 것이다. 본문이 없는 `204` 는 감싸지 않는다.

```json
{ "success": true, "status": 200, "data": { }, "timestamp": "2026-11-08 12:00:00" }
```

- 실패 응답은 모든 API 에서 같은 형식이다. 화면에는 `message` 를 보여준다.

```json
{
  "success": false,
  "status": 404,
  "code": "STUDY-001",
  "message": "존재하지 않는 스터디 모집글이에요.",
  "method": "GET",
  "path": "/api/study-posts/999",
  "timestamp": "2026-11-08 12:00:00"
}
```

- 목록 API 는 `content`, `page`(0부터), `size`, `totalElements`, `totalPages` 를 돌려준다.
- 작성자는 `author` 객체(`nickname`, `generation`, `part`)로 내려준다. 이메일은 내리지 않는다.
- 시각은 `2026-11-08T12:00:00.123456` 같은 ISO 형식이다.

## 목록

| # | Method | URL | 권한 | 설명 |
|---|---|---|---|---|
| 1 | POST | `/api/auth/signup` | 공개 | 회원가입 (기수, 파트 포함) |
| 2 | POST | `/api/auth/login` | 공개 | 로그인 (토큰 발급) |
| 3 | POST | `/api/auth/logout` | 로그인 | 로그아웃 (토큰 무효화) |
| 4 | GET | `/api/users/me` | 로그인 | 내 정보 조회 |
| 5 | POST | `/api/study-posts` | 로그인 | 모집글 작성 |
| 6 | GET | `/api/study-posts` | 공개 | 모집글 목록 (검색, 모집 상태·파트 필터, 페이징) |
| 7 | GET | `/api/study-posts/{postId}` | 공개 | 모집글 상세 |
| 8 | PATCH | `/api/study-posts/{postId}` | 작성자 | 모집글 수정 (모집 상태 변경 포함) |
| 9 | DELETE | `/api/study-posts/{postId}` | 작성자 | 모집글 삭제 (댓글도 함께 삭제) |
| 10 | POST | `/api/study-posts/{postId}/comments` | 로그인 | 댓글(신청) 작성 |
| 11 | GET | `/api/study-posts/{postId}/comments` | 공개 | 댓글 목록 |
| 12 | PATCH | `/api/comments/{commentId}` | 작성자 | 댓글 수정 |
| 13 | DELETE | `/api/comments/{commentId}` | 작성자 | 댓글 삭제 |

## 인증

### 1. 회원가입 `POST /api/auth/signup`

```txt
[Request]
{ "email": "user@gmail.com", "password": "password1234", "name": "홍길동",
  "nickname": "길동이", "generation": 14, "part": "BE" }

[Response 201]
{ "userId": 1 }
```

| 필드 | 검증 |
|---|---|
| `email` | 필수, 이메일 형식, 100자 이하 |
| `password` | 필수, 8자 이상 100자 이하 (저장은 BCrypt 암호화) |
| `name` | 필수, 30자 이하 |
| `nickname` | 필수, 2~10자 |
| `generation` | 필수, 1 이상의 정수 |
| `part` | 필수, `PM`(기획), `DE`(디자인), `BE`(백엔드), `FE`(프론트엔드) 중 하나 |

에러: `400 COMMON-003`, `409 USER-001`

### 2. 로그인 `POST /api/auth/login`

```txt
[Request]
{ "email": "user@gmail.com", "password": "password1234" }

[Response 200]
{ "accessToken": "eyJhbGciOi...", "tokenType": "Bearer" }
```

이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 에러로 돌려, 가입 여부를 추측하지 못하게 한다.
에러: `400 COMMON-003`(빈 값), `401 USER-002`

### 3. 로그아웃 `POST /api/auth/logout`

```txt
[Response 204] 본문 없음
```

- 사용한 토큰의 `jti` 를 DB(`revoked_tokens`)에 저장해 만료 시각까지 무효 처리한다. 서버를 다시 켜도 유지된다.
- 로그아웃한 토큰으로 다시 요청하면 `401` 이다.
- 프론트는 저장한 토큰을 지우고 로그인 화면으로 이동한다.

에러: `401 COMMON-004`

### 4. 내 정보 조회 `GET /api/users/me`

```txt
[Response 200]
{ "userId": 1, "email": "user@gmail.com", "name": "홍길동", "nickname": "길동이",
  "generation": 14, "part": "BE" }
```

비밀번호는 응답에 넣지 않는다. 에러: `401 COMMON-004`

## 스터디 모집글

### 5. 모집글 작성 `POST /api/study-posts`

```txt
[Request]
{ "title": "알고리즘 스터디 모집", "content": "주 2회 백준 풀이..." }

[Response 201]
{ "postId": 10 }
```

- 검증: `title` 필수 50자 이하, `content` 필수 2000자 이하
- 처음 만들면 `studyStatus` 는 `RECRUITING` 이다.

에러: `400 COMMON-003`, `401 COMMON-004`

### 6. 모집글 목록 `GET /api/study-posts`

```txt
GET /api/study-posts?keyword=알고리즘&status=RECRUITING&part=BE&page=0&size=10

[Response 200] 최신순
{
  "content": [
    { "postId": 10, "title": "알고리즘 스터디 모집", "studyStatus": "RECRUITING",
      "author": { "nickname": "길동이", "generation": 14, "part": "BE" },
      "commentCount": 3, "createdAt": "2026-11-08T12:00:00" }
  ],
  "page": 0, "size": 10, "totalElements": 1, "totalPages": 1
}
```

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `keyword` | 선택 | 제목 또는 내용에 포함된 키워드 |
| `status` | 선택 | `RECRUITING` 또는 `CLOSED`. 없으면 전체 |
| `part` | 선택 | `PM`, `DE`, `BE`, `FE`. **글 작성자의 파트**가 같은 글만. 없으면 전체 |
| `page` | 선택 | 기본 0, 0 이상 |
| `size` | 선택 | 기본 10, 1~100 |

`keyword`, `status`, `part` 는 함께 쓸 수 있고, 함께 쓰면 모두 만족하는 글만 나온다.
에러: `400 COMMON-003`(status·part 값이 올바르지 않거나 page·size 범위 밖)

### 7. 모집글 상세 `GET /api/study-posts/{postId}`

```txt
[Response 200]
{
  "postId": 10, "title": "알고리즘 스터디 모집", "content": "주 2회 백준 풀이...",
  "studyStatus": "RECRUITING",
  "author": { "nickname": "길동이", "generation": 14, "part": "BE" },
  "isAuthor": true,
  "createdAt": "2026-11-08T12:00:00", "updatedAt": "2026-11-08T12:00:00"
}
```

로그인하지 않았으면 `isAuthor` 는 `false` 이다. 에러: `404 STUDY-001`

### 8. 모집글 수정 `PATCH /api/study-posts/{postId}`

```txt
[Request] 바꿀 필드만 보낸다
{ "title": "수정된 제목", "content": "수정된 내용", "studyStatus": "CLOSED" }

[Response 200]
{ "postId": 10 }
```

- 보낸 필드만 바뀐다. 보낸 `title` 은 비어 있지 않고 50자 이하, `content` 는 비어 있지 않고 2000자 이하여야 한다.
- `studyStatus` 는 `RECRUITING` 과 `CLOSED` 사이를 자유롭게 바꿀 수 있다.

에러: `400 COMMON-003`, `401 COMMON-004`, `403 STUDY-002`, `404 STUDY-001`

### 9. 모집글 삭제 `DELETE /api/study-posts/{postId}`

```txt
[Response 204] 본문 없음
```

글을 삭제하면 달린 댓글도 함께 삭제된다. 에러: `401 COMMON-004`, `403 STUDY-002`, `404 STUDY-001`

## 댓글 = 스터디 신청

### 10. 댓글 작성 `POST /api/study-posts/{postId}/comments`

```txt
[Request]
{ "content": "안녕하세요! 알고리즘 입문 중인데 참여하고 싶어요." }

[Response 201]
{ "commentId": 5 }
```

- 검증: `content` 필수 500자 이하
- 모집이 마감된(`CLOSED`) 글에는 쓸 수 없다.

에러: `400 COMMON-003`, `401 COMMON-004`, `404 STUDY-001`, `409 STUDY-003`

### 11. 댓글 목록 `GET /api/study-posts/{postId}/comments`

```txt
GET /api/study-posts/10/comments?page=0&size=20

[Response 200] 오래된 순
{
  "content": [
    { "commentId": 5, "content": "안녕하세요! ...",
      "author": { "nickname": "철수", "generation": 13, "part": "FE" },
      "isAuthor": false, "createdAt": "2026-11-08T13:00:00" }
  ],
  "page": 0, "size": 20, "totalElements": 1, "totalPages": 1
}
```

`page` 는 기본 0, `size` 는 기본 20 (1~100). 로그인하지 않았으면 `isAuthor` 는 항상 `false` 이다.
에러: `400 COMMON-003`(page·size 범위 밖), `404 STUDY-001`

### 12. 댓글 수정 `PATCH /api/comments/{commentId}`

```txt
[Request]
{ "content": "수정한 신청 메시지" }

[Response 200]
{ "commentId": 5 }
```

에러: `400 COMMON-003`, `401 COMMON-004`, `403 COMMENT-002`, `404 COMMENT-001`

### 13. 댓글 삭제 `DELETE /api/comments/{commentId}`

```txt
[Response 204] 본문 없음
```

에러: `401 COMMON-004`, `403 COMMENT-002`, `404 COMMENT-001`

## 에러 코드

응답의 `code` 는 `도메인-일련번호` 형식이다. (`ErrorCode` enum 의 상수 이름은 괄호 안)

| HTTP | code | 상수 | 메시지 |
|---|---|---|---|
| 400 | `COMMON-003` | `INVALID_INPUT` | 검증 실패 시 해당 필드의 안내 문구, 그 밖에는 "요청 값이 올바르지 않아요." |
| 401 | `COMMON-004` | `UNAUTHORIZED` | 로그인이 필요해요. (토큰이 없거나, 무효이거나, 로그아웃한 토큰) |
| 401 | `USER-002` | `INVALID_CREDENTIALS` | 이메일 또는 비밀번호가 올바르지 않아요. |
| 403 | `STUDY-002` | `NOT_POST_AUTHOR` | 모집글 작성자만 할 수 있어요. |
| 403 | `COMMENT-002` | `NOT_COMMENT_AUTHOR` | 댓글 작성자만 할 수 있어요. |
| 404 | `STUDY-001` | `STUDY_POST_NOT_FOUND` | 존재하지 않는 스터디 모집글이에요. |
| 404 | `COMMENT-001` | `COMMENT_NOT_FOUND` | 존재하지 않는 댓글이에요. |
| 409 | `USER-001` | `EMAIL_ALREADY_EXISTS` | 이미 가입된 이메일이에요. |
| 409 | `STUDY-003` | `STUDY_POST_CLOSED` | 모집이 마감된 글에는 댓글을 쓸 수 없어요. |
| 500 | `COMMON-002` | `COMMON_INTERNAL_SERVER_ERROR` | 서버 내부 오류가 발생했습니다. |

응답의 `code` 는 3주차 `ErrorCode` 규칙(`도메인-일련번호`)을 따르고, 위 표의 "상수" 열은 `ErrorCode` enum 의 상수 이름이다. (3주차의 `BOOK_NOT_FOUND` 가 `BOOK-001` 인 것과 같은 관계)

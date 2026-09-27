# green_board

Spring Boot 게시판 수업에서 참고하는 코드입니다.
수업에서는 기본 CRUD부터 시작해서 기능을 하나씩 붙입니다. 이 저장소는 JWT까지 적용한 최종 상태라 세션 로그인 코드는 없습니다.

Java 21, Spring Boot 3.5.9, MySQL을 사용합니다.

## 실행

IntelliJ에서 Gradle 프로젝트로 열고 `GreenBoardApplication`을 실행합니다.
실행 설정에 아래 환경 변수를 넣어 주세요. `.env` 파일은 Spring Boot가 자동으로 읽지 않습니다.

```text
DB_URL=jdbc:mysql://localhost:3306/데이터베이스이름
DB_USERNAME=사용자이름
DB_PASSWORD=비밀번호
JWT_SECRET=JWT서명키
MAIL_USERNAME=Gmail주소
MAIL_PASSWORD=Gmail앱비밀번호
```

JWT 서명키는 현재 HS256 설정에 맞게 32바이트 이상으로 지정합니다.
Discord 신고 알림을 사용한다면 `DISCORD_WEBHOOK_URL`도 설정합니다. 없으면 신고만 저장합니다.

기존 수업 DB에 추가할 테이블과 컬럼은 `src/main/resources/schema-additions.sql`,
`src/main/resources/schema-reference-fixes.sql`에 있습니다.
이미 있는 컬럼과 FULLTEXT 인덱스는 건너뛰세요. 현재 로컬 수업 DB에는 필요한 변경을 적용해 두었습니다.
두 파일은 자동 실행되지 않으며, 새 DB를 처음부터 만드는 전체 스키마는 아닙니다.

Swagger는 실행 후 `/swagger-ui/index.html`에서 확인할 수 있습니다.

## 수업 참고

- 게시글 CRUD, 조회수, 좋아요, 페이지네이션, 정렬, 제목 FULLTEXT 검색
- 공지사항, 댓글, 내 게시글·댓글·좋아요 조회
- 회원가입과 이메일 인증, JWT 로그인, 토큰 재발급과 정리 스케줄러
- 비밀번호 변경, 회원 차단·탈퇴, 관리자 삭제, 신고 처리와 Discord 알림
- DTO 검증, 공통 응답, 예외 처리, JPA Auditing, Soft Delete, Log4j2

목록의 본문은 100자까지 반환합니다. 제한 전 조회 예제는
`BoardService.getAllBoardsWithoutPreview()`에 남겨 두었고, 실제 API에서는 호출하지 않습니다.
공지사항과 일반 게시글을 함께 조회하는 코드는 `getBoardHome()`에 있습니다.

```text
GET /api/board?page=0&size=10
GET /api/board?page=0&size=10&sort=hits,desc&sort=id,desc
GET /api/board?page=0&size=10&sort=likeCount,desc&sort=id,desc
```

게시글 수정일은 내용뿐 아니라 조회수나 좋아요 수가 바뀔 때도 갱신됩니다.
비밀번호 변경 시 리프레시 토큰을 삭제하지만, 이미 발급한 액세스 토큰은 만료까지 유효합니다.

실행 점검 결과와 메일·Discord 연동 시 확인할 내용은 [QA.md](QA.md)에 적어 두었습니다.

# 🛒 SimpleShop - 세션 기반 쇼핑몰 API

Spring Boot 기반으로 로그인부터 상품 등록, 다중 이미지 업로드, Swagger 문서화까지 구현한 실전형 REST API 백엔드 프로젝트입니다.

---

## 🎯 프로젝트 목적

**클라우드 인프라 경험 기반의 백엔드 전향**을 목표로, 실무 서비스 구성 요소인 인증/인가, 이미지 처리, RESTful 설계, 문서화 등을 직접 구현하며 백엔드 개발 역량을 강화했습니다.

- 세션 인증 및 스프링 시큐리티 흐름 이해
- S3 업로드 및 IAM Role 연동 구조 구현
- Swagger 3.0 기반 문서화 및 테스트 자동화
- 도메인 중심 구조와 API 설계 패턴 적용

---

## 🪞 회고 (Retrospective)

- **세션 인증을 수동 구현하며** Spring Security의 흐름을 체득
- S3 이미지 업로드를 **IAM Role 방식으로 구성**, 보안과 운영까지 고려
- **다중 이미지 처리 + 순서 보장/삭제 기능 구현**을 통해 실무에 가까운 API 설계 경험
- Swagger 문서에 쿠키 인증(JSESSIONID)을 연동하여 **프론트 협업을 위한 API 테스트 환경 구성**
- **페이징 + 정렬 기능 구현**으로 실제 데이터 API 설계 패턴을 습득

---

## 📌 주요 기능

| 기능 | 설명 |
|------|------|
| ✅ 회원가입 & 로그인 | 세션 기반 인증 처리 (JSESSIONID) |
| ✅ 세션 인증 처리 | 로그인 후 API 접근 시 세션 기반 인증 필터 적용 |
| ✅ 상품 CRUD | 작성자만 수정/삭제 가능 |
| ✅ 이미지 업로드 | AWS S3 기반 다중 이미지 업로드 (URL 반환) |
| ✅ 이미지 삭제 | 단일 이미지 삭제 + 자동 순서 재정렬 |
| ✅ 페이징 및 정렬 | 최신순, 가격순 정렬 옵션 제공 |
| ✅ Swagger 문서화 | Springdoc OpenAPI 3.0 기반 UI 자동 생성 |

---

## 🛠️ 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.x, Spring Security, Spring Web, JPA (Hibernate) |
| 인증 | 세션 기반 인증 (HttpSession + Cookie) |
| DB | H2, MariaDB |
| 파일 저장소 | AWS S3 (IAM Role 연동) |
| 문서화 | Swagger UI (springdoc-openapi 3.x) |
| 테스트 도구 | Postman, Swagger UI |
| 개발 도구 | IntelliJ, Gradle, Git |

---

## 🔐 인증 구조 (Session-based)

```text
[Login]
→ POST /api/users/login
→ 서버 세션 생성 + JSESSIONID 발급 (HttpOnly Cookie)

[인증 필터 작동]
→ 모든 요청에 쿠키 포함
→ 세션 인증 필터(SessionFilter)가 사용자 ID 검증

[Logout]
→ POST /api/users/logout
→ 세션 무효화
````

> Swagger UI에서 `cookie: JSESSIONID`를 수동으로 입력하여 테스트 가능

---

## 📷 이미지 처리 구조

* **다중 업로드** 지원 (`POST /api/products/{id}/images`)
* 업로드 시 순서 자동 지정 (`imageOrder`)
* 삭제 시 순서 자동 재정렬
* S3에서 `public-read` URL 반환 → 직접 표시 가능

### 상품 조회 예시

```json
{
  "id": 1,
  "name": "테스트 상품",
  "images": [
    { "id": 10, "url": "https://.../img1.jpg", "order": 0 },
    { "id": 11, "url": "https://.../img2.jpg", "order": 1 }
  ]
}
```

---

## 🧪 실행 방법

1. `application.yml` 에 다음 항목 설정

   ```yaml
   file:
     upload-dir: uploads/
   cloud:
     aws:
       s3:
         bucket: your-bucket-name
   ```
2. 로컬 서버 실행: `./gradlew bootRun`
   - 8080 포트 사용시 제거
     - `ID=$(lsof -ti :8080)` -> `[ -n "$PID" ] && kill "$PID"`
3. Swagger 문서 확인: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
---

## 📂 디렉토리 구조 (요약)

```
src/main/java/com/example/simpleshop
├── config                 # Security, Swagger, S3 설정
├── controller             # User, Product API 컨트롤러
├── domain
│   ├── product            # Product, ProductImage 엔티티 및 서비스
│   └── user               # User 엔티티 및 인증 서비스
├── dto                   # Request/Response DTOs
```

---




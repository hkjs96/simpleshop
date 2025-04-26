# 세션 기반 인증(SPA) 예제 프로젝트

Spring Boot 백엔드와 React 프론트엔드를 분리하여 안전한 세션 기반 인증을 구현한 예제입니다. 제품 관리 기능(이미지 업로드·조회)도 포함되어 있습니다.

---

## 🔑 주요 기능

- **REST API `/api/users/login`**: 이메일/비밀번호로 로그인
- **세션 관리**: JSESSIONID 쿠키로 인증 유지
- **세션 고정 공격 방어**: 로그인 시 세션 재생성
- **CORS 설정**: React(`http://localhost:3000`)와 통신 지원
- **보안 쿠키**: HttpOnly, SameSite
- **제품 관리**: 이미지 업로드/다운로드 및 base64 인코딩 지원

---

## 📦 기술 스택

- **백엔드**: Java 17, Spring Boot 3.x, Spring Security
- **프론트엔드**: React 18, Fetch API
- **데이터베이스**: H2(테스트), MySQL/MariaDB

---

## ▶️ 인증 흐름

1. 클라이언트가 `/api/users/login`로 로그인 요청
2. 서버에서 사용자 검증 후 세션 생성
3. `JSESSIONID` HttpOnly 쿠키 반환
4. 이후 요청에 쿠키 자동 포함
5. 필터(`SessionAuthenticationFilter`)에서 세션 유효성 검사

---

## ⚙️ 백엔드 설정

### CORS 설정 (예시)
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

### 쿠키 설정 (예시)
```java
private void configureCookie(HttpServletResponse res, HttpSession session) {
    Cookie cookie = new Cookie("JSESSIONID", session.getId());
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(-1);
    // production 환경에서는 아래 옵션 활성화
    // cookie.setSecure(true);
    // cookie.setAttribute("SameSite", "Lax");
    res.addCookie(cookie);
}
```

---

## 🚀 프론트엔드 통합 가이드

### 로그인
```js
async function login(email, password) {
  const res = await fetch("http://localhost:8080/api/users/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
    credentials: "include"
  });
  if (!res.ok) throw new Error("로그인 실패");
  return await res.json();
}
```

### 인증된 요청
```js
async function fetchProtected(url) {
  const res = await fetch(url, { credentials: "include" });
  if (res.status === 401) window.location.href = "/login";
  if (!res.ok) throw new Error("요청 실패");
  return await res.json();
}
```

### 로그아웃
```js
async function logout() {
  const res = await fetch("http://localhost:8080/api/users/logout", {
    method: "POST",
    credentials: "include"
  });
  if (!res.ok) throw new Error("로그아웃 실패");
  window.location.href = "/login";
}
```

---

## 🖼️ 제품 이미지 처리 방식 비교

| 방식       | 설명                                      | 장점                               | 단점                        |
|-----------|-----------------------------------------|-----------------------------------|----------------------------|
| 전통 방식 | 제품 정보와 이미지를 별도 endpoint로 호출    | 큰 파일에 유리, 페이로드 작음       | API 호출 수 증가           |
| 통합 방식 | 제품 정보에 base64 이미지 포함            | 호출 수 감소, 코드 단순화          | 페이로드 33% 증가          |

---

## 📝 테스트

- `UserControllerTest`: 로그인·로그아웃·세션 관리 검증
- `SecurityConfigTest`: 시큐리티 설정·CORS 검증
- `ProductControllerTest`: 제품 조회·이미지 처리 검증

---

## 🔔 주의 사항

1. **HTTPS** 환경에서는 `cookie.setSecure(true)` 활성화
2. 배포 시 CORS 허용 도메인 수정
3. **CSRF 보호** 추가 검토 (현재 API용 비활성)
4. 대용량 이미지의 경우 CDN 활용 권장


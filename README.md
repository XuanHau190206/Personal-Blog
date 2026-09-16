# Personal Blog

Blog cá nhân tối giản: Spring Boot + Thymeleaf (SSR), không dùng database — mỗi bài viết
là một file `.json` trên đĩa. Xem chi tiết yêu cầu ở `docs/PRD.md` và `docs/DOMAIN.md`.

## Chạy ứng dụng

```bash
./mvnw spring-boot:run
```

Mặc định chạy ở `http://localhost:8080`.

Chạy toàn bộ test:

```bash
./mvnw test
```

## Đăng nhập Admin

- URL: `http://localhost:8080/login`
- Tài khoản demo: `admin` / `admin123`

Đổi mật khẩu bằng cách sinh BCrypt hash mới rồi cập nhật `admin.username` /
`admin.password-hash` trong `src/main/resources/application.properties`:

```bash
jshell --class-path <path-to-spring-security-crypto.jar> -q -
System.out.println(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("mat-khau-moi"));
```

## Lưu trữ bài viết

Mỗi bài viết là 1 file JSON tại `data/articles/{uuid}.json` (thư mục này bị `.gitignore`,
chỉ giữ lại `.gitkeep`). Có thể chỉnh đường dẫn qua `blog.storage.path` trong
`application.properties`.

## Kiến trúc

Xem package structure, API contract và các quyết định kiến trúc trong
`docs/PRD.md` / `docs/DOMAIN.md`. Tóm tắt các layer:

- `web/` — Controller (Spring MVC + Thymeleaf), không chứa business logic.
- `article/` — `ArticleService` (business rules, cache), `FileArticleRepository` (I/O file JSON, khoá ghi, chặn path traversal).
- `markdown/` — `MarkdownRenderer` (commonmark) chuyển Markdown → HTML cho trang chi tiết.
- `config/SecurityConfig` — Spring Security: form login, bảo vệ `/admin/**`, BCrypt.

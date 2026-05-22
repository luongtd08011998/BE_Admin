# Tóm tắt Dự án — HOIDANIT-SPRING-REST-WITH-AI

> Tài liệu này tổng hợp mục tiêu dự án, các quyết định thiết kế quan trọng và những quy ước đã được thống nhất.
> Đọc file này trước khi bắt đầu bất kỳ công việc nào.

---

## 1. Mục tiêu dự án

**Hệ thống quản lý nhân sự & khách hàng** — RESTful API backend phục vụ 2 nhóm người dùng:

| Nhóm | Vai trò |
|------|---------|
| **Admin** | Quản lý nội dung (bài viết, tài liệu, danh mục, người dùng, vai trò, quyền hạn...) |
| **Khách hàng (QLKH)** | Tra cứu hóa đơn điện tử (VNPT), nhận thông báo, gửi phản hồi, quản lý thiết bị FCM |

**Tech stack:**
- Java 25 + Spring Boot 4.x + Spring Framework 7
- Spring Security 7 + oauth2-resource-server (Nimbus JOSE JWT — HS512)
- Spring Data JPA + MySQL
- Maven
- SpringDoc OpenAPI (Swagger)

---

## 2. Các quyết định thiết kế quan trọng (ADR)

### ADR-001 — Chiến lược Refresh Token
- **Quyết định:** Refresh token được lưu trong **database** (có thể revoke), trả về đồng thời qua **cookie HttpOnly** và **response body**.
- **Lý do:** Hỗ trợ đăng xuất từ xa, giảm rủi ro token bị đánh cắp qua XSS (cookie HttpOnly).
- **Thời hạn:** Access token 15 phút, Refresh token 7 ngày.

### ADR-002 — Chiến lược tải lên file
- **Quyết định:** File được lưu trữ **cục bộ** (Local Storage) trên server, phục vụ qua **Static Resource Serving** của Spring Boot.
- **Lý do:** Đơn giản, phù hợp quy mô hiện tại, không phụ thuộc dịch vụ đám mây bên ngoài.
- **Giới hạn:** Tối đa 10MB/file, hỗ trợ jpg/png/gif/webp/pdf/doc/docx.

### ADR-003 — Xác thực JWT với oauth2-resource-server
- **Quyết định:** Dùng cơ chế built-in `oauth2ResourceServer` của Spring Security — **không viết custom filter**.
- **Lý do:** Giảm code boilerplate, tận dụng tích hợp sẵn của framework.
- **Cấu hình:** `JwtEncoder` (ký token) + `JwtDecoder` (xác minh token) là Spring Beans, thuật toán HS512.

### ADR-004 — Dynamic Filter với JPA Specification
- **Quyết định:** Dùng `PredicateSpecification<T>` (Spring Data JPA 4.0) thay vì `Specification<T>` cũ.
- **Lý do:** Tương thích với Spring Boot 4, tách rõ logic filter khỏi repository.
- **Cấu trúc:** Filter request dùng record, repository extends `JpaSpecificationExecutor<T>`, service trả `ResultPaginationDTO`.

### ADR-005 — Tích hợp VNPT Portal (SOAP)
- **Quyết định:** Gọi SOAP API `downloadInvZipFkey` để tải hóa đơn điện tử qua `VnptPortalInvoiceClient`.
- **Cấu hình:** Thông tin đăng nhập VNPT được nạp từ `.env` qua `DotenvEnvironmentPostProcessor`, hỗ trợ override bằng `-Denv.file=`.

### ADR-006 — Deploy Production
- **Môi trường:** Windows Server, chạy JAR bằng `start.bat` với JVM args.
- **Proxy:** Nginx reverse proxy với self-signed SSL (port 443), cấu hình HTTP → HTTPS redirect.
- **CORS:** Cho phép origin `https://125.253.121.171` trong `SecurityConfig`.

---

## 3. Kiến trúc & Quy ước đã thống nhất

### 3.1 Cấu trúc package
```
com.example.projectname/
├── config/          # SecurityConfig, JwtConfig, CorsConfig, OpenApiConfig
├── security/        # SecurityUtil, CustomUserDetailsService
├── exception/       # GlobalExceptionHandler, AppException, ResourceNotFoundException
├── dto/             # ApiResponse (dùng chung), PaginationDTO
└── feature/         # Mỗi tính năng = 1 package
    └── {name}/
        ├── {Name}Controller.java
        ├── {Name}Service.java       # Interface
        ├── {Name}ServiceImpl.java   # Implementation
        ├── {Name}Repository.java
        ├── {Name}.java              # Entity
        ├── CONTEXT.md               # Tài liệu ngữ cảnh của module
        └── dto/
```

### 3.2 Quy tắc các lớp

| Lớp | Quy tắc cốt lõi |
|-----|----------------|
| **Controller** | Nhận request → gọi service → trả response. Không có logic nghiệp vụ |
| **Service** | Interface + Impl. Không biết về HTTP. Chuyển đổi Entity → DTO tại đây |
| **Repository** | Spring Data JPA. Không có logic nghiệp vụ. Trả `Optional<T>` cho kết quả đơn |
| **Entity** | Dùng explicit getter/setter, không dùng `@Data`. Tên bảng snake_case |
| **DTO** | Dùng Java `record`. Request có validation, Response có `fromEntity()` |

### 3.3 Những thứ **BẮT BUỘC**
- ✅ Luôn dùng constructor injection (`final` field + explicit constructor) — **KHÔNG dùng `@Autowired`**
- ✅ Luôn bọc response trong `ApiResponse<T>`
- ✅ Luôn dùng `@Valid` trên `@RequestBody`
- ✅ Luôn inject Interface, không inject class Impl
- ✅ `@Transactional` trên Impl method khi ghi dữ liệu
- ✅ Mật khẩu BCrypt (strength 12), không bao giờ lưu plain text
- ✅ Không bao giờ expose Entity trực tiếp ra ngoài service layer
- ✅ Không log: password, token, thông tin nhạy cảm

### 3.4 Những thứ **CẤM**
- ❌ Dùng `@Autowired` field injection
- ❌ Dùng H2 in-memory database (kể cả trong test)
- ❌ Dùng `@Data` (Lombok) trên Entity
- ❌ Dùng `java.util.Date` hoặc `Timestamp` — dùng `Instant` hoặc `LocalDateTime`
- ❌ `@Enumerated(EnumType.ORDINAL)` — luôn dùng `STRING`
- ❌ Hardcode config — dùng `application.yml` + biến môi trường
- ❌ Expose stack trace, SQL error cho client
- ❌ Viết business logic trong Controller
- ❌ Gọi `HttpServletRequest` / `ResponseEntity` trong Service

### 3.5 Giới hạn kích thước code

| Đối tượng | Giới hạn |
|-----------|---------|
| File | < 300 dòng |
| Method | < 50 dòng |
| Tham số method | < 5 |
| Tham số constructor | < 7 |
| Nested blocks (if/for) | < 3 cấp |

---

## 4. Chuẩn API Response

```json
{
  "statusCode": 200,
  "data": { ... },
  "message": "Success",
  "timestamp": "2026-05-22T04:00:00"
}
```
- **Thành công:** `ApiResponse.success(data)` hoặc `ApiResponse.created(data)`
- **Lỗi:** `ApiResponse.error(statusCode, message)` (qua `GlobalExceptionHandler`)
- Validation lỗi: trả `Map<field, message>`

---

## 5. Tài liệu liên quan

| File | Mục đích |
|------|---------|
| `CLAUDE.md` | Entry point — đọc trước tiên |
| `docs/PROJECT-RULES.md` | Toàn bộ coding convention (nguồn sự thật duy nhất) |
| `docs/PROJECT-STATUS.md` | Tiến độ hiện tại + danh sách toàn bộ API |
| `docs/ARCHITECTURE.md` | Kiến trúc hệ thống |
| `docs/API_SPEC.md` | Đặc tả chi tiết từng API endpoint |
| `docs/DATABASE.md` | Schema cơ sở dữ liệu |
| `docs/decisions/` | Nhật ký các quyết định thiết kế (ADR) |
| `feature/{name}/CONTEXT.md` | Ngữ cảnh chi tiết từng module |

---

## 6. Trạng thái hiện tại (tóm tắt)

> Xem chi tiết đầy đủ tại `docs/PROJECT-STATUS.md`

- ✅ **Toàn bộ 20 nhóm API đã hoàn thành** — từ Auth, CRUD (Users/Companies/Roles/Permissions/Tags/Articles/Categories/Documents), Files & Media, Dashboard, toàn bộ QLKH module, Admin module, đến External (VietQR)
- ✅ Deploy lên Windows Server với Nginx + SSL
- ✅ Tích hợp VNPT Portal (SOAP)
- ⚠️ **Spring Boot 4 breaking changes:** Jackson 3 đổi package (`tools.jackson.*`), MockMvc annotation đổi package
- 📌 **Chú ý còn lại:** RBAC middleware (Phase 7) và Polish (Phase 8) chưa triển khai đầy đủ

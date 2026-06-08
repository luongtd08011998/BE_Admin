# Trạng thái Dự án (Project Status)

> Cập nhật lần cuối: 2026-05-05 | Thực hiện bởi: @hoidanit | Phiên làm việc: #5
>
> AI: Cập nhật file này ở cuối mỗi phiên làm việc khi được yêu cầu.
> Tuân thủ định dạng chính xác này. Giữ nội dung ngắn gọn — dưới 80 dòng (hoặc tối ưu hóa độ dài).

---

## Đã hoàn thành (Completed)

- ✅ Khung dự án skeleton (Spring Boot 4, Maven, application.yml)
- ✅ Cấu trúc tài liệu (CLAUDE.md, PROJECT-RULES, ARCHITECTURE, DATABASE, API_SPEC)
- ✅ ADR-001: Quyết định chiến lược Refresh Token (Cookie + Body)
- ✅ ADR-002: Quyết định chiến lược tải lên file (Lưu trữ cục bộ + Cung cấp tài nguyên tĩnh)
- ✅ Cấu hình quy trình làm việc của AI (.claude/commands/)
- ✅ Các lớp ngoại lệ cơ bản (ResourceNotFoundException, DuplicateResourceException)
- ✅ Bộ xử lý ngoại lệ toàn cục GlobalExceptionHandler (@RestControllerAdvice)
- ✅ Lớp bọc ApiResponse (lớp với các phương thức factory)
- ✅ Cấu hình bảo mật SecurityConfig (cho phép tất cả trên `/`, `/auth/**`; bắt buộc JWT với phần còn lại)
- ✅ Cấu hình JWT - JwtConfig (JwtEncoder, JwtDecoder — đã sẵn sàng và được áp dụng)
- ✅ **[2026-03-01]** CRUD Permission — entity, repository, DTOs (records), service, controller + CONTEXT.md
- ✅ **[2026-03-01]** CRUD Company — entity, repository, DTOs (records), service, controller
- ✅ **[2026-03-02]** CRUD Role — entity (ManyToMany Permission), repository, DTOs (records), service, controller + CONTEXT.md
- ✅ **[2026-03-02]** CRUD User — entity (ManyToOne Company, ManyToMany Role), DTOs (records), service, controller (phân trang) + CONTEXT.md
- ✅ **[2026-04-04]** CRUD Tag — entity (tên độc nhất), repository, DTOs (records), service, controller + CONTEXT.md
- ✅ **[2026-04-05]** CRUD Article — entity (byte/active), TagArticle join entity, repository, DTOs, service, controller + CONTEXT.md + seed 5 permission + 3 bài viết mẫu
- ✅ **[2026-04-14]** QLKH Invoices (một phần) — VNPT SOAP `downloadInvZipFkey`, `VnptPortalProperties`, endpoint tải hóa đơn ZIP
- ✅ **[2026-04-14]** Dotenv, CORS production HTTPS, Deploy Windows Server (start.bat + Nginx SSL)
- ✅ **[2026-05-05]** Files & Media Library — entity, repository, specification, service, controller (upload/list/filter/delete), 4 permission seeded, hỗ trợ jpg/png/gif/webp/pdf/doc/docx (tối đa 10MB)
- ✅ **[2026-05-22]** CRUD Category — entity (cây phân cấp, slug), repository, DTOs, service, controller (CRUD + tree + slug + search + bài viết theo danh mục)
- ✅ **[2026-05-22]** CRUD Document — entity, repository, DTOs, service, controller
- ✅ **[2026-05-22]** Dashboard — endpoint tổng quan thống kê
- ✅ **[2026-05-22]** QLKH Auth — đăng nhập/refresh/logout/me cho khách hàng
- ✅ **[2026-05-22]** QLKH Invoices (hoàn chỉnh) — list/detail/download/e-invoice/sales-invoice/VNPT health
- ✅ **[2026-05-22]** QLKH Notifications — list/unread-count/mark-as-read
- ✅ **[2026-05-22]** QLKH Device — đăng ký/hủy FCM token
- ✅ **[2026-05-22]** QLKH Feedback — gửi/list/chi tiết
- ✅ **[2026-05-22]** Admin Invoices — list + gửi nhắc nợ/quá hạn/cắt nước
- ✅ **[2026-05-22]** Admin Feedbacks — list/thống kê/chi tiết/cập nhật trạng thái/reply
- ✅ **[2026-05-22]** Admin Roads — dropdown danh sách tuyến đường
- ✅ **[2026-05-22]** External — tạo mã VietQR (API Key)
- ✅ **[2026-05-22]** Auth Admin — login/register/refresh/logout/me (JWT)

### Trạng thái từng nhóm API

| Nhóm API                                    | Trạng thái | Ghi chú                                          |
| ------------------------------------------- | ---------- | ------------------------------------------------ |
| **Auth** (login/register/refresh/logout/me) | ✅ Xong    | login/register/refresh/logout/me (JWT)           |
| **Users** (CRUD)                            | ✅ Xong    | Phần đầy đủ entity, DTO                          |
| **Companies** (CRUD)                        | ✅ Xong    | Đầy đủ entity, DTO                               |
| **Roles** (CRUD)                            | ✅ Xong    | Đầy đủ entity, DTO                               |
| **Permissions** (CRUD)                      | ✅ Xong    | Đầy đủ entity, DTO                               |
| **Tags** (CRUD)                             | ✅ Xong    | Đầy đủ entity, DTO                               |
| **Articles** (CRUD + search)                | ✅ Xong    | Đầy đủ entity, DTO                               |
| **Categories** (CRUD + tree/slug)           | ✅ Xong    | Đầy đủ entity, DTO, cây phân cấp, slug           |
| **Documents** (CRUD)                        | ✅ Xong    | Đầy đủ entity, DTO                               |
| **Files & Media** (upload/list/delete)      | ✅ Xong    | 2026-05-05 — Media Library hoàn chỉnh            |
| **Dashboard**                               | ✅ Xong    | Endpoint tổng quan thống kê                      |
| **QLKH Auth** (login/refresh/logout/me)     | ✅ Xong    | Đăng nhập/refresh/logout/me khách hàng           |
| **QLKH Invoices** (list/detail/download)    | ✅ Xong    | List/detail/download/e-invoice/sales/VNPT health |
| **QLKH Notifications**                      | ✅ Xong    | List/unread-count/mark-as-read                   |
| **QLKH Device** (FCM token)                 | ✅ Xong    | Đăng ký/hủy FCM token                            |
| **QLKH Feedback**                           | ✅ Xong    | Gửi/list/chi tiết                                |
| **Admin Invoices** (list + gửi nhắc)        | ✅ Xong    | List + gửi nhắc nợ/quá hạn/cắt nước              |
| **Admin Feedbacks** (list/reply/status)     | ✅ Xong    | List/thống kê/chi tiết/trạng thái/reply          |
| **Admin Roads** (dropdown)                  | ✅ Xong    | Dropdown danh sách tuyến đường                   |
| **External** (VietQR)                       | ✅ Xong    | Tạo mã VietQR (API Key)                          |

## Danh sách toàn bộ API (API Reference)

> **Chú thích:** 🔓 Công khai · 🔒 Admin JWT · 🔒Q QLKH JWT · 🔑 API Key

### Auth (Xác thực Admin)

| Method | Endpoint       | Auth | Mô tả                    |
| ------ | -------------- | ---- | ------------------------ |
| POST   | /auth/login    | 🔓   | Đăng nhập admin          |
| POST   | /auth/register | 🔓   | Đăng ký tài khoản        |
| POST   | /auth/refresh  | 🔓   | Làm mới token            |
| POST   | /auth/logout   | 🔒   | Đăng xuất                |
| GET    | /auth/me       | 🔒   | Thông tin admin hiện tại |

### Users (Người dùng)

| Method | Endpoint    | Auth | Mô tả                    |
| ------ | ----------- | ---- | ------------------------ |
| GET    | /users      | 🔒   | Danh sách người dùng     |
| GET    | /users/{id} | 🔒   | Lấy thông tin người dùng |
| POST   | /users      | 🔒   | Tạo người dùng           |
| PUT    | /users      | 🔒   | Cập nhật người dùng      |
| DELETE | /users/{id} | 🔒   | Xóa người dùng           |

### Companies (Công ty)

| Method | Endpoint        | Auth | Mô tả                 |
| ------ | --------------- | ---- | --------------------- |
| GET    | /companies      | 🔒   | Danh sách công ty     |
| GET    | /companies/{id} | 🔒   | Lấy thông tin công ty |
| POST   | /companies      | 🔒   | Tạo công ty           |
| PUT    | /companies      | 🔒   | Cập nhật công ty      |
| DELETE | /companies/{id} | 🔒   | Xóa công ty           |

### Roles (Vai trò)

| Method | Endpoint    | Auth | Mô tả                 |
| ------ | ----------- | ---- | --------------------- |
| GET    | /roles      | 🔒   | Danh sách vai trò     |
| GET    | /roles/{id} | 🔒   | Lấy thông tin vai trò |
| POST   | /roles      | 🔒   | Tạo vai trò           |
| PUT    | /roles      | 🔒   | Cập nhật vai trò      |
| DELETE | /roles/{id} | 🔒   | Xóa vai trò           |

### Permissions (Quyền hạn)

| Method | Endpoint          | Auth | Mô tả                   |
| ------ | ----------------- | ---- | ----------------------- |
| GET    | /permissions      | 🔒   | Danh sách quyền hạn     |
| GET    | /permissions/{id} | 🔒   | Lấy thông tin quyền hạn |
| POST   | /permissions      | 🔒   | Tạo quyền hạn           |
| PUT    | /permissions      | 🔒   | Cập nhật quyền hạn      |
| DELETE | /permissions/{id} | 🔒   | Xóa quyền hạn           |

### Tags (Thẻ)

| Method | Endpoint   | Auth | Mô tả             |
| ------ | ---------- | ---- | ----------------- |
| GET    | /tags      | 🔒   | Danh sách thẻ     |
| GET    | /tags/{id} | 🔒   | Lấy thông tin thẻ |
| POST   | /tags      | 🔒   | Tạo thẻ           |
| PUT    | /tags      | 🔒   | Cập nhật thẻ      |
| DELETE | /tags/{id} | 🔒   | Xóa thẻ           |

### Articles (Bài viết)

| Method | Endpoint         | Auth | Mô tả                  |
| ------ | ---------------- | ---- | ---------------------- |
| GET    | /articles        | 🔒   | Danh sách bài viết     |
| GET    | /articles/search | 🔒   | Tìm kiếm bài viết      |
| GET    | /articles/{id}   | 🔒   | Lấy thông tin bài viết |
| POST   | /articles        | 🔒   | Tạo bài viết           |
| PUT    | /articles        | 🔒   | Cập nhật bài viết      |
| DELETE | /articles/{id}   | 🔒   | Xóa bài viết           |

### Categories (Danh mục)

| Method | Endpoint                         | Auth | Mô tả                          |
| ------ | -------------------------------- | ---- | ------------------------------ |
| GET    | /categories                      | 🔒   | Danh sách danh mục             |
| GET    | /categories/roots                | 🔒   | Danh mục gốc                   |
| GET    | /categories/{id}                 | 🔒   | Lấy thông tin danh mục         |
| POST   | /categories                      | 🔒   | Tạo danh mục                   |
| PUT    | /categories                      | 🔒   | Cập nhật danh mục              |
| DELETE | /categories/{id}                 | 🔒   | Xóa danh mục                   |
| GET    | /categories/search               | 🔒   | Tìm kiếm theo tên              |
| GET    | /categories/parent/{parentId}    | 🔒   | Danh mục con trực tiếp         |
| GET    | /categories/{id}/children        | 🔒   | Danh mục con trực tiếp (alias) |
| GET    | /categories/slug/{slug}          | 🔒   | Lấy theo slug                  |
| GET    | /categories/slug/{slug}/articles | 🔒   | Bài viết theo slug             |
| GET    | /categories/{id}/tree            | 🔒   | Cây danh mục                   |
| GET    | /categories/tree                 | 🔒   | Toàn bộ cây danh mục           |
| GET    | /categories/{id}/articles        | 🔒   | Bài viết theo cây danh mục     |

### Documents (Tài liệu)

| Method | Endpoint                       | Auth | Mô tả                  |
| ------ | ------------------------------ | ---- | ---------------------- |
| GET    | /documents                     | 🔒   | Danh sách tài liệu     |
| GET    | /documents/article/{articleId} | 🔒   | Tài liệu theo bài viết |
| GET    | /documents/{id}                | 🔒   | Lấy thông tin tài liệu |
| POST   | /documents                     | 🔒   | Tạo tài liệu           |
| PUT    | /documents                     | 🔒   | Cập nhật tài liệu      |
| DELETE | /documents/{id}                | 🔒   | Xóa tài liệu           |

### Files & Media (Tệp & Phương tiện)

| Method | Endpoint    | Auth | Mô tả               |
| ------ | ----------- | ---- | ------------------- |
| POST   | /files      | 🔒   | Tải lên tệp         |
| POST   | /media      | 🔒   | Tải lên media       |
| GET    | /media      | 🔒   | Danh sách media     |
| GET    | /media/{id} | 🔒   | Lấy thông tin media |
| DELETE | /media/{id} | 🔒   | Xóa media           |

### Dashboard (Bảng điều khiển)

| Method | Endpoint   | Auth | Mô tả                     |
| ------ | ---------- | ---- | ------------------------- |
| GET    | /dashboard | 🔒   | Tổng quan bảng điều khiển |

---

### QLKH Auth (Xác thực Khách hàng)

| Method | Endpoint           | Auth | Mô tả                         |
| ------ | ------------------ | ---- | ----------------------------- |
| POST   | /qlkh/auth/login   | 🔓   | Đăng nhập khách hàng          |
| POST   | /qlkh/auth/refresh | 🔓   | Làm mới token khách hàng      |
| POST   | /qlkh/auth/logout  | 🔒Q  | Đăng xuất khách hàng          |
| GET    | /qlkh/customers/me | 🔒Q  | Thông tin khách hàng hiện tại |

### QLKH Invoices (Hóa đơn)

| Method | Endpoint                                 | Auth | Mô tả                             |
| ------ | ---------------------------------------- | ---- | --------------------------------- |
| GET    | /qlkh/invoices                           | 🔒Q  | Danh sách hóa đơn                 |
| GET    | /qlkh/invoices/{id}                      | 🔒Q  | Lấy thông tin hóa đơn             |
| GET    | /qlkh/invoices/by-fkey                   | 🔒Q  | Tra cứu theo fkey                 |
| GET    | /qlkh/invoices/by-root-key               | 🔒Q  | Tra cứu theo rootKey              |
| GET    | /qlkh/invoices/{id}/e-invoice-download   | 🔒Q  | Tải hóa đơn điện tử               |
| GET    | /qlkh/invoices/{id}/e-invoice-view       | 🔒Q  | Xem hóa đơn điện tử               |
| GET    | /qlkh/invoices/e-invoice-list            | 🔒Q  | Danh sách hóa đơn điện tử (XML)   |
| GET    | /qlkh/month-invoices/readings            | 🔓   | Chỉ số đồng hồ                    |
| GET    | /qlkh/month-invoices/consumption-history | 🔓   | Lịch sử tiêu thụ của 1 khách hàng |
| GET    | /qlkh/sales-invoices                     | 🔒Q  | Danh sách hóa đơn bán hàng        |
| GET    | /qlkh/sales-invoices/{id}                | 🔒Q  | Lấy thông tin hóa đơn bán hàng    |
| GET    | /qlkh/vnpt/health                        | 🔒Q  | Kiểm tra kết nối VNPT             |
| GET    | /qlkh/customer/articles/maintenance      | 🔓   | Bài viết bảo trì                  |
| GET    | /qlkh/customer/articles/featured         | 🔓   | Bài viết nổi bật                  |

### QLKH Notifications (Thông báo)

| Method | Endpoint                                  | Auth | Mô tả                 |
| ------ | ----------------------------------------- | ---- | --------------------- |
| GET    | /qlkh/customer/notifications              | 🔒Q  | Danh sách thông báo   |
| GET    | /qlkh/customer/notifications/unread-count | 🔒Q  | Số thông báo chưa đọc |
| POST   | /qlkh/customer/notifications/read         | 🔒Q  | Đánh dấu đã đọc       |

### QLKH Device (Thiết bị)

| Method | Endpoint                         | Auth | Mô tả                 |
| ------ | -------------------------------- | ---- | --------------------- |
| POST   | /qlkh/customer/device/register   | 🔒Q  | Đăng ký FCM token     |
| POST   | /qlkh/customer/device/unregister | 🔒Q  | Hủy đăng ký FCM token |

### QLKH Feedback (Phản hồi khách hàng)

| Method | Endpoint                      | Auth | Mô tả                      |
| ------ | ----------------------------- | ---- | -------------------------- |
| POST   | /qlkh/customer/feedbacks      | 🔒Q  | Gửi phản hồi               |
| GET    | /qlkh/customer/feedbacks      | 🔒Q  | Danh sách phản hồi của tôi |
| GET    | /qlkh/customer/feedbacks/{id} | 🔒Q  | Chi tiết phản hồi          |

---

### Admin Invoices (Hóa đơn - Quản trị)

| Method | Endpoint                              | Auth | Mô tả                     |
| ------ | ------------------------------------- | ---- | ------------------------- |
| GET    | /admin/invoices                       | 🔒   | Danh sách hóa đơn (admin) |
| POST   | /admin/invoices/send-debt-reminder    | 🔒   | Gửi nhắc nợ               |
| POST   | /admin/invoices/send-overdue-reminder | 🔒   | Gửi nhắc quá hạn          |
| POST   | /admin/invoices/send-water-cutoff     | 🔒   | Gửi thông báo cắt nước    |

### Admin Feedbacks (Phản hồi - Quản trị)

| Method | Endpoint                      | Auth | Mô tả               |
| ------ | ----------------------------- | ---- | ------------------- |
| GET    | /admin/feedbacks/statistics   | 🔒   | Thống kê phản hồi   |
| GET    | /admin/feedbacks              | 🔒   | Danh sách phản hồi  |
| GET    | /admin/feedbacks/{id}         | 🔒   | Chi tiết phản hồi   |
| PUT    | /admin/feedbacks/{id}/status  | 🔒   | Cập nhật trạng thái |
| POST   | /admin/feedbacks/{id}/replies | 🔒   | Thêm phản hồi       |
| GET    | /admin/feedbacks/{id}/replies | 🔒   | Danh sách phản hồi  |

### Admin Roads (Tuyến đường - Quản trị)

| Method | Endpoint     | Auth | Mô tả                            |
| ------ | ------------ | ---- | -------------------------------- |
| GET    | /admin/roads | 🔒   | Danh sách tuyến đường (dropdown) |

---

## Nợ kỹ thuật (Technical Debt)

### [P1] Refactor package `qlkh/` — Không nhất quán với `feature/`

**Cấu trúc mục tiêu sau refactor:**

```
qlkh/
├── auth/           QlkhAuthController, QlkhAuthService(Interface+Impl), dto/
├── invoice/        QlkhInvoiceController, Service(Interface+Impl), entity/, dto/
├── notification/   NotificationController, Service(Interface+Impl), entity/, dto/
├── device/         CustomerDeviceController, dto/
├── feedback/       FeedbackController, FeedbackService(Interface+Impl), entity/, dto/
├── customer/       Customer entity, CustomerRepository, dto/
├── invoiceadmin/   InvoiceAdminController, Service(Interface+Impl), dto/
├── road/           RoadAdminController, RoadService(Interface+Impl), entity/, dto/
└── vnpt/           (giữ nguyên)
```

_(Ghi chú: Giữ Admin Invoice và Admin Road bên trong `qlkh/` để tương thích cấu trúc quét Multiple Data Sources của Spring JPA, tránh lỗi query nhầm sang database `primary`)_

**Ưu tiên:** P1 — Nên làm trước khi thêm tính năng mới vào QLKH module

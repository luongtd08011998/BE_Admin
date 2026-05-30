# Prompt Phát Triển Giao Diện Quản Lý Kho (QLK) cho Frontend Team / AI

> **Mục đích:** Tài liệu (Prompt) này dùng để gửi cho team Frontend hoặc cung cấp cho AI Frontend (như v0, Cursor, Copilot) để phát triển UI/UX cho Phân hệ Quản Lý Kho (QLK) tích hợp vào hệ thống hiện tại.

---

## 1. BỐI CẢNH & YÊU CẦU CHUNG (CONTEXT & GLOBAL REQUIREMENTS)

Chúng ta cần phát triển giao diện Quản Lý Kho (QLK) cho một hệ thống Web Admin hiện có. Backend (Spring Boot) đã hoàn thiện 100% các RESTful API và có sẵn Swagger/OpenAPI.
Yêu cầu Frontend tuân thủ các quy chuẩn sau:
- **UI Framework:** Tái sử dụng hệ thống Component hiện hành của dự án (Ant Design, MUI, Tailwind CSS...).
- **Authentication & API Calls:** 
  - Đính kèm Bearer Token (JWT) vào header `Authorization` của mọi request.
  - Xử lý Global Error (Toast notification) cho các response lỗi từ Backend (`400 Bad Request`, `403 Forbidden`, `404 Not Found`, `500 Server Error`).

## 2. LUỒNG ĐĂNG NHẬP MỚI (AUTH FLOW UPDATE)

API Đăng nhập: `POST /api/v1/auth/login`
- **Thay đổi quan trọng:** Bổ sung thêm trường chọn "Kho" (Warehouse) vào form đăng nhập. 
- **Giao diện:** 
  - Tại trang Login, sau khi nhập Email/Password, gọi API `GET /api/v1/qlk/warehouses` (API này cho phép truy cập public lấy danh sách kho đang hoạt động) để hiển thị một Dropdown (Select) danh sách Kho.
  - Dropdown này cho phép chọn kho (ví dụ: *Kho IT, Kho Vận Hành...*).
  - Đối với tài khoản `SUPER_ADMIN`, việc chọn kho là tùy chọn (Có thể chọn "Tất cả kho"). 
  - Đối với nhân viên thường, bắt buộc phải chọn Kho mà họ có quyền truy cập.
- **Payload Login:** `{ "email": "...", "password": "...", "warehouseId": 1 }`
- **Response JWT:** Token trả về sẽ chứa claim `warehouseId` bên trong.

## 3. PHÂN QUYỀN VÀ MENU (RBAC & SIDEBAR)

Hiển thị/Ẩn các Menu Sidebar dựa vào chuỗi Roles trả về từ API `GET /api/v1/auth/me` hoặc decode trực tiếp từ JWT.

Các Role chính trong phân hệ QLK:
- `SUPER_ADMIN`: Toàn quyền.
- `THU_KHO` (Thủ kho): Tạo/Sửa phiếu nhập xuất, quản lý vật tư, xem tồn kho.
- `KY_THUAT` (Kỹ thuật viên): Chỉ được xem tồn kho và danh mục vật tư.
- `GIAM_DOC` (Giám đốc/Quản lý): Xem mọi thứ, có quyền Duyệt / Từ chối phiếu xuất/nhập.

---

## 4. CÁC MODULE CHÍNH CẦN THIẾT KẾ UI (CORE MODULES)

### 4.1. Quản lý Danh mục (Master Data)
Các trang CRUD cơ bản (Dạng Table, có Pagination, nút Thêm/Sửa/Xóa mở Modal):
- **Quản lý Kho (`/api/v1/qlk/warehouses`):**
  - Chức năng: Thêm/Sửa/Xóa thông tin kho (Mã kho, Tên, Địa chỉ).
  - Chức năng nâng cao (Chỉ dành cho Admin): **Phân bổ nhân viên vào Kho** (Gọi API `POST /api/v1/qlk/warehouses/{id}/users` với danh sách `userIds`). UI là một Transfer Component hoặc Multi-Select Select.
- **Danh mục Vật tư (`/api/v1/qlk/categories`):** Quản lý nhóm vật tư (Ví dụ: Van, Ống, Bơm...).
- **Nhà Cung Cấp (`/api/v1/qlk/suppliers`):** Thông tin NCC (Tên, MST, Địa chỉ, Số điện thoại).

### 4.2. Quản lý Vật Tư (Materials)
**API Endpoint:** `/api/v1/qlk/materials`
- **Giao diện Danh sách:** Bảng hiển thị Tên vật tư, SKU, Đơn vị tính (Piece, Kg, Box...), Danh mục, Trạng thái (Hoạt động / Ngừng hoạt động) và Hình ảnh thu nhỏ (Thumbnail).
- **Giao diện Thêm/Sửa:**
  - Form điền thông tin chi tiết.
  - Cho phép upload ảnh vật tư (Gọi API Upload File hiện có của dự án và lấy URL lưu vào DB).
  - Có Auto-generate SKU nếu người dùng để trống.

### 4.3. Quản lý Phiếu Nhập / Xuất (Stock Vouchers) - 🌟 MODULE QUAN TRỌNG NHẤT
**API Endpoint gốc:** `/api/v1/qlk/warehouses/{warehouseId}/vouchers`
- **Giao diện Danh sách:** Hiển thị danh sách phiếu, lọc theo `type` (NHAP_KHO, XUAT_KHO) và lọc theo `status` (NHAP_BAN, CHO_DUYET, DA_DUYET, TU_CHOI).
- **Giao diện Tạo mới Phiếu (Master-Detail Form):**
  - Khối thông tin chung (Master): Ngày lập phiếu, Nhà cung cấp (Nếu là nhập), Loại phiếu, Ghi chú.
  - Khối chi tiết vật tư (Detail - Dạng Table Edit Inline hoặc Modal Add Line): 
    - Nút "Thêm vật tư", mở popup chọn vật tư.
    - Nhập Số lượng, Đơn giá (Hệ thống tự tính Thành tiền = SL * Đơn giá).
  - Khi lưu, gửi mảng `details: [{ materialId, quantity, price }]`.
- **Luồng Duyệt Phiếu (Approval Flow):**
  - Nút **"Trình duyệt"**: Dành cho Thủ kho (Đổi trạng thái `NHAP_BAN` -> `CHO_DUYET`). API: `POST .../{id}/submit`
  - Nút **"Phê duyệt"** / **"Từ chối"**: Dành cho Giám đốc/Admin (Từ trạng thái `CHO_DUYET`). API: `POST .../{id}/approve` (Kèm payload `{ "approved": true/false, "rejectReason": "..." }`).

### 4.4. Báo cáo Tồn Kho (Inventory & Ledger)
- **Tồn kho hiện tại (`GET .../inventory/stocks`):** Bảng báo cáo hiển thị: Tên Vật tư, Tồn kho thực tế (`quantity`), Tồn kho đang chờ xuất (`reservedQuantity`), Tồn kho khả dụng (`available = quantity - reservedQuantity`). Có thanh search theo tên vật tư/SKU.
- **Sổ cái Giao dịch (`GET .../inventory/transactions`):** Bảng hiển thị lịch sử nhập xuất: Ngày giờ, Người thực hiện, Số lượng trước thay đổi, Số lượng thay đổi (+/-), Số lượng sau thay đổi, Lý do (Ghi chú phiếu).

---

## 5. HƯỚNG DẪN DÀNH CHO AI (NẾU DÙNG AI GENERATE CODE)
- Hãy tạo layout theo phong cách Dashboard hiện đại (Modern Dashboard), sử dụng Card layout để bọc các bảng.
- Đối với Table, bắt buộc sử dụng cơ chế Server-side Pagination dựa trên object `ResultPaginationDTO` chuẩn của hệ thống: `{ meta: { current, pageSize, total, pages }, result: [...] }`.
- Các trạng thái (Status) như Phiếu (NHÁP, CHỜ DUYẾT, ĐÃ DUYỆT, TỪ CHỐI) cần được render bằng thẻ **Tag / Badge** có màu sắc tương ứng (Xám, Vàng/Cam, Xanh lá, Đỏ).
- Cần có các Loading State (Spinner/Skeleton) và Disable các nút Trình duyệt/Phê duyệt nếu User không đủ quyền (Dựa vào mảng Permissions trong Context).

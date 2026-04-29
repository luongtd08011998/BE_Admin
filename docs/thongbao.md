Bạn là một senior backend engineer. Hãy xây dựng một hệ thống backend Spring Boot production-ready cho ứng dụng mobile quản lý hóa đơn nước.

---

## 🎯 MỤC TIÊU

Xây dựng hệ thống:

- Đăng nhập khách hàng KHÔNG dùng password
- Lưu device để push notification
- Gửi thông báo khi:
  - Có hóa đơn mới
  - Thanh toán thành công

---

## 🧱 DATABASE HIỆN TẠI

Hệ thống đã có sẵn:

- `customer`
- `Moninvoice`
- `payment`

👉 KHÔNG được sửa hoặc tạo lại các bảng này

---

## 🔐 1. ĐĂNG NHẬP KHÁCH HÀNG (KHÔNG PASSWORD)

### Yêu cầu:

Khách hàng đăng nhập bằng:

- digiCode
- phone

---

### API:

POST /api/customer/auth/login

---

### Request:

{
"digiCode": "string",
"phone": "string"
}

---

### Xử lý:

- Tìm customer theo digiCode + phone
- Nếu tồn tại → cho login
- Tạo JWT token

---

### Response:

{
"accessToken": "...",
"customerId": ...,
"name": "..."
}

---

## 📱 2. LƯU DEVICE TOKEN

### Tạo bảng:

CREATE TABLE customer_device (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
customer_id INT NOT NULL,
device_token VARCHAR(255) NOT NULL,
platform VARCHAR(20),
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
UNIQUE KEY uk_customer_device (customer_id, device_token)
);

---

### API:

POST /api/customer/device/register

---

### Request:

{
"deviceToken": "string",
"platform": "ANDROID | IOS"
}

---

### Xử lý:

- Lấy customerId từ JWT
- Lưu device_token
- Không lưu trùng

---

## 🔔 3. BẢNG NOTIFICATION

### Tạo bảng:

CREATE TABLE notification (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
customer_id INT NOT NULL,
title VARCHAR(255),
content TEXT,
type VARCHAR(50), -- INVOICE, PAYMENT
is_read BOOLEAN DEFAULT FALSE,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

---

## 🔥 4. PUSH NOTIFICATION (FCM)

Sử dụng Firebase Cloud Messaging:

- Gửi theo device_token
- Một customer có nhiều device

---

## 📄 5. LOGIC NGHIỆP VỤ

---

### CASE 1: CÓ HÓA ĐƠN MỚI

Khi có bản ghi mới trong `invoices_invoice`:

- Lấy customer_id

- Insert notification:
  - title: "Hóa đơn mới"
  - content: "Bạn có hóa đơn tháng mới"

- Gửi push notification

---

### CASE 2: THANH TOÁN THÀNH CÔNG

Khi có bản ghi mới trong `payment`:

- Lấy customer_id

- Insert notification:
  - title: "Thanh toán thành công"
  - content: "Bạn đã thanh toán thành công"

- Gửi push notification

---

## 🧠 6. TRIỂN KHAI

KHÔNG dùng database trigger

👉 Khi tạo invoice/payment → gọi NotificationService

---

## 🧩 7. CÁC THÀNH PHẦN

### Entity:

- CustomerDevice
- Notification

### Repository:

- CustomerRepository
- CustomerDeviceRepository
- NotificationRepository

### Service:

- AuthService
- NotificationService
- FirebaseService

### Controller:

- AuthController
- DeviceController

---

## 🔐 8. SECURITY

- Dùng Spring Security + JWT
- Các API cần xác thực bằng token
- Lấy customerId từ JWT

---

## 📦 9. API BỔ SUNG

GET /api/customer/notifications
POST /api/customer/notifications/read

---

## ⚠️ LƯU Ý

- Không sửa DB cũ
- Code theo clean architecture
- Dùng DTO
- Xử lý exception đầy đủ

---

## 🎯 OUTPUT

Sinh full code:

- Entity
- Repository
- Service
- Controller
- JWT
- Firebase push

Code phải chạy được.

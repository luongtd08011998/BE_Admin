# Tài Liệu API Phân Hệ Quản Lý Kho (QLK)

> Tài liệu này mô tả chi tiết các RESTful API của phân hệ Quản Lý Kho (QLK) dành cho đội ngũ Frontend.
> **Base URL:** `http://localhost:8080` (hoặc domain cấu hình trên môi trường)
> **Phân trang:** Thêm query `?page=1&size=10` vào các API `GET` danh sách. Trả về bọc trong `data.result` và `data.meta`.
> **Response Chuẩn:** Mọi API đều trả về dạng bọc `ApiResponse`:
> ```json
> {
>   "statusCode": 200,
>   "message": "Success message",
>   "data": { ... }
> }
> ```

---

## 1. QUẢN LÝ KHO (WAREHOUSE)

### 1.1 Lấy danh sách Kho (Public)
Dùng tại màn hình đăng nhập hoặc quản trị.
- **URL:** `GET /api/v1/qlk/warehouses`
- **Quyền:** Public (Không cần Token)
- **Response Data:** `[ { "id": 1, "code": "IT", "name": "Kho IT", "address": null, "status": "HOAT_DONG" }, ... ]`

### 1.2 Lấy chi tiết 1 Kho
- **URL:** `GET /api/v1/qlk/warehouses/{id}`
- **Quyền:** `VIEW_WAREHOUSE`

### 1.3 Tạo Kho mới
- **URL:** `POST /api/v1/qlk/warehouses`
- **Quyền:** `CREATE_WAREHOUSE`
- **Payload:** `{ "code": "IT", "name": "Kho IT", "address": "Tầng 2", "status": "HOAT_DONG", "description": "" }`

### 1.4 Cập nhật Kho
- **URL:** `PUT /api/v1/qlk/warehouses/{id}`
- **Quyền:** `UPDATE_WAREHOUSE`
- **Payload:** Tương tự lúc Tạo mới.

### 1.5 Xóa Kho
- **URL:** `DELETE /api/v1/qlk/warehouses/{id}`
- **Quyền:** `DELETE_WAREHOUSE`

### 1.6 Phân bổ Nhân viên vào Kho (Admin)
- **URL:** `POST /api/v1/qlk/warehouses/{id}/users`
- **Quyền:** `ASSIGN_WAREHOUSE_USERS`
- **Payload:** `[1, 2, 3]` (Mảng các ID của `userId` cần phân quyền vào kho này)

---

## 2. QUẢN LÝ DANH MỤC VẬT TƯ (CATEGORY)

- **Lấy danh sách:** `GET /api/v1/qlk/categories` (Quyền: `VIEW_QLK_CATEGORIES`)
- **Lấy chi tiết:** `GET /api/v1/qlk/categories/{id}` (Quyền: `VIEW_QLK_CATEGORY`)
- **Tạo mới:** `POST /api/v1/qlk/categories` (Quyền: `CREATE_QLK_CATEGORY`)
  - Payload: `{ "name": "Ống nhựa", "description": "" }`
- **Cập nhật:** `PUT /api/v1/qlk/categories/{id}` (Quyền: `UPDATE_QLK_CATEGORY`)
- **Xóa:** `DELETE /api/v1/qlk/categories/{id}` (Quyền: `DELETE_QLK_CATEGORY`)

---

## 3. QUẢN LÝ NHÀ CUNG CẤP (SUPPLIER)

- **Lấy danh sách:** `GET /api/v1/qlk/suppliers` (Quyền: `VIEW_SUPPLIERS`)
- **Lấy chi tiết:** `GET /api/v1/qlk/suppliers/{id}` (Quyền: `VIEW_SUPPLIER`)
- **Tạo mới:** `POST /api/v1/qlk/suppliers` (Quyền: `CREATE_SUPPLIER`)
  - Payload: `{ "code": "NCC01", "name": "Cty Nước", "taxCode": "1234", "address": "...", "phone": "098", "email": "a@a.com" }`
- **Cập nhật:** `PUT /api/v1/qlk/suppliers/{id}` (Quyền: `UPDATE_SUPPLIER`)
- **Xóa:** `DELETE /api/v1/qlk/suppliers/{id}` (Quyền: `DELETE_SUPPLIER`)

---

## 4. QUẢN LÝ VẬT TƯ (MATERIAL)

- **Lấy danh sách:** `GET /api/v1/qlk/materials` (Quyền: `VIEW_MATERIALS`)
- **Lấy chi tiết:** `GET /api/v1/qlk/materials/{id}` (Quyền: `VIEW_MATERIAL`)
- **Tạo mới:** `POST /api/v1/qlk/materials` (Quyền: `CREATE_MATERIAL`)
  - Payload:
  ```json
  {
    "code": "VT-001",
    "name": "Bơm 3 pha",
    "categoryId": 1,
    "specification": "220V",
    "unit": "Cái",
    "unitPrice": 150000,
    "minStock": 10,
    "barcode": "",
    "imageUrl": "http://..."
  }
  ```
- **Cập nhật:** `PUT /api/v1/qlk/materials/{id}` (Quyền: `UPDATE_MATERIAL`)
  - Payload: Thêm trường `"status": "DANG_SU_DUNG"` hoặc `"NGUNG_SU_DUNG"`.
- **Xóa:** `DELETE /api/v1/qlk/materials/{id}` (Quyền: `DELETE_MATERIAL`)

---

## 5. PHIẾU NHẬP / XUAT (STOCK VOUCHER)
> Lưu ý: Mọi API phiếu đều đi kèm với `{warehouseId}` trên URL. Frontend lấy ID kho đang làm việc từ Context/JWT.

### 5.1 Lấy danh sách Phiếu
- **URL:** `GET /api/v1/qlk/warehouses/{warehouseId}/vouchers`
- **Quyền:** `VIEW_STOCK_VOUCHERS`
- **Query Filters:** `?type=NHAP_KHO&status=DA_DUYET`

### 5.2 Lấy chi tiết Phiếu (Gồm thông tin chung và danh sách chi tiết vật tư)
- **URL:** `GET /api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}`
- **Quyền:** `VIEW_STOCK_VOUCHER`

### 5.3 Tạo Phiếu mới
- **URL:** `POST /api/v1/qlk/warehouses/{warehouseId}/vouchers`
- **Quyền:** `CREATE_STOCK_VOUCHER`
- **Payload:**
```json
{
  "type": "NHAP_KHO", 
  "supplierId": 1, 
  "note": "Nhập đợt 1",
  "details": [
    {
      "materialId": 1,
      "quantity": 10,
      "price": 50000,
      "note": "Hàng mới"
    }
  ]
}
```
*(Ghi chú: Giá trị `type` có thể là `NHAP_KHO` hoặc `XUAT_KHO`. Với xuất kho, `supplierId` có thể null)*. Trạng thái sau khi tạo là `NHAP_BAN`.

### 5.4 Sửa Phiếu (Chỉ khi ở trạng thái NHÁP)
- **URL:** `PUT /api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}`
- **Quyền:** `UPDATE_STOCK_VOUCHER`
- **Payload:** Giống lúc tạo.

### 5.5 Xóa Phiếu (Chỉ khi ở trạng thái NHÁP)
- **URL:** `DELETE /api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}`
- **Quyền:** `DELETE_STOCK_VOUCHER`

### 5.6 Trình duyệt Phiếu (Thủ kho)
- **URL:** `POST /api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}/submit`
- **Quyền:** `SUBMIT_STOCK_VOUCHER`
- **Payload:** Trống `{}`. Trạng thái chuyển từ `NHAP_BAN` -> `CHO_DUYET`.

### 5.7 Phê duyệt / Từ chối Phiếu (Giám đốc / Admin)
- **URL:** `POST /api/v1/qlk/warehouses/{warehouseId}/vouchers/{id}/approve`
- **Quyền:** `APPROVE_STOCK_VOUCHER`
- **Payload:**
```json
{
  "approved": true, 
  "rejectReason": ""
}
```
*(Gửi `approved: false` và `rejectReason` nếu từ chối. Nếu Duyệt (`true`), tồn kho sẽ chính thức được cộng/trừ)*.

---

## 6. BÁO CÁO TỒN KHO & SỔ CÁI (INVENTORY)

### 6.1 Xem Báo Cáo Tồn Kho Hiện Tại
- **URL:** `GET /api/v1/qlk/warehouses/{warehouseId}/inventory/stocks`
- **Quyền:** `VIEW_INVENTORY`
- **Response Data:** Mảng các object chứa: `materialId, materialName, unit, quantity (tồn thực), reservedQuantity (chờ xuất)`.

### 6.2 Xem Sổ Cái Giao Dịch (Lịch sử Nhập/Xuất)
- **URL:** `GET /api/v1/qlk/warehouses/{warehouseId}/inventory/transactions`
- **Quyền:** `VIEW_INVENTORY_TX`
- **Response Data:** Danh sách các biến động: `transactionType, quantityBefore, quantityChange, quantityAfter, referenceVoucherId...`

### 6.3 Xem Chốt Kho Tồn Đầu Kỳ (Nâng cao)
- **URL:** `GET /api/v1/qlk/warehouses/{warehouseId}/inventory/snapshots`
- **Quyền:** `VIEW_INVENTORY_SNAPSHOT`

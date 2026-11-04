# Tài Liệu Đặc Tả REST API — Module Giao Việc (`gv`)
### Dành cho Đội ngũ Phát triển Mobile (Flutter / React Native / iOS / Android)

---

## 📌 1. Thông Tin Chung & Quy Chuẩn (General Information)

### 1.1. Base URL
- **Môi trường Dev/Local**: `http://<IP_SERVER>:8080/api/v1/gv`
- **Môi trường Production**: `https://<DOMAIN>/api/v1/gv`

### 1.2. Xác thực (Authentication)
Tất cả các API trong module Giao việc đều yêu cầu gửi JWT Access Token trong HTTP Header:
```http
Authorization: Bearer <access_token>
Content-Type: application/json
```
*(Token lấy từ API đăng nhập `/api/v1/auth/login` của hệ thống).*

---

### 1.3. Cấu trúc Response chuẩn (`ApiResponse<T>`)

Mọi response thành công đều bọc trong object `ApiResponse`:
```json
{
  "statusCode": 200,
  "message": "Gọi API thành công",
  "data": { ... }
}
```

Cấu trúc khi có lỗi:
```json
{
  "statusCode": 400,
  "error": "BAD_REQUEST",
  "message": "Nội dung thông báo lỗi chi tiết"
}
```

---

### 1.4. Quy chuẩn Phân trang (Pagination)
Các endpoint danh sách có phân trang sử dụng các query params sau:
- `page`: Số trang, bắt đầu từ `0` (mặc định: `0`).
- `size`: Số phần tử trên 1 trang (mặc định: `10` hoặc `20`).
- `sort`: Tên trường sắp xếp, chiều sắp xếp (VD: `createdAt,desc` hoặc `dueDate,asc`).

Cấu trúc trả về dạng `Page<T>`:
```json
{
  "content": [ ... ],
  "pageable": { "pageNumber": 0, "pageSize": 10 },
  "totalElements": 45,
  "totalPages": 5,
  "last": false,
  "first": true,
  "empty": false
}
```

---

## 🎨 2. Bảng Tra Cứu Enums & Gợi Ý Giao Diện Mobile

### 2.1. Trạng thái công việc (`GvTaskStatus`)
| Giá trị Enum | Tên hiển thị (VN) | Mã màu HEX | Gợi ý hiển thị trên Mobile |
| :--- | :--- | :---: | :--- |
| `TO_DO` | Mới giao / Chưa làm | `#64748B` | Tag xám nhạt |
| `IN_PROGRESS` | Đang thực hiện | `#0284C7` | Tag xanh dương, nút bấm "Nộp việc" |
| `PENDING_REVIEW` | Chờ duyệt | `#EAB308` | Tag vàng cam, cảnh báo sếp cần duyệt |
| `REJECTED` | Bị trả lại / Chưa đạt | `#EF4444` | Tag đỏ, hiển thị lý do trả lại |
| `DONE` | Đã nghiệm thu xong | `#22C55E` | Tag xanh lá kèm icon check ✅ |
| `CANCELLED` | Đã hủy bỏ | `#94A3B8` | Tag xám gạch ngang chữ |

### 2.2. Độ ưu tiên công việc (`GvTaskPriority`)
| Giá trị Enum | Tên hiển thị | Mã màu HEX | Icon gợi ý |
| :--- | :--- | :---: | :--- |
| `LOW` | Thấp | `#94A3B8` | 🔽 Mũi tên xanh lá/xám |
| `NORMAL` | Bình thường | `#3B82F6` | ➖ Dấu gạch xanh dương |
| `HIGH` | Cao | `#F97316` | 🔼 Mũi tên cam |
| `URGENT` | Khẩn cấp | `#DC2626` | 🔥 Ngọn lửa đỏ |

### 2.3. Loại thông báo (`GvNotificationType`)
| Giá trị Enum | Mô tả sự kiện | Hành động khi user click vào thông báo trên Mobile |
| :--- | :--- | :--- |
| `TASK_ASSIGNED` | Bạn được giao việc mới | Mở màn hình Chi tiết công việc (`taskId`) |
| `DEADLINE_REMINDER` | Việc sắp đến hạn chót (trước 2h) | Mở màn hình Chi tiết công việc (`taskId`) |
| `OVERDUE_ALERT` | Việc đã quá hạn | Mở màn hình Chi tiết công việc (`taskId`) |
| `REVIEW_SUBMITTED` | Nhân viên nộp việc chờ bạn duyệt | Mở màn hình Phê duyệt công việc (`taskId`) |
| `REVIEW_APPROVED` | Công việc đã được duyệt xong | Mở màn hình Chi tiết công việc (`taskId`) |
| `REVIEW_REJECTED` | Công việc bị sếp trả lại | Mở màn hình Chi tiết (xem feedback để làm lại) |
| `TASK_COMMENTED` | Có bình luận / trao đổi mới | Mở tab Thảo luận trong chi tiết công việc |
| `RECURRING_OVERDUE` | Cảnh báo nợ việc định kỳ | Mở danh sách công việc |

---

## 🚀 3. Chi Tiết Các Nhóm API

---

### 📂 PHÂN HỆ 1: CÔNG VIỆC CHÍNH & KANBAN (Tasks)

#### 1. Lấy danh sách công việc (Tìm kiếm & Phân trang)
- **Method**: `GET`
- **URL**: `/api/v1/gv/tasks`
- **Query Params (Tùy chọn)**:
  - `keyword`: Tìm theo mã việc hoặc tiêu đề (VD: `TASK-202609-0001` hoặc `Báo cáo`)
  - `status`: Lọc theo trạng thái đơn (`TO_DO`, `IN_PROGRESS`, `PENDING_REVIEW`, `DONE`...)
  - `priority`: Lọc theo độ ưu tiên (`LOW`, `NORMAL`, `HIGH`, `URGENT`)
  - `departmentId`: Lọc theo ID phòng ban
  - `assigneeId`: Lọc theo người nhận việc
  - `creatorId`: Lọc theo người giao việc
  - `isOverdue`: `true` (chỉ lấy việc đã quá hạn), `false` (lấy việc chưa quá hạn)
  - `isRoot`: `true` (chỉ lấy việc cha, bỏ qua việc con)
  - `dueDateFrom`: `2026-09-01T00:00:00Z`
  - `dueDateTo`: `2026-09-30T23:59:59Z`
  - `page`: `0`, `size`: `20`, `sort`: `dueDate,asc`
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "message": "Gọi API thành công",
  "data": {
    "content": [
      {
        "id": 1,
        "code": "TASK-202609-0001",
        "title": "Lập báo cáo tài chính quý 3",
        "description": "Rà soát toàn bộ số liệu thu chi quý 3",
        "creatorId": 10,
        "creatorName": "Nguyễn Văn Giám Đốc",
        "assigneeId": 25,
        "assigneeName": "Trần Thị Kế Toán",
        "assigneeEmail": "ketoan@congty.com",
        "departmentId": 2,
        "departmentName": "Phòng Kế Toán",
        "parentId": null,
        "parentTitle": null,
        "status": "IN_PROGRESS",
        "priority": "HIGH",
        "progress": 67,
        "startDate": "2026-09-28T07:00:00Z",
        "dueDate": "2026-09-30T17:00:00Z",
        "completedAt": null,
        "reviewedById": null,
        "reviewedByName": null,
        "reviewedAt": null,
        "reviewFeedback": null,
        "subTasksCount": 0,
        "checklistsCount": 3,
        "completedChecklistsCount": 2,
        "labels": [
          { "id": 1, "name": "Báo cáo", "color": "#3B82F6" }
        ],
        "createdAt": "2026-09-28T07:00:00Z",
        "updatedAt": "2026-09-28T08:30:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

#### 2. Lấy dữ liệu Bảng Kanban (6 Cột Trạng Thái)
- **Method**: `GET`
- **URL**: `/api/v1/gv/tasks/kanban`
- **Query Params**:
  - `departmentId`: Lọc theo phòng ban (tùy chọn)
  - `assigneeId`: Lọc theo người nhận việc (tùy chọn)
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "message": "Gọi API thành công",
  "data": {
    "todo": [ /* danh sách TaskResponse trạng thái TO_DO */ ],
    "inProgress": [ /* danh sách TaskResponse trạng thái IN_PROGRESS */ ],
    "pendingReview": [ /* danh sách TaskResponse trạng thái PENDING_REVIEW */ ],
    "rejected": [ /* danh sách TaskResponse trạng thái REJECTED */ ],
    "done": [ /* danh sách TaskResponse trạng thái DONE */ ],
    "cancelled": [ /* danh sách TaskResponse trạng thái CANCELLED */ ]
  }
}
```

---

#### 3. Thống kê Dashboard (Hiển thị thẻ đếm số lượng trên Mobile Home)
- **Method**: `GET`
- **URL**: `/api/v1/gv/tasks/dashboard/stats`
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "message": "Gọi API thành công",
  "data": {
    "totalTasks": 24,
    "todoTasks": 5,
    "inProgressTasks": 8,
    "pendingReviewTasks": 3,
    "rejectedTasks": 1,
    "doneTasks": 7,
    "cancelledTasks": 0,
    "overdueTasks": 2,
    "myAssignedTasks": 9,
    "myCreatedTasks": 15
  }
}
```

---

#### 4. Chi tiết công việc (Task Detail — Màn hình trung tâm trên Mobile)
- **Method**: `GET`
- **URL**: `/api/v1/gv/tasks/{id}`
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "message": "Gọi API thành công",
  "data": {
    "task": {
      "id": 1,
      "code": "TASK-202609-0001",
      "title": "Lập báo cáo tài chính quý 3",
      "description": "Chi tiết yêu cầu...",
      "creatorId": 10,
      "creatorName": "Nguyễn Văn Giám Đốc",
      "assigneeId": 25,
      "assigneeName": "Trần Thị Kế Toán",
      "status": "PENDING_REVIEW",
      "priority": "HIGH",
      "progress": 100,
      "startDate": "2026-09-28T07:00:00Z",
      "dueDate": "2026-09-30T17:00:00Z",
      "labels": [{ "id": 1, "name": "Báo cáo", "color": "#3B82F6" }]
    },
    "subTasks": [],
    "checklists": [
      {
        "id": 101,
        "taskId": 1,
        "title": "Thu thập số liệu thu chi",
        "isCompleted": true,
        "completedByName": "Trần Thị Kế Toán",
        "completedAt": "2026-09-28T08:00:00Z",
        "sortOrder": 0
      }
    ],
    "comments": [
      {
        "id": 501,
        "taskId": 1,
        "userId": 25,
        "userName": "Trần Thị Kế Toán",
        "content": "Em đã hoàn thành các mục và gửi kèm file số liệu ạ.",
        "createdAt": "2026-09-28T09:00:00Z"
      }
    ],
    "attachments": [
      {
        "id": 801,
        "taskId": 1,
        "fileName": "BaoCao_Q3.pdf",
        "fileUrl": "https://cdn.congty.com/uploads/BaoCao_Q3.pdf",
        "fileSize": 2048500,
        "fileType": "application/pdf",
        "uploadedByName": "Trần Thị Kế Toán",
        "uploadedAt": "2026-09-28T09:00:00Z"
      }
    ],
    "watchers": [
      {
        "id": 12,
        "taskId": 1,
        "userId": 5,
        "userName": "Lê Văn Trưởng Phòng Kế Toán",
        "addedByName": "Nguyễn Văn Giám Đốc"
      }
    ]
  }
}
```

---

#### 5. Tạo công việc mới
- **Method**: `POST`
- **URL**: `/api/v1/gv/tasks`
- **Request Body**:
```json
{
  "title": "Kiểm kê kho hàng cuối tháng",
  "description": "Kiểm kê toàn bộ số lượng thực tế tại kho chính",
  "assigneeId": 25,
  "departmentId": 2,
  "parentId": null,
  "priority": "HIGH",
  "startDate": "2026-09-28T08:00:00Z",
  "dueDate": "2026-09-30T17:00:00Z",
  "labelIds": [1, 3],
  "watcherIds": [5, 8]
}
```

---

#### 6. Chuyển trạng thái công việc (Nhân viên bắt đầu làm)
- **Method**: `PATCH`
- **URL**: `/api/v1/gv/tasks/{id}/status`
- **Request Body**:
```json
{
  "status": "IN_PROGRESS",
  "note": "Bắt đầu triển khai công việc"
}
```

---

#### 7. Nộp việc chờ duyệt (Submit for Review)
- **Method**: `POST`
- **URL**: `/api/v1/gv/tasks/{id}/submit-review`
- **Request Body**: Không cần body (hoặc gửi `{}`)
- **Tác vụ hệ thống**: Tự động chuyển status thành `PENDING_REVIEW` và gửi notification cho Sếp/Người giao việc.

---

#### 8. Sếp nghiệm thu / Trả lại công việc (Review)
- **Method**: `POST`
- **URL**: `/api/v1/gv/tasks/{id}/review`
- **Trường hợp 1 — Duyệt hoàn thành**:
```json
{
  "approved": true,
  "feedback": "Báo cáo rất chi tiết, nghiệm thu đạt."
}
```
*(Status chuyển sang `DONE`, `completedAt` ghi nhận thời gian hiện tại).*

- **Trường hợp 2 — Trả lại yêu cầu sửa đổi**:
```json
{
  "approved": false,
  "feedback": "Số liệu tháng 8 chưa khớp với bảng kê ngân hàng, làm lại bảng đối soát."
}
```
*(Status chuyển sang `REJECTED`, nhân viên nhận thông báo và làm lại).*

---

#### 9. Thêm / Xóa người theo dõi (Watchers)
- **Thêm người theo dõi**: `POST /api/v1/gv/tasks/{id}/watchers/{userId}`
- **Xóa người theo dõi**: `DELETE /api/v1/gv/tasks/{id}/watchers/{userId}`

---

#### 10. Hủy bỏ công việc
- **Method**: `POST`
- **URL**: `/api/v1/gv/tasks/{id}/cancel?reason=Dự án tạm hoãn theo chỉ đạo`

---

### 📂 PHÂN HỆ 2: ĐẦU VIỆC CON / CHECKLISTS

#### 1. Lấy danh sách checklist của task
- **Method**: `GET`
- **URL**: `/api/v1/gv/tasks/{taskId}/checklists`

#### 2. Thêm mới 1 checklist item
- **Method**: `POST`
- **URL**: `/api/v1/gv/tasks/{taskId}/checklists`
- **Request Body**:
```json
{
  "title": "Kiểm tra hóa đơn VAT đầu vào",
  "sortOrder": 1
}
```

#### 3. Tick hoàn thành / Bỏ tick checklist
- **Method**: `PATCH`
- **URL**: `/api/v1/gv/tasks/{taskId}/checklists/{checklistId}/toggle`
> **Lưu ý quan trọng cho Mobile**: Mỗi khi gọi API toggle này, backend **tự động tính lại % tiến độ (`progress`)** của task cha. Sau khi tick, Mobile có thể cập nhật thanh Progress Bar trên UI.

#### 4. Xóa checklist item
- **Method**: `DELETE`
- **URL**: `/api/v1/gv/tasks/{taskId}/checklists/{checklistId}`

---

### 📂 PHÂN HỆ 3: TRAO ĐỔI & BÌNH LUẬN (Comments)

#### 1. Lấy danh sách bình luận
- **Method**: `GET`
- **URL**: `/api/v1/gv/tasks/{taskId}/comments`

#### 2. Đăng bình luận mới
- **Method**: `POST`
- **URL**: `/api/v1/gv/tasks/{taskId}/comments`
- **Request Body**:
```json
{
  "content": "Sếp ơi em đã tải file cập nhật lên rồi nhé."
}
```
*(Backend tự động bắn thông báo cho Assignee, Creator và các Watchers).*

#### 3. Xóa bình luận
- **Method**: `DELETE`
- **URL**: `/api/v1/gv/tasks/{taskId}/comments/{commentId}`

---

### 📂 PHÂN HỆ 4: TÀI LIỆU & FILE MINH CHỨNG (Attachments)

#### 1. Lưu thông tin file đã đính kèm vào task
- **Method**: `POST`
- **URL**: `/api/v1/gv/tasks/{taskId}/attachments`
- **Request Body**:
```json
{
  "commentId": null,
  "fileName": "BienBanKiemKe.pdf",
  "fileUrl": "https://storage.congty.com/tasks/BienBanKiemKe.pdf",
  "fileSize": 1048576,
  "fileType": "application/pdf"
}
```

#### 2. Xóa file đính kèm
- **Method**: `DELETE`
- **URL**: `/api/v1/gv/tasks/{taskId}/attachments/{attachmentId}`

---

### 📂 PHÂN HỆ 5: CHUÔNG THÔNG BÁO (Notifications)

#### 1. Lấy số lượng thông báo chưa đọc (Dùng cho Badge đỏ trên App Bar)
- **Method**: `GET`
- **URL**: `/api/v1/gv/notifications/unread-count`
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "message": "Gọi API thành công",
  "data": 3
}
```

#### 2. Lấy danh sách thông báo của tôi (Phân trang)
- **Method**: `GET`
- **URL**: `/api/v1/gv/notifications/my-notifications?page=0&size=20`
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "message": "Gọi API thành công",
  "data": {
    "content": [
      {
        "id": 10,
        "taskId": 1,
        "taskCode": "TASK-202609-0001",
        "taskTitle": "Lập báo cáo tài chính quý 3",
        "type": "TASK_ASSIGNED",
        "title": "Bạn được giao công việc mới: TASK-202609-0001",
        "content": "Nguyễn Văn Giám Đốc đã giao việc: Lập báo cáo tài chính quý 3",
        "isRead": false,
        "readAt": null,
        "createdAt": "2026-09-28T08:00:00Z"
      }
    ],
    "totalElements": 1
  }
}
```

#### 3. Đánh dấu 1 thông báo đã đọc
- **Method**: `PATCH`
- **URL**: `/api/v1/gv/notifications/{id}/mark-read`

#### 4. Đánh dấu tất cả thông báo là đã đọc
- **Method**: `PATCH`
- **URL**: `/api/v1/gv/notifications/mark-all-read`

---

### 📂 PHÂN HỆ 6: PHÒNG BAN & NHÃN (Metadata Dropdowns)

#### 1. Lấy danh sách phòng ban đang hoạt động (Dropdown chọn phòng)
- **Method**: `GET`
- **URL**: `/api/v1/gv/departments/active`
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "data": [
    {
      "id": 1,
      "code": "BGD",
      "name": "Ban Giám Đốc",
      "managerId": 10,
      "managerName": "Nguyễn Văn Giám Đốc",
      "isActive": true
    },
    {
      "id": 2,
      "code": "KT",
      "name": "Phòng Kế Toán",
      "managerId": 5,
      "managerName": "Lê Văn Trưởng Phòng Kế Toán",
      "isActive": true
    }
  ]
}
```

#### 2. Lấy danh sách nhãn màu (Dropdown / Filter Chips)
- **Method**: `GET`
- **URL**: `/api/v1/gv/labels/active`
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "data": [
    { "id": 1, "name": "Báo cáo", "color": "#3B82F6", "description": "Việc làm báo cáo định kỳ" },
    { "id": 2, "name": "Khẩn cấp", "color": "#EF4444", "description": "Cần giải quyết ngay" },
    { "id": 3, "name": "Hành chính", "color": "#10B981", "description": "Giấy tờ hành chính" }
  ]
}
```

---

### 📂 PHÂN HỆ 7: VIỆC ĐỊNH KỲ (Recurring Templates)

#### 1. Lấy danh sách việc định kỳ đang hoạt động
- **Method**: `GET`
- **URL**: `/api/v1/gv/recurring-templates/active`

#### 2. Tạo khuôn mẫu việc định kỳ mới
- **Method**: `POST`
- **URL**: `/api/v1/gv/recurring-templates`
- **Request Body**:
```json
{
  "title": "Sao lưu dữ liệu định kỳ tuần",
  "description": "Backup database và nén dữ liệu lưu trữ server dự phòng",
  "departmentId": 3,
  "defaultAssigneeId": 18,
  "frequency": "WEEKLY",
  "dayOfWeek": 1,
  "dayOfMonth": null,
  "timeOfDay": "07:30",
  "deadlineDaysOffset": 1,
  "overdueAction": "ALERT_AND_CREATE"
}
```
*(Backend có Cron Job chạy ngầm lúc 06:00 sáng mỗi ngày để tự động sinh công việc mới theo đúng chu kỳ).*

#### 3. Bật / Tắt việc định kỳ
- **Method**: `PATCH`
- **URL**: `/api/v1/gv/recurring-templates/{id}/toggle-active`

---

### 📂 PHÂN HỆ 8: NHẬT KÝ KIỂM TOÁN (Audit Logs)

#### 1. Lấy lịch sử thay đổi của 1 công việc (Activity Timeline)
- **Method**: `GET`
- **URL**: `/api/v1/gv/audit-logs?entityName=GvTask&entityId={taskId}&page=0&size=20`
- **Response mẫu (200 OK)**:
```json
{
  "statusCode": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "action": "CREATE_TASK",
        "entityName": "GvTask",
        "entityId": 1,
        "details": "action=CREATE_TASK, entity=GvTask, status=SUCCESS",
        "ipAddress": "192.168.1.10",
        "timestamp": "2026-09-28T07:00:00Z"
      },
      {
        "id": 2,
        "action": "REVIEW_TASK",
        "entityName": "GvTask",
        "entityId": 1,
        "details": "action=REVIEW_TASK, entity=GvTask, status=SUCCESS",
        "ipAddress": "192.168.1.12",
        "timestamp": "2026-09-28T10:15:00Z"
      }
    ]
  }
}
```

---

## 💡 4. Gợi Ý Luồng Nghiệp Vụ Cho Mobile (UX Recommendations)

1. **Màn hình Home / Dashboard**:
   - Gọi `GET /api/v1/gv/tasks/dashboard/stats` để vẽ các thẻ thống kê tổng quan (Việc của tôi, Quá hạn, Chờ duyệt...).
   - Gọi `GET /api/v1/gv/notifications/unread-count` để hiển thị chấm đỏ trên icon quả chuông góc phải trên cùng.
2. **Màn hình Danh sách & Kanban**:
   - Cho phép user chuyển đổi qua lại giữa dạng **List** (`GET /api/v1/gv/tasks`) và dạng **Kanban cột ngang** (`GET /api/v1/gv/tasks/kanban`).
   - Sử dụng các mã màu HEX trả về trong trường `color` của `labels` và bảng mã màu trong mục **2.1** để vẽ tag chip đồng bộ.
3. **Màn hình Chi tiết Công việc**:
   - Hiển thị thanh tiến độ `%` (tính từ `progress`).
   - Hiển thị danh sách Checklists với checkbox để user tick (`toggle`) trực tiếp.
   - Nút hành động theo vai trò:
     - Nếu user là **Người nhận việc** và task đang `IN_PROGRESS`: Hiện nút **"Nộp việc chờ duyệt"** (`POST .../submit-review`).
     - Nếu user là **Người giao việc / Quản lý** và task đang `PENDING_REVIEW`: Hiện 2 nút **"Đồng ý nghiệm thu"** và **"Trả lại / Yêu cầu sửa"** (`POST .../review`).

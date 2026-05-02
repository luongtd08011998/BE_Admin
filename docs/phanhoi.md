Kế Hoạch: Thông Báo Người Dùng Khi Admin Phản Hồi / Đổi Trạng Thái Phản Ánh
Tổng Quan
Khi admin nhận được phản ánh từ người dùng, admin sẽ thực hiện một trong hai hành động:

Đổi trạng thái (PENDING → PROCESSING → RESOLVED / REJECTED)
Gửi tin nhắn phản hồi (reply từ staff)
Sau đó, hệ thống cần chủ động thông báo cho người dùng về sự thay đổi này thông qua Firebase Cloud Messaging (FCM) Push Notification.

Luồng Hoạt Động (Flow)
Admin đổi trạng thái / gửi reply
           ↓
     Backend Spring Boot
           ↓
 Tạo Notification record trong DB (type=FEEDBACK)
           ↓
 Gọi FCM API → Push đến thiết bị người dùng
           ↓
Mobile App nhận Push Notification
           ↓
   Tap → Deep link → FeedbackDetailScreen
Phần 1: Backend (Spring Boot)
1.1. Trigger Thông Báo Tự Động
Khi admin thực hiện đổi trạng thái hoặc thêm reply, Backend cần:

A. Khi Admin đổi trạng thái phản ánh:

PATCH /api/v1/admin/feedbacks/{id}/status
Body: { "status": "RESOLVED" }
→ Sau khi lưu DB, gọi NotificationService.notifyFeedbackStatusChanged(feedback, newStatus)

B. Khi Admin gửi reply:

POST /api/v1/admin/feedbacks/{id}/replies
Body: { "content": "Chúng tôi đã tiếp nhận..." }
→ Sau khi lưu reply, gọi NotificationService.notifyFeedbackReply(feedback, replyContent)

1.2. Notification Record trong DB
Tạo bản ghi Notification cho user (loại FEEDBACK) để hiển thị trong tab thông báo:

kotlin
// Cấu trúc record cần tạo
Notification(
    customerId = feedback.customer.id,   // user nhận thông báo
    title = "Phản ánh của bạn đã được cập nhật",
    content = "Trạng thái: Đang xử lý | PHKH001-001",
    type = "FEEDBACK",
    referenceId = feedback.id,           // dùng để deep link
    isSystem = false,                    // thông báo cá nhân
    isRead = false
)
1.3. FCM Push Notification
Backend gọi FCM để push đến device token của user (đã lưu khi login):

json
// FCM Payload
{
  "token": "<device_fcm_token>",
  "notification": {
    "title": "Phản ánh của bạn đã được cập nhật",
    "body": "PHKH001-001: Đã xử lý xong"
  },
  "data": {
    "type": "FEEDBACK",
    "referenceId": "1"
  }
}

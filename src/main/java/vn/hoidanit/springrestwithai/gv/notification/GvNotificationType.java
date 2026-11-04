package vn.hoidanit.springrestwithai.gv.notification;

public enum GvNotificationType {
    TASK_ASSIGNED,          // Bạn được giao công việc mới
    DEADLINE_REMINDER,      // Công việc sắp đến hạn (nhắc trước X tiếng)
    OVERDUE_ALERT,          // Công việc đã quá hạn
    REVIEW_SUBMITTED,       // Nhân viên đã nộp việc, chờ bạn duyệt
    REVIEW_APPROVED,        // Công việc của bạn đã được duyệt hoàn thành
    REVIEW_REJECTED,        // Công việc bị từ chối/yêu cầu làm lại
    RECURRING_OVERDUE,      // Cảnh báo nợ việc định kỳ
    TASK_COMMENTED          // Có trao đổi mới trong công việc
}

package vn.hoidanit.springrestwithai.gv.task;

public enum GvTaskStatus {
    TO_DO,              // Mới tạo/giao việc, chưa bắt đầu
    IN_PROGRESS,        // Đang thực hiện
    PENDING_REVIEW,     // Nhân viên đã xong việc, nộp chờ sếp duyệt
    REJECTED,           // Sếp trả lại / chưa đạt, yêu cầu bổ sung
    DONE,               // Đã nghiệm thu hoàn thành
    CANCELLED           // Hủy bỏ công việc
}

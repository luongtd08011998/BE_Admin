package vn.hoidanit.springrestwithai.gv.recurring;

public enum GvOverdueAction {
    ALERT_AND_CREATE,   // Vẫn sinh task kỳ mới + gửi cảnh báo nợ việc kỳ cũ
    SKIP_UNTIL_DONE     // Tạm dừng sinh task mới cho đến khi hoàn thành task cũ
}

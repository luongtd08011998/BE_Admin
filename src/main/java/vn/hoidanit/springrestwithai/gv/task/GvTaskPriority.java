package vn.hoidanit.springrestwithai.gv.task;

public enum GvTaskPriority {
    LOW,        // Thấp, có thể xử lý khi rảnh
    NORMAL,     // Bình thường (mặc định)
    HIGH,       // Quan trọng, ưu tiên xử lý trong tuần
    URGENT      // Khẩn cấp, cần giải quyết ngay lập tức
}

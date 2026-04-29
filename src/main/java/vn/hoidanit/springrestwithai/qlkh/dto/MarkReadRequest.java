package vn.hoidanit.springrestwithai.qlkh.dto;

import java.util.List;

public class MarkReadRequest {

    /** Danh sách id thông báo cần đánh dấu đã đọc. Nếu null hoặc rỗng → đánh dấu tất cả. */
    private List<Long> ids;
    
    private Boolean isSystem;

    public List<Long> getIds() { return ids; }

    public void setIds(List<Long> ids) { this.ids = ids; }

    public Boolean getIsSystem() { return isSystem; }

    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }
}

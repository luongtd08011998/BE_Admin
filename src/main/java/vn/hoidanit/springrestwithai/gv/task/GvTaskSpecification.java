package vn.hoidanit.springrestwithai.gv.task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.springrestwithai.gv.task.dto.GvTaskFilterRequest;

public class GvTaskSpecification {

    public static Specification<GvTask> build(GvTaskFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Keyword (mã việc hoặc tiêu đề)
            if (filter.keyword() != null && !filter.keyword().isBlank()) {
                String pattern = "%" + filter.keyword().toLowerCase().trim() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("code")), pattern)
                ));
            }

            // Trạng thái đơn lẻ
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            // Danh sách nhiều trạng thái
            if (filter.statuses() != null && !filter.statuses().isEmpty()) {
                predicates.add(root.get("status").in(filter.statuses()));
            }

            // Độ ưu tiên
            if (filter.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.priority()));
            }

            // Phòng ban
            if (filter.departmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("id"), filter.departmentId()));
            }

            // Người nhận việc
            if (filter.assigneeId() != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), filter.assigneeId()));
            }

            // Người giao việc
            if (filter.creatorId() != null) {
                predicates.add(cb.equal(root.get("creator").get("id"), filter.creatorId()));
            }

            // Việc cha
            if (filter.parentId() != null) {
                predicates.add(cb.equal(root.get("parent").get("id"), filter.parentId()));
            }

            // Chỉ lấy việc cha (root)
            if (Boolean.TRUE.equals(filter.isRoot())) {
                predicates.add(cb.isNull(root.get("parent")));
            }

            // Quá hạn: dueDate < now && status NOT IN (DONE, CANCELLED)
            if (Boolean.TRUE.equals(filter.isOverdue())) {
                predicates.add(cb.lessThan(root.get("dueDate"), Instant.now()));
                predicates.add(root.get("status").in(GvTaskStatus.DONE, GvTaskStatus.CANCELLED).not());
            }

            // Khoảng ngày dueDate
            if (filter.dueDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), filter.dueDateFrom()));
            }
            if (filter.dueDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), filter.dueDateTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

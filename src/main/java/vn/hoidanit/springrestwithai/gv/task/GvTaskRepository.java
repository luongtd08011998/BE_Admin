package vn.hoidanit.springrestwithai.gv.task;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GvTaskRepository extends JpaRepository<GvTask, Long>, JpaSpecificationExecutor<GvTask> {

    Optional<GvTask> findByCode(String code);

    boolean existsByCode(String code);

    List<GvTask> findByParentId(Long parentId);

    List<GvTask> findByAssigneeId(Long assigneeId);

    List<GvTask> findByCreatorId(Long creatorId);

    List<GvTask> findByDepartmentId(Long departmentId);

    List<GvTask> findByDueDateBeforeAndStatusNotIn(Instant date, Collection<GvTaskStatus> statuses);

    List<GvTask> findByDueDateBetweenAndStatusNotIn(Instant start, Instant end, Collection<GvTaskStatus> statuses);

    List<GvTask> findByStatusNotIn(Collection<GvTaskStatus> statuses);

    long countByAssigneeIdAndStatus(Long assigneeId, GvTaskStatus status);

    long countByDepartmentIdAndStatus(Long departmentId, GvTaskStatus status);

    long countByDepartmentId(Long departmentId);

    long countByAssigneeId(Long assigneeId);

    long countByCreatorId(Long creatorId);

    long countByDepartmentIdAndDueDateBeforeAndStatusNotIn(Long departmentId, Instant date, Collection<GvTaskStatus> statuses);

    long countByAssigneeIdAndDueDateBeforeAndStatusNotIn(Long assigneeId, Instant date, Collection<GvTaskStatus> statuses);

    // Đếm theo trạng thái toàn hệ thống (dùng cho Dashboard Summary)
    long countByStatus(GvTaskStatus status);

    // Đếm số việc quá hạn toàn hệ thống (chưa DONE, chưa CANCELLED, đã quá dueDate)
    long countByDueDateBeforeAndStatusNotIn(Instant date, Collection<GvTaskStatus> statuses);
}

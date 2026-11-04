package vn.hoidanit.springrestwithai.gv.recurring;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvRecurringTemplateRepository extends JpaRepository<GvRecurringTemplate, Long> {

    List<GvRecurringTemplate> findByIsActiveTrue();

    List<GvRecurringTemplate> findByIsActiveTrueAndNextRunAtBefore(Instant date);

    List<GvRecurringTemplate> findByDepartmentId(Long departmentId);
}

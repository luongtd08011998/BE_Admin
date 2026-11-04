package vn.hoidanit.springrestwithai.gv.checklist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvTaskChecklistRepository extends JpaRepository<GvTaskChecklist, Long> {

    List<GvTaskChecklist> findByTaskIdOrderBySortOrderAsc(Long taskId);

    long countByTaskId(Long taskId);

    long countByTaskIdAndIsCompletedTrue(Long taskId);

    void deleteByTaskId(Long taskId);
}

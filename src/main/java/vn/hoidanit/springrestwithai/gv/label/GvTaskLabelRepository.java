package vn.hoidanit.springrestwithai.gv.label;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvTaskLabelRepository extends JpaRepository<GvTaskLabel, Long> {

    List<GvTaskLabel> findByTaskId(Long taskId);

    List<GvTaskLabel> findByLabelId(Long labelId);

    boolean existsByTaskIdAndLabelId(Long taskId, Long labelId);

    void deleteByTaskId(Long taskId);

    void deleteByTaskIdAndLabelId(Long taskId, Long labelId);
}

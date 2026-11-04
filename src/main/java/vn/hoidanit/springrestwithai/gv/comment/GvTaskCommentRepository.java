package vn.hoidanit.springrestwithai.gv.comment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvTaskCommentRepository extends JpaRepository<GvTaskComment, Long> {

    List<GvTaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    long countByTaskId(Long taskId);
}

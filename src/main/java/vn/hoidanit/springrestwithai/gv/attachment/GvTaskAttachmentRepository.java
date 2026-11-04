package vn.hoidanit.springrestwithai.gv.attachment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GvTaskAttachmentRepository extends JpaRepository<GvTaskAttachment, Long> {

    List<GvTaskAttachment> findByTaskId(Long taskId);

    List<GvTaskAttachment> findByCommentId(Long commentId);
}

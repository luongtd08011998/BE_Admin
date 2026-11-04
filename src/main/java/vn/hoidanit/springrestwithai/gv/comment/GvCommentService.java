package vn.hoidanit.springrestwithai.gv.comment;

import java.util.List;
import vn.hoidanit.springrestwithai.gv.comment.dto.CreateGvCommentRequest;
import vn.hoidanit.springrestwithai.gv.comment.dto.GvCommentResponse;

public interface GvCommentService {

    List<GvCommentResponse> getByTaskId(Long taskId);

    GvCommentResponse create(Long taskId, Long userId, CreateGvCommentRequest request);

    void delete(Long commentId, Long currentUserId);
}

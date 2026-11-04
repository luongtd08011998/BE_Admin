package vn.hoidanit.springrestwithai.gv.attachment;

import java.util.List;
import vn.hoidanit.springrestwithai.gv.attachment.dto.GvAttachmentResponse;

public interface GvAttachmentService {

    List<GvAttachmentResponse> getByTaskId(Long taskId);

    GvAttachmentResponse create(Long taskId, Long commentId, Long userId, String fileName, String fileUrl, Long fileSize, String fileType);

    void delete(Long attachmentId, Long currentUserId);
}

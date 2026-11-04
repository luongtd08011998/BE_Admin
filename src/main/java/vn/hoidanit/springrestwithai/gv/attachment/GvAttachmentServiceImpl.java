package vn.hoidanit.springrestwithai.gv.attachment;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.gv.attachment.dto.GvAttachmentResponse;
import vn.hoidanit.springrestwithai.gv.comment.GvTaskComment;
import vn.hoidanit.springrestwithai.gv.comment.GvTaskCommentRepository;
import vn.hoidanit.springrestwithai.gv.task.GvTask;
import vn.hoidanit.springrestwithai.gv.task.GvTaskRepository;

@Service
@Transactional
public class GvAttachmentServiceImpl implements GvAttachmentService {

    private final GvTaskAttachmentRepository attachmentRepository;
    private final GvTaskRepository taskRepository;
    private final GvTaskCommentRepository commentRepository;
    private final UserRepository userRepository;

    public GvAttachmentServiceImpl(
            GvTaskAttachmentRepository attachmentRepository,
            GvTaskRepository taskRepository,
            GvTaskCommentRepository commentRepository,
            UserRepository userRepository) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvAttachmentResponse> getByTaskId(Long taskId) {
        return attachmentRepository.findByTaskId(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GvAttachmentResponse create(Long taskId, Long commentId, Long userId, String fileName, String fileUrl, Long fileSize, String fileType) {
        GvTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId));

        GvTaskComment comment = null;
        if (commentId != null) {
            comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bình luận", "id", commentId));
        }

        GvTaskAttachment attachment = GvTaskAttachment.builder()
                .task(task)
                .comment(comment)
                .uploadedBy(user)
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .fileType(fileType)
                .build();

        return toResponse(attachmentRepository.save(attachment));
    }

    @Override
    public void delete(Long attachmentId, Long currentUserId) {
        GvTaskAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("File đính kèm", "id", attachmentId));

        boolean isSuperAdmin = vn.hoidanit.springrestwithai.gv.util.GvSecurityUtil.isSuperAdmin();
        if (!isSuperAdmin && !attachment.getUploadedBy().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Bạn chỉ có thể xóa file do chính mình upload");
        }

        attachmentRepository.delete(attachment);
    }

    private GvAttachmentResponse toResponse(GvTaskAttachment a) {
        return GvAttachmentResponse.builder()
                .id(a.getId())
                .taskId(a.getTask().getId())
                .commentId(a.getComment() != null ? a.getComment().getId() : null)
                .fileName(a.getFileName())
                .fileUrl(a.getFileUrl())
                .fileSize(a.getFileSize())
                .fileType(a.getFileType())
                .uploadedById(a.getUploadedBy().getId())
                .uploadedByName(a.getUploadedBy().getName())
                .uploadedAt(a.getUploadedAt())
                .build();
    }
}

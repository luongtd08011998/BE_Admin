package vn.hoidanit.springrestwithai.gv.comment;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.gv.comment.dto.CreateGvCommentRequest;
import vn.hoidanit.springrestwithai.gv.comment.dto.GvCommentResponse;
import vn.hoidanit.springrestwithai.gv.notification.GvNotificationService;
import vn.hoidanit.springrestwithai.gv.notification.GvNotificationType;
import vn.hoidanit.springrestwithai.gv.task.GvTask;
import vn.hoidanit.springrestwithai.gv.task.GvTaskRepository;
import vn.hoidanit.springrestwithai.gv.watcher.GvTaskWatcherRepository;

@Service
@Transactional
public class GvCommentServiceImpl implements GvCommentService {

    private final GvTaskCommentRepository commentRepository;
    private final GvTaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GvTaskWatcherRepository watcherRepository;
    private final GvNotificationService notificationService;

    public GvCommentServiceImpl(
            GvTaskCommentRepository commentRepository,
            GvTaskRepository taskRepository,
            UserRepository userRepository,
            GvTaskWatcherRepository watcherRepository,
            GvNotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.watcherRepository = watcherRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvCommentResponse> getByTaskId(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GvCommentResponse create(Long taskId, Long userId, CreateGvCommentRequest request) {
        GvTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId));

        GvTaskComment comment = GvTaskComment.builder()
                .task(task)
                .user(user)
                .content(request.content().trim())
                .build();

        GvTaskComment saved = commentRepository.save(comment);

        // Bắn thông báo cho người liên quan (creator, assignee, watchers) trừ chính người comment
        notifyStakeholders(task, user, request.content().trim());

        return toResponse(saved);
    }

    @Override
    public void delete(Long commentId, Long currentUserId) {
        GvTaskComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Bình luận", "id", commentId));

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Bạn chỉ có thể xóa bình luận do chính mình đăng");
        }

        commentRepository.delete(comment);
    }

    private void notifyStakeholders(GvTask task, User commenter, String contentSnippet) {
        String snippet = contentSnippet.length() > 50 ? contentSnippet.substring(0, 47) + "..." : contentSnippet;
        String notiTitle = commenter.getName() + " đã bình luận trên việc " + task.getCode();

        // Assignee
        if (!task.getAssignee().getId().equals(commenter.getId())) {
            notificationService.sendNotification(task.getAssignee().getId(), task.getId(),
                    GvNotificationType.TASK_COMMENTED, notiTitle, snippet);
        }
        // Creator
        if (!task.getCreator().getId().equals(commenter.getId()) && !task.getCreator().getId().equals(task.getAssignee().getId())) {
            notificationService.sendNotification(task.getCreator().getId(), task.getId(),
                    GvNotificationType.TASK_COMMENTED, notiTitle, snippet);
        }
        // Watchers
        watcherRepository.findByTaskId(task.getId()).forEach(w -> {
            if (!w.getUser().getId().equals(commenter.getId())
                    && !w.getUser().getId().equals(task.getAssignee().getId())
                    && !w.getUser().getId().equals(task.getCreator().getId())) {
                notificationService.sendNotification(w.getUser().getId(), task.getId(),
                        GvNotificationType.TASK_COMMENTED, notiTitle, snippet);
            }
        });
    }

    private GvCommentResponse toResponse(GvTaskComment c) {
        return GvCommentResponse.builder()
                .id(c.getId())
                .taskId(c.getTask().getId())
                .userId(c.getUser().getId())
                .userName(c.getUser().getName())
                .userEmail(c.getUser().getEmail())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}

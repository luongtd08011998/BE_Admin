package vn.hoidanit.springrestwithai.gv.checklist;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.exception.BadRequestException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.gv.checklist.dto.CreateGvChecklistRequest;
import vn.hoidanit.springrestwithai.gv.checklist.dto.GvChecklistResponse;
import vn.hoidanit.springrestwithai.gv.checklist.dto.UpdateGvChecklistRequest;
import vn.hoidanit.springrestwithai.gv.task.GvTask;
import vn.hoidanit.springrestwithai.gv.task.GvTaskRepository;
import vn.hoidanit.springrestwithai.gv.task.GvTaskStatus;

@Service
@Transactional
public class GvChecklistServiceImpl implements GvChecklistService {

    private final GvTaskChecklistRepository checklistRepository;
    private final GvTaskRepository taskRepository;
    private final UserRepository userRepository;

    public GvChecklistServiceImpl(
            GvTaskChecklistRepository checklistRepository,
            GvTaskRepository taskRepository,
            UserRepository userRepository) {
        this.checklistRepository = checklistRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GvChecklistResponse> getByTaskId(Long taskId) {
        return checklistRepository.findByTaskIdOrderBySortOrderAsc(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GvChecklistResponse create(Long taskId, CreateGvChecklistRequest request) {
        GvTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Công việc", "id", taskId));

        validateTaskCanModifyChecklist(task);

        boolean isRequired = request.isRequired() != null ? request.isRequired() : true;

        GvTaskChecklist item = GvTaskChecklist.builder()
                .task(task)
                .title(request.title().trim())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .isCompleted(false)
                .isRequired(isRequired)
                .build();

        GvTaskChecklist saved = checklistRepository.save(item);
        updateTaskProgress(taskId);
        return toResponse(saved);
    }

    @Override
    public GvChecklistResponse update(Long checklistId, UpdateGvChecklistRequest request) {
        GvTaskChecklist item = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist", "id", checklistId));

        validateTaskCanModifyChecklist(item.getTask());

        item.setTitle(request.title().trim());
        if (request.sortOrder() != null) {
            item.setSortOrder(request.sortOrder());
        }
        if (request.isRequired() != null) {
            item.setIsRequired(request.isRequired());
        }

        return toResponse(checklistRepository.save(item));
    }

    @Override
    public GvChecklistResponse toggle(Long checklistId, Long userId) {
        GvTaskChecklist item = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist", "id", checklistId));

        validateTaskCanModifyChecklist(item.getTask());

        boolean nextState = !Boolean.TRUE.equals(item.getIsCompleted());
        item.setIsCompleted(nextState);

        if (nextState) {
            item.setCompletedAt(Instant.now());
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                item.setCompletedBy(user);
            }
        } else {
            item.setCompletedAt(null);
            item.setCompletedBy(null);
        }

        GvTaskChecklist saved = checklistRepository.save(item);
        updateTaskProgress(item.getTask().getId());
        return toResponse(saved);
    }

    @Override
    public void delete(Long checklistId) {
        GvTaskChecklist item = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist", "id", checklistId));

        validateTaskCanModifyChecklist(item.getTask());

        Long taskId = item.getTask().getId();
        checklistRepository.delete(item);
        updateTaskProgress(taskId);
    }

    private void validateTaskCanModifyChecklist(GvTask task) {
        if (task.getStatus() == GvTaskStatus.DONE) {
            throw new BadRequestException("Không thể chỉnh sửa danh sách checklist vì công việc đã được nghiệm thu hoàn thành");
        }
        if (task.getStatus() == GvTaskStatus.CANCELLED) {
            throw new BadRequestException("Không thể chỉnh sửa danh sách checklist vì công việc đã bị hủy");
        }
        if (task.getStatus() == GvTaskStatus.PENDING_REVIEW) {
            throw new BadRequestException("Không thể chỉnh sửa danh sách checklist khi công việc đang chờ quản lý nghiệm thu");
        }
    }

    private void updateTaskProgress(Long taskId) {
        taskRepository.findById(taskId).ifPresent(task -> {
            long total = checklistRepository.countByTaskId(taskId);
            if (total == 0) {
                return;
            }
            long completed = checklistRepository.countByTaskIdAndIsCompletedTrue(taskId);
            int progress = (int) Math.round((double) completed / total * 100);
            task.setProgress(progress);
            taskRepository.save(task);
        });
    }

    private GvChecklistResponse toResponse(GvTaskChecklist item) {
        return GvChecklistResponse.builder()
                .id(item.getId())
                .taskId(item.getTask().getId())
                .title(item.getTitle())
                .isCompleted(item.getIsCompleted())
                .completedById(item.getCompletedBy() != null ? item.getCompletedBy().getId() : null)
                .completedByName(item.getCompletedBy() != null ? item.getCompletedBy().getName() : null)
                .completedAt(item.getCompletedAt())
                .sortOrder(item.getSortOrder())
                .isRequired(item.getIsRequired() != null ? item.getIsRequired() : true)
                .createdAt(item.getCreatedAt())
                .build();
    }
}

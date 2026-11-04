package vn.hoidanit.springrestwithai.gv.task.dto;

import java.util.List;
import lombok.Builder;
import vn.hoidanit.springrestwithai.gv.attachment.dto.GvAttachmentResponse;
import vn.hoidanit.springrestwithai.gv.checklist.dto.GvChecklistResponse;
import vn.hoidanit.springrestwithai.gv.comment.dto.GvCommentResponse;
import vn.hoidanit.springrestwithai.gv.watcher.dto.GvWatcherResponse;

@Builder
public record GvTaskDetailResponse(
    GvTaskResponse task,
    List<GvTaskResponse> subTasks,
    List<GvChecklistResponse> checklists,
    List<GvCommentResponse> comments,
    List<GvAttachmentResponse> attachments,
    List<GvWatcherResponse> watchers
) {}

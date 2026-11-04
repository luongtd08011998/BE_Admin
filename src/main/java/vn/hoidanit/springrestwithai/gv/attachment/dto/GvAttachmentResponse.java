package vn.hoidanit.springrestwithai.gv.attachment.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record GvAttachmentResponse(
    Long id,
    Long taskId,
    Long commentId,
    String fileName,
    String fileUrl,
    Long fileSize,
    String fileType,
    Long uploadedById,
    String uploadedByName,
    Instant uploadedAt
) {}

package vn.hoidanit.springrestwithai.gv.attachment.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGvAttachmentRequest(
    Long commentId,

    @NotBlank(message = "Tên file không được để trống")
    String fileName,

    @NotBlank(message = "Đường dẫn file không được để trống")
    String fileUrl,

    Long fileSize,
    String fileType
) {}

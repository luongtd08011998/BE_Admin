package vn.hoidanit.springrestwithai.gv.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGvCommentRequest(
    @NotBlank(message = "Nội dung bình luận không được để trống")
    String content
) {}

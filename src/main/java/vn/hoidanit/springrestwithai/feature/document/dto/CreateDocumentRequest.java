package vn.hoidanit.springrestwithai.feature.document.dto;

import jakarta.validation.constraints.NotBlank;
import vn.hoidanit.springrestwithai.util.constant.DocumentStatus;

public record CreateDocumentRequest(
        @NotBlank(message = "Tiêu đề không được để trống") String title,
        @NotBlank(message = "Slug không được để trống") String slug,
        String content,
        String summary,
        String thumbnail,
        DocumentStatus status,
        Long categoryId
) {
}

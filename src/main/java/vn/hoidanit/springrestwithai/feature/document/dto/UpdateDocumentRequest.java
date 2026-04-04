package vn.hoidanit.springrestwithai.feature.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateDocumentRequest(
        @NotNull(message = "ID không được để trống") Long id,
        String title,
        String description,
        @NotBlank(message = "URL tài liệu không được để trống") String documentUrl,
        @NotNull(message = "Bài viết không được để trống") Long articleId
) {
}

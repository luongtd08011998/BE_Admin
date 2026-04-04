package vn.hoidanit.springrestwithai.feature.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateArticleRequest(
        @NotNull(message = "ID không được để trống") Long id,
        @NotBlank(message = "Tiêu đề không được để trống") String title,
        @NotBlank(message = "Slug không được để trống") String slug,
        String content,
        String thumbnail,
        @NotNull(message = "Loại bài viết không được để trống") Byte type,
        Byte active,
        @NotNull(message = "Tác giả không được để trống") Long authorId,
        Long categoryId,
        @NotNull(message = "Danh sách tag không được null") List<Long> tagIds
) {
}

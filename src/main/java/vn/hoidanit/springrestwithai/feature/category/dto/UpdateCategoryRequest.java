package vn.hoidanit.springrestwithai.feature.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCategoryRequest(
        @NotNull(message = "ID không được để trống") Long id,
        @NotBlank(message = "Tên danh mục không được để trống") String name,
        @NotBlank(message = "Slug không được để trống") String slug,
        Byte active,
        Long parentId
) {
}

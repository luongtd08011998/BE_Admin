package vn.hoidanit.springrestwithai.feature.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống") String name,
        @NotBlank(message = "Slug không được để trống") String slug,
        Byte active,
        Long parentId
) {
}

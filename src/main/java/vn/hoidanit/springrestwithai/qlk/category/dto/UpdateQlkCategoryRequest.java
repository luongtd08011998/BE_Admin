package vn.hoidanit.springrestwithai.qlk.category.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateQlkCategoryRequest(
    @NotBlank(message = "Tên danh mục không được để trống")
    String name,
    
    String description
) {}

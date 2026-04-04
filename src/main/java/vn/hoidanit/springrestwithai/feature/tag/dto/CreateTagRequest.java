package vn.hoidanit.springrestwithai.feature.tag.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTagRequest(
        @NotBlank(message = "Tên tag không được để trống") String name,
        String description
) {
}

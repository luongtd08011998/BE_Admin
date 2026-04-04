package vn.hoidanit.springrestwithai.feature.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTagRequest(
        @NotNull(message = "ID không được để trống") Long id,
        @NotBlank(message = "Tên tag không được để trống") String name,
        String description
) {
}

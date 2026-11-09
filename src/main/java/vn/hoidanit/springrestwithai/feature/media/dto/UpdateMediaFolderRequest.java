package vn.hoidanit.springrestwithai.feature.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMediaFolderRequest(
        @NotBlank(message = "Tên thư mục không được để trống")
        @Size(max = 150, message = "Tên thư mục không vượt quá 150 ký tự")
        String name,
        Long parentId) {
}


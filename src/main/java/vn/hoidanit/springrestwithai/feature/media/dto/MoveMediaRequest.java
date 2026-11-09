package vn.hoidanit.springrestwithai.feature.media.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record MoveMediaRequest(
        @NotEmpty(message = "Danh sách media không được để trống")
        List<Long> mediaIds,
        Long folderId) {
}


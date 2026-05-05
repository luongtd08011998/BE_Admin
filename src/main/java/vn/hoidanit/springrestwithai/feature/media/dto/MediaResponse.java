package vn.hoidanit.springrestwithai.feature.media.dto;

import java.time.Instant;

import vn.hoidanit.springrestwithai.feature.media.Media;

public record MediaResponse(
        Long id,
        String title,
        String fileName,
        String fileUrl,
        String fileType,
        Long fileSize,
        Long uploadedBy,
        Instant createdAt) {

    public static MediaResponse fromEntity(Media media) {
        return new MediaResponse(
                media.getId(),
                media.getTitle(),
                media.getFileName(),
                media.getFileUrl(),
                media.getFileType(),
                media.getFileSize(),
                media.getUploadedBy(),
                media.getCreatedAt());
    }
}

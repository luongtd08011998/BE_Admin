package vn.hoidanit.springrestwithai.feature.media.dto;

import java.time.Instant;

import vn.hoidanit.springrestwithai.feature.media.MediaFolder;

public record MediaFolderResponse(
        Long id,
        String name,
        Long parentId,
        long itemCount,
        long subfolderCount,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt) {

    public static MediaFolderResponse fromEntity(MediaFolder folder, long itemCount, long subfolderCount) {
        return new MediaFolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getParentId(),
                itemCount,
                subfolderCount,
                folder.getCreatedBy(),
                folder.getCreatedAt(),
                folder.getUpdatedAt());
    }
}


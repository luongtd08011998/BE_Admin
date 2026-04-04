package vn.hoidanit.springrestwithai.feature.document.dto;

import vn.hoidanit.springrestwithai.feature.document.Document;
import vn.hoidanit.springrestwithai.util.constant.DocumentStatus;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String title,
        String slug,
        String summary,
        String thumbnail,
        DocumentStatus status,
        CategoryInfo category,
        Instant createdAt,
        Instant updatedAt
) {
    public record CategoryInfo(Long id, String name) {
    }

    public static DocumentResponse fromEntity(Document document) {
        CategoryInfo categoryInfo = document.getCategory() != null
                ? new CategoryInfo(document.getCategory().getId(), document.getCategory().getName())
                : null;
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getSlug(),
                document.getSummary(),
                document.getThumbnail(),
                document.getStatus(),
                categoryInfo,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}

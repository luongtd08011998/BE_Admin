package vn.hoidanit.springrestwithai.feature.document.dto;

import vn.hoidanit.springrestwithai.feature.document.Document;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String title,
        String description,
        String documentUrl,
        ArticleInfo article,
        Instant createdAt,
        Instant updatedAt
) {
    public record ArticleInfo(Long id, String title) {
    }

    public static DocumentResponse fromEntity(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getDocumentUrl(),
                new ArticleInfo(
                        document.getArticle().getId(),
                        document.getArticle().getTitle()),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}

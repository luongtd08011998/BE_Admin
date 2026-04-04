package vn.hoidanit.springrestwithai.feature.category.dto;

import vn.hoidanit.springrestwithai.feature.category.Category;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Byte active,
        ParentInfo parent,
        Instant createdAt,
        Instant updatedAt
) {
    public record ParentInfo(Long id, String name) {
    }

    public static CategoryResponse fromEntity(Category category) {
        ParentInfo parentInfo = category.getParent() != null
                ? new ParentInfo(category.getParent().getId(), category.getParent().getName())
                : null;
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getActive(),
                parentInfo,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}

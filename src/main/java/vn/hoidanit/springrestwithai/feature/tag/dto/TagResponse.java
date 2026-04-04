package vn.hoidanit.springrestwithai.feature.tag.dto;

import vn.hoidanit.springrestwithai.feature.tag.Tag;

import java.time.Instant;

public record TagResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getDescription(),
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }
}

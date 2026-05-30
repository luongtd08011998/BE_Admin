package vn.hoidanit.springrestwithai.qlk.category.dto;

import java.time.Instant;
import vn.hoidanit.springrestwithai.qlk.category.QlkCategory;

public record QlkCategoryResponse(
    Long id,
    String name,
    String description,
    Long warehouseId,
    String warehouseName,
    Instant createdAt,
    Instant updatedAt
) {
    public static QlkCategoryResponse from(QlkCategory category) {
        if (category == null) return null;
        return new QlkCategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getWarehouse() != null ? category.getWarehouse().getId() : null,
            category.getWarehouse() != null ? category.getWarehouse().getName() : null,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}

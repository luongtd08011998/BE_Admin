package vn.hoidanit.springrestwithai.qlk.material.dto;

import java.math.BigDecimal;
import java.time.Instant;
import vn.hoidanit.springrestwithai.qlk.material.Material;
import vn.hoidanit.springrestwithai.qlk.material.MaterialStatus;
import vn.hoidanit.springrestwithai.qlk.category.dto.QlkCategoryResponse;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;

public record MaterialResponse(
    Long id,
    String code,
    String name,
    String specification,
    String unit,
    BigDecimal unitPrice,
    Integer minStock,
    String barcode,
    String imageUrl,
    QlkCategoryResponse category,
    MaterialStatus status,
    UserResponse createdBy,
    Instant createdAt,
    Instant updatedAt
) {
    public static MaterialResponse from(Material material) {
        if (material == null) return null;
        return new MaterialResponse(
            material.getId(),
            material.getCode(),
            material.getName(),
            material.getSpecification(),
            material.getUnit(),
            material.getUnitPrice(),
            material.getMinStock(),
            material.getBarcode(),
            material.getImageUrl(),
            material.getCategory() != null ? QlkCategoryResponse.from(material.getCategory()) : null,
            material.getStatus(),
            material.getCreatedBy() != null ? UserResponse.fromEntity(material.getCreatedBy()) : null,
            material.getCreatedAt(),
            material.getUpdatedAt()
        );
    }
}

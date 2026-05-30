package vn.hoidanit.springrestwithai.qlk.material.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import vn.hoidanit.springrestwithai.qlk.material.MaterialStatus;

public record UpdateMaterialRequest(
    @NotBlank(message = "Tên vật tư không được để trống")
    String name,
    
    String specification,
    String unit,
    BigDecimal unitPrice,
    Integer minStock,
    String barcode,
    String imageUrl,
    Long categoryId,
    MaterialStatus status
) {}

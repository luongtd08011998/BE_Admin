package vn.hoidanit.springrestwithai.qlk.material.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateMaterialRequest(
    @NotBlank(message = "Mã vật tư không được để trống")
    String code,
    
    @NotBlank(message = "Tên vật tư không được để trống")
    String name,
    
    String specification,
    String unit,
    BigDecimal unitPrice,
    Integer minStock,
    String barcode,
    String imageUrl,
    Long categoryId
) {}

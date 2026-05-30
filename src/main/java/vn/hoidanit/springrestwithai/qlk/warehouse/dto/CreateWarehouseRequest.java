package vn.hoidanit.springrestwithai.qlk.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import vn.hoidanit.springrestwithai.qlk.warehouse.WarehouseStatus;

public record CreateWarehouseRequest(
    @NotBlank(message = "Mã kho không được để trống")
    String code,
    
    @NotBlank(message = "Tên kho không được để trống")
    String name,
    
    String address,
    
    String description,
    
    WarehouseStatus status
) {}

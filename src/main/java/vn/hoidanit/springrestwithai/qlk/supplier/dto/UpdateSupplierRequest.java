package vn.hoidanit.springrestwithai.qlk.supplier.dto;

import jakarta.validation.constraints.NotBlank;
import vn.hoidanit.springrestwithai.qlk.supplier.SupplierStatus;

public record UpdateSupplierRequest(
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    String name,
    
    String phone,
    String address,
    String email,
    SupplierStatus status
) {}

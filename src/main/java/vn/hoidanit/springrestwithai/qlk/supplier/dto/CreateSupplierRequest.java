package vn.hoidanit.springrestwithai.qlk.supplier.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSupplierRequest(
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    String name,
    
    String phone,
    String address,
    String email
) {}

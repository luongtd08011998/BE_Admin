package vn.hoidanit.springrestwithai.qlk.supplier.dto;

import java.time.Instant;
import vn.hoidanit.springrestwithai.qlk.supplier.Supplier;
import vn.hoidanit.springrestwithai.qlk.supplier.SupplierStatus;

public record SupplierResponse(
    Long id,
    String name,
    String phone,
    String address,
    String email,
    SupplierStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static SupplierResponse from(Supplier supplier) {
        if (supplier == null) return null;
        return new SupplierResponse(
            supplier.getId(),
            supplier.getName(),
            supplier.getPhone(),
            supplier.getAddress(),
            supplier.getEmail(),
            supplier.getStatus(),
            supplier.getCreatedAt(),
            supplier.getUpdatedAt()
        );
    }
}

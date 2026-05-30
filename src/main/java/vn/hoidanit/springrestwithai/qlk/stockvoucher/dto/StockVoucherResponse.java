package vn.hoidanit.springrestwithai.qlk.stockvoucher.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.StockVoucher;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.VoucherType;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.VoucherStatus;
import vn.hoidanit.springrestwithai.qlk.supplier.dto.SupplierResponse;
import vn.hoidanit.springrestwithai.qlk.warehouse.dto.WarehouseResponse;
import vn.hoidanit.springrestwithai.feature.user.dto.UserResponse;

public record StockVoucherResponse(
    Long id,
    String voucherCode,
    VoucherType type,
    VoucherStatus status,
    LocalDate issuedDate,
    String note,
    SupplierResponse supplier,
    WarehouseResponse warehouse,
    UserResponse createdBy,
    UserResponse approvedBy,
    Instant approvedAt,
    String rejectReason,
    Instant createdAt,
    Instant updatedAt,
    List<VoucherDetailResponse> details
) {
    public static StockVoucherResponse from(StockVoucher voucher) {
        if (voucher == null) return null;
        return new StockVoucherResponse(
            voucher.getId(),
            voucher.getVoucherCode(),
            voucher.getType(),
            voucher.getStatus(),
            voucher.getIssuedDate(),
            voucher.getNote(),
            voucher.getSupplier() != null ? SupplierResponse.from(voucher.getSupplier()) : null,
            voucher.getWarehouse() != null ? WarehouseResponse.from(voucher.getWarehouse()) : null,
            voucher.getCreatedBy() != null ? UserResponse.fromEntity(voucher.getCreatedBy()) : null,
            voucher.getApprovedBy() != null ? UserResponse.fromEntity(voucher.getApprovedBy()) : null,
            voucher.getApprovedAt(),
            voucher.getRejectReason(),
            voucher.getCreatedAt(),
            voucher.getUpdatedAt(),
            voucher.getDetails() != null ? 
                voucher.getDetails().stream().map(VoucherDetailResponse::from).collect(Collectors.toList()) : 
                List.of()
        );
    }
}

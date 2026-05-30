package vn.hoidanit.springrestwithai.qlk.stockvoucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import vn.hoidanit.springrestwithai.qlk.stockvoucher.VoucherType;

public record CreateStockVoucherRequest(
    @NotBlank(message = "Mã phiếu không được để trống")
    String voucherCode,
    
    @NotNull(message = "Loại phiếu không được để trống")
    VoucherType type,
    
    LocalDate issuedDate,
    String note,
    Long supplierId,
    List<CreateVoucherDetailRequest> details
) {}

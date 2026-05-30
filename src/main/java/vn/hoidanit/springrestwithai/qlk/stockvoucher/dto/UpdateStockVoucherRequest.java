package vn.hoidanit.springrestwithai.qlk.stockvoucher.dto;

import java.time.LocalDate;
import java.util.List;

public record UpdateStockVoucherRequest(
    LocalDate issuedDate,
    String note,
    Long supplierId,
    List<CreateVoucherDetailRequest> details
) {}

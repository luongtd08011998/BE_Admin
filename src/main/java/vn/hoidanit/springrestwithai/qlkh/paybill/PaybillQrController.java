package vn.hoidanit.springrestwithai.qlkh.paybill;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.springrestwithai.qlkh.paybill.dto.CreateQrRequest;
import vn.hoidanit.springrestwithai.qlkh.paybill.dto.PaybillQrResult;
import vn.hoidanit.springrestwithai.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/qlkh/paybill")
public class PaybillQrController {

    private final PaybillQrService paybillQrService;

    public PaybillQrController(PaybillQrService paybillQrService) {
        this.paybillQrService = paybillQrService;
    }

    @PostMapping("/qr")
    public ResponseEntity<ApiResponse<PaybillQrResult>> createQrCode(@RequestBody CreateQrRequest payload) {
        if (payload.getCustomerId() == null || payload.getMonthInvoiceIds() == null || payload.getMonthInvoiceIds().isEmpty()) {
            throw new IllegalArgumentException("Thiếu tham số customerId hoặc monthInvoiceIds");
        }
        PaybillQrResult result = paybillQrService.createQrCode(payload.getCustomerId(), payload.getMonthInvoiceIds());
        return ResponseEntity.ok(ApiResponse.success("Tạo mã QR thành công", result));
    }
}

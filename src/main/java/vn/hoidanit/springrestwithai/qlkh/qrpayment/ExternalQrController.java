package vn.hoidanit.springrestwithai.qlkh.qrpayment;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.qlkh.qrpayment.dto.QrGenerateRequest;
import vn.hoidanit.springrestwithai.qlkh.qrpayment.dto.QrPaymentExternalResponse;

@RestController
@RequestMapping("/api/v1/external/qr-payment")
public class ExternalQrController {

    private final VietQrService vietQrService;

    public ExternalQrController(VietQrService vietQrService) {
        this.vietQrService = vietQrService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<QrPaymentExternalResponse>> generate(
            @Valid @RequestBody QrGenerateRequest request) {
        
        double amount = request.amount() != null ? request.amount() : 0d;
        double envFee = request.envFee() != null ? request.envFee() : 0d;
        double taxFee = request.taxFee() != null ? request.taxFee() : 0d;
        double total = amount + envFee + taxFee;

        String qrUrl = vietQrService.buildQrUrl(
            request.customerCode(), request.yearMonth(), total);

        if (qrUrl == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.badRequest("Tổng tiền phải > 0"));
        }

        String addInfo = vietQrService.generateAddInfo(request.customerCode(), request.yearMonth());

        QrPaymentExternalResponse resp = new QrPaymentExternalResponse(
            request.fkey(), request.customerCode(), request.yearMonth(),
            total, qrUrl, addInfo);

        return ResponseEntity.ok(
            ApiResponse.success("Tạo QR thanh toán thành công", resp));
    }
}

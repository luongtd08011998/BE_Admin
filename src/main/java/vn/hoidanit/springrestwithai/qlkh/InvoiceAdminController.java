package vn.hoidanit.springrestwithai.qlkh;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.dto.AdminInvoiceFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.dto.DebtReminderResponse;

@RestController
@RequestMapping("/api/v1/admin/invoices")
public class InvoiceAdminController {

    private final InvoiceAdminService invoiceAdminService;

    public InvoiceAdminController(InvoiceAdminService invoiceAdminService) {
        this.invoiceAdminService = invoiceAdminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ResultPaginationDTO>> getAll(
            @ParameterObject AdminInvoiceFilterRequest filter,
            @ParameterObject Pageable pageable) {
        ResultPaginationDTO result = invoiceAdminService.getAll(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hóa đơn thành công", result));
    }

    @PostMapping("/send-debt-reminder")
    public ResponseEntity<ApiResponse<DebtReminderResponse>> sendDebtReminder(@RequestBody java.util.Map<String, String> request) {
        String yearMonth = request.get("yearMonth");
        if (yearMonth == null || yearMonth.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("Vui lòng cung cấp yearMonth"));
        }
        
        Integer monthInvoiceId = null;
        if (request.containsKey("monthInvoiceId") && request.get("monthInvoiceId") != null && !request.get("monthInvoiceId").isBlank()) {
            try {
                monthInvoiceId = Integer.parseInt(request.get("monthInvoiceId"));
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(ApiResponse.badRequest("monthInvoiceId không hợp lệ"));
            }
        }
        
        DebtReminderResponse response = invoiceAdminService.sendDebtReminder(yearMonth, monthInvoiceId);
        return ResponseEntity.ok(ApiResponse.success("Gửi thông báo nhắc nợ thành công", response));
    }
}

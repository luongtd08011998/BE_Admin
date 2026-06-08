package vn.hoidanit.springrestwithai.qlkh.invoiceadmin;

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
import vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.AdminInvoiceFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.DebtReminderResponse;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/send-overdue-reminder")
    public ResponseEntity<ApiResponse<DebtReminderResponse>> sendOverdueReminder(@RequestBody java.util.Map<String, String> request) {
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

        DebtReminderResponse response = invoiceAdminService.sendOverdueReminder(yearMonth, monthInvoiceId);
        return ResponseEntity.ok(ApiResponse.success("Gửi thông báo quá hạn thành công", response));
    }

    @PostMapping("/send-water-cutoff")
    public ResponseEntity<ApiResponse<Boolean>> sendWaterCutoff(@RequestBody java.util.Map<String, String> request) {
        String monthInvoiceIdStr = request.get("monthInvoiceId");
        if (monthInvoiceIdStr == null || monthInvoiceIdStr.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("Vui lòng cung cấp monthInvoiceId"));
        }

        Integer monthInvoiceId;
        try {
            monthInvoiceId = Integer.parseInt(monthInvoiceIdStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("monthInvoiceId không hợp lệ"));
        }

        String employeeName = request.getOrDefault("employeeName", null);
        String employeePhone = request.getOrDefault("employeePhone", null);

        boolean sent = invoiceAdminService.sendWaterCutoff(monthInvoiceId, employeeName, employeePhone);
        return ResponseEntity.ok(ApiResponse.success("Gửi thông báo cúp nước thành công", sent));
    }

    @PostMapping("/send-invoice-notification")
    public ResponseEntity<ApiResponse<DebtReminderResponse>> sendInvoiceNotification(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> monthInvoiceIds = request.get("monthInvoiceIds");
        if (monthInvoiceIds == null || monthInvoiceIds.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("Vui lòng cung cấp monthInvoiceIds"));
        }
        DebtReminderResponse response = invoiceAdminService.sendInvoiceNotification(monthInvoiceIds);
        return ResponseEntity.ok(ApiResponse.success("Gửi thông báo hóa đơn thành công", response));
    }

    @PostMapping("/send-payment-notification")
    public ResponseEntity<ApiResponse<DebtReminderResponse>> sendPaymentNotification(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> monthInvoiceIds = request.get("monthInvoiceIds");
        if (monthInvoiceIds == null || monthInvoiceIds.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("Vui lòng cung cấp monthInvoiceIds"));
        }
        DebtReminderResponse response = invoiceAdminService.sendPaymentNotification(monthInvoiceIds);
        return ResponseEntity.ok(ApiResponse.success("Gửi thông báo xác nhận thanh toán thành công", response));
    }
}

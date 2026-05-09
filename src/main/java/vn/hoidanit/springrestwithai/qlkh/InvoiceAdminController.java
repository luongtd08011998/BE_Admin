package vn.hoidanit.springrestwithai.qlkh;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.dto.AdminInvoiceFilterRequest;

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
}

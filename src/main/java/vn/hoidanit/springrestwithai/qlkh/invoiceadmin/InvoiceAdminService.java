package vn.hoidanit.springrestwithai.qlkh.invoiceadmin;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.AdminInvoiceFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.DebtReminderResponse;
public interface InvoiceAdminService {
    ResultPaginationDTO getAll(AdminInvoiceFilterRequest filter, Pageable pageable);
    DebtReminderResponse sendDebtReminder(String yearMonth, Integer monthInvoiceId);
    DebtReminderResponse sendOverdueReminder(String yearMonth, Integer monthInvoiceId);
    boolean sendWaterCutoff(Integer monthInvoiceId, String employeeName, String employeePhone);
}

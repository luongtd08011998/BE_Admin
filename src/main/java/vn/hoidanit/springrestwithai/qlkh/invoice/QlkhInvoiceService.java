package vn.hoidanit.springrestwithai.qlkh.invoice;

import org.springframework.data.domain.Pageable;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.invoice.dto.*;
import vn.hoidanit.springrestwithai.qlkh.vnpt.VnptPortalInvoiceClient.VnptDebugResult;

import java.util.List;

public interface QlkhInvoiceService {

    VnptDebugResult vnptHealth(String fkey);

    List<MonthInvoiceReadingItemResponse> getMonthInvoiceReadings(String yearMonth, String fromYearMonth, String toYearMonth);

    List<ConsumptionHistoryItemResponse> getConsumptionHistory(String customerCode, String fromYearMonth, String toYearMonth);

    ResultPaginationDTO getInvoices(Customer customer, String yearMonth, Pageable pageable);

    InvoiceResponse getInvoiceByFkey(Customer customer, String fkey);

    InvoiceResponse getInvoiceByRootKey(Customer customer, String rootKey);

    EInvoiceDownloadDto downloadMonthEInvoiceXml(Customer customer, Integer invoiceId);

    InvoiceViewResponse viewMonthEInvoice(Customer customer, Integer invoiceId);

    byte[] listEInvoices(Customer customer, String fromDate, String toDate);

    InvoiceResponse getInvoice(Customer customer, Integer invoiceId);

    ResultPaginationDTO getSalesInvoices(Customer customer, String templateCode, Pageable pageable);

    SalesInvoiceResponse getSalesInvoice(Customer customer, Integer salesInvoiceId);
    
    record EInvoiceDownloadDto(byte[] content, String filename, boolean isZip) {}
}

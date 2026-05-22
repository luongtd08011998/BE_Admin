package vn.hoidanit.springrestwithai.qlkh.invoiceadmin;
import vn.hoidanit.springrestwithai.qlkh.notification.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.AdminInvoiceFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.AdminInvoiceResponse;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.hoidanit.springrestwithai.qlkh.invoiceadmin.dto.DebtReminderResponse;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoice;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.SalesInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;

@Service
public class InvoiceAdminServiceImpl implements InvoiceAdminService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceAdminService.class);

    private final MonthInvoiceRepository monthInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final vn.hoidanit.springrestwithai.qlkh.qrpayment.VietQrService vietQrService;

    // Thread pool dùng cho gửi notification song song
    private final ExecutorService notificationExecutor = Executors.newFixedThreadPool(10);

    public InvoiceAdminServiceImpl(MonthInvoiceRepository monthInvoiceRepository,
                               CustomerRepository customerRepository,
                               NotificationService notificationService,
                               vn.hoidanit.springrestwithai.qlkh.qrpayment.VietQrService vietQrService) {
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
        this.vietQrService = vietQrService;
    }

    public ResultPaginationDTO getAll(AdminInvoiceFilterRequest filter, Pageable pageable) {
        String yearMonth = (filter != null && filter.getYearMonth() != null) ? filter.getYearMonth().trim() : null;

        if (yearMonth == null || yearMonth.isBlank()) {
            ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                    pageable.getPageNumber() + 1,
                    pageable.getPageSize(),
                    0,
                    0);
            return new ResultPaginationDTO(meta, Collections.emptyList());
        }

        Integer paymentStatus = (filter != null) ? filter.getPaymentStatus() : null;
        String customerName = (filter != null) ? filter.getCustomerName() : null;
        String digiCode = (filter != null) ? filter.getDigiCode() : null;
        Integer remindStatus = (filter != null) ? filter.getRemindStatus() : null;
        Integer roadId = (filter != null) ? filter.getRoadId() : null;

        // Fetch all reminded invoice IDs from Notification table
        java.util.List<Long> remindedLongIds = notificationService.findAllRemindedInvoiceIds();
        java.util.List<Integer> remindedIds = remindedLongIds.stream().map(Long::intValue).toList();

        // If list is empty, avoid SQL syntax errors in IN clause
        if (remindedIds.isEmpty()) {
            remindedIds = java.util.List.of(-1);
        }

        // Fetch overdue and cutwater invoice IDs
        java.util.List<Long> overdueLongIds = notificationService.findAllOverdueInvoiceIds();
        java.util.List<Integer> overdueIds = overdueLongIds.stream().map(Long::intValue).toList();
        if (overdueIds.isEmpty()) overdueIds = java.util.List.of(-1);

        java.util.List<Long> cutwaterLongIds = notificationService.findAllCutwaterInvoiceIds();
        java.util.List<Integer> cutwaterIds = cutwaterLongIds.stream().map(Long::intValue).toList();
        if (cutwaterIds.isEmpty()) cutwaterIds = java.util.List.of(-1);

        Page<AdminInvoiceResponse> page = monthInvoiceRepository.findAdminInvoices(yearMonth, paymentStatus, customerName, digiCode, remindStatus, roadId, remindedIds, overdueIds, cutwaterIds, pageable);

        // Gọi VNPT song song cho tất cả hóa đơn trong trang hiện tại
        List<AdminInvoiceResponse> content = page.getContent();
        
        // Map isReminded, isOverdue, isWaterCutoff boolean
        java.util.Set<Integer> remindedSet = new java.util.HashSet<>(remindedIds);
        java.util.Set<Integer> overdueSet = new java.util.HashSet<>(overdueIds);
        java.util.Set<Integer> cutwaterSet = new java.util.HashSet<>(cutwaterIds);
        for (AdminInvoiceResponse res : content) {
            res.setIsReminded(remindedSet.contains(res.getId()));
            res.setIsOverdue(overdueSet.contains(res.getId()));
            res.setIsWaterCutoff(cutwaterSet.contains(res.getId()));
            
            if (res.getPaymentStatus() != null && res.getPaymentStatus() != 2
                    && res.getTotalAmount() != null && res.getTotalAmount() > 0) {
                res.setQrUrl(vietQrService.buildQrUrl(
                    res.getDigiCode(), res.getYearMonth(), res.getTotalAmount()));
            }
        }

        return ResultPaginationDTO.fromPage(page);
    }


    public DebtReminderResponse sendDebtReminder(String yearMonth, Integer monthInvoiceId) {
        if (yearMonth == null || yearMonth.isBlank()) {
            throw new IllegalArgumentException("yearMonth is required");
        }

        List<vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO> unpaidInvoices = monthInvoiceRepository.findUnpaidInvoiceDTOsByYearMonth(yearMonth.trim());
        
        if (monthInvoiceId != null) {
            unpaidInvoices = unpaidInvoices.stream()
                .filter(inv -> inv.getMonthInvoiceId().equals(monthInvoiceId))
                .toList();
        }

        if (unpaidInvoices == null || unpaidInvoices.isEmpty()) {
            return new DebtReminderResponse(0, 0);
        }

        // Lọc bỏ những hóa đơn có tổng tiền bằng 0 hoặc đã có hóa đơn thay thế
        unpaidInvoices = unpaidInvoices.stream()
                .filter(inv -> inv.getAmount() > 0 && (inv.getHasReplacement() == null || !inv.getHasReplacement()))
                .toList();

        if (unpaidInvoices.isEmpty()) {
            return new DebtReminderResponse(0, 0);
        }

        java.util.concurrent.atomic.AtomicInteger sentCount = new java.util.concurrent.atomic.AtomicInteger(0);

        List<CompletableFuture<Void>> futures = unpaidInvoices.stream()
                .filter(inv -> inv.getCustomerId() != null)
                .<CompletableFuture<Void>>map(inv -> CompletableFuture.runAsync(() -> {
                    try {
                        notificationService.sendDebtReminderNotification(
                                inv.getCustomerId(),
                                inv.getMonthInvoiceId(),
                                inv.getYearMonth(),
                                inv.getDigiCode(),
                                inv.getCustomerName(),
                                inv.getAmount(),
                                inv.getAddress());
                        sentCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("Lỗi khi gửi thông báo nhắc nợ cho invoiceId={}: {}", inv.getMonthInvoiceId(), e.getMessage());
                    }
                }, notificationExecutor))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Timeout hoặc lỗi khi gửi thông báo nhắc nợ song song: {}", e.getMessage());
        }

        return new DebtReminderResponse(sentCount.get(), unpaidInvoices.size() - sentCount.get());
    }

    public DebtReminderResponse sendOverdueReminder(String yearMonth, Integer monthInvoiceId) {
        if (yearMonth == null || yearMonth.isBlank()) {
            throw new IllegalArgumentException("yearMonth is required");
        }

        List<vn.hoidanit.springrestwithai.qlkh.invoice.dto.InvoiceInfoDTO> unpaidInvoices = monthInvoiceRepository.findUnpaidInvoiceDTOsByYearMonth(yearMonth.trim());

        if (monthInvoiceId != null) {
            unpaidInvoices = unpaidInvoices.stream()
                .filter(inv -> inv.getMonthInvoiceId().equals(monthInvoiceId))
                .toList();
        }

        if (unpaidInvoices == null || unpaidInvoices.isEmpty()) {
            return new DebtReminderResponse(0, 0);
        }

        unpaidInvoices = unpaidInvoices.stream()
                .filter(inv -> inv.getAmount() > 0 && (inv.getHasReplacement() == null || !inv.getHasReplacement()))
                .toList();

        if (unpaidInvoices.isEmpty()) {
            return new DebtReminderResponse(0, 0);
        }

        java.util.concurrent.atomic.AtomicInteger sentCount = new java.util.concurrent.atomic.AtomicInteger(0);

        List<CompletableFuture<Void>> futures = unpaidInvoices.stream()
                .filter(inv -> inv.getCustomerId() != null)
                .<CompletableFuture<Void>>map(inv -> CompletableFuture.runAsync(() -> {
                    try {
                        notificationService.sendOverdueNotification(
                                inv.getCustomerId(),
                                inv.getMonthInvoiceId(),
                                inv.getYearMonth(),
                                inv.getDigiCode(),
                                inv.getCustomerName(),
                                inv.getAddress());
                        sentCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("Lỗi khi gửi thông báo quá hạn cho invoiceId={}: {}", inv.getMonthInvoiceId(), e.getMessage());
                    }
                }, notificationExecutor))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Timeout hoặc lỗi khi gửi thông báo quá hạn song song: {}", e.getMessage());
        }

        return new DebtReminderResponse(sentCount.get(), unpaidInvoices.size() - sentCount.get());
    }

    /**
     * Gửi thông báo cúp nước cho một hóa đơn cụ thể.
     * Admin truyền kèm tên + SĐT nhân viên thực hiện cúp nước.
     */
    public boolean sendWaterCutoff(Integer monthInvoiceId, String employeeName, String employeePhone) {
        if (monthInvoiceId == null) {
            throw new IllegalArgumentException("monthInvoiceId is required");
        }
        var invoice = monthInvoiceRepository.findById(monthInvoiceId).orElse(null);
        if (invoice == null) return false;

        // Chỉ gửi cúp nước cho hóa đơn chưa thanh toán
        if (invoice.getPaymentStatus() != null && invoice.getPaymentStatus() == 2) {
            return false;
        }

        var customer = customerRepository.findById(invoice.getCustomerId()).orElse(null);
        String digiCode = customer != null ? customer.getDigiCode() : "";

        notificationService.sendWaterCutoffNotification(
                invoice.getCustomerId(),
                invoice.getMonthInvoiceId(),
                digiCode,
                employeeName,
                employeePhone);
        return true;
    }

}


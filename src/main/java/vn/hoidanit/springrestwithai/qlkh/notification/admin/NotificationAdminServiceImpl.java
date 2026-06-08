package vn.hoidanit.springrestwithai.qlkh.notification.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.notification.NotificationRepository;
import vn.hoidanit.springrestwithai.feature.notification.NotificationSpecification;
import vn.hoidanit.springrestwithai.feature.notification.entity.DeliveryStatus;
import vn.hoidanit.springrestwithai.feature.notification.entity.Notification;
import vn.hoidanit.springrestwithai.qlkh.FirebaseService;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationAdminFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationAdminResponse;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.NotificationStatisticsResponse;
import vn.hoidanit.springrestwithai.qlkh.notification.dto.FCMBatchResult;
import vn.hoidanit.springrestwithai.feature.notification.CustomerDeviceRepository;
import vn.hoidanit.springrestwithai.feature.notification.entity.CustomerDevice;

import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationAdminServiceImpl implements NotificationAdminService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final CustomerDeviceRepository customerDeviceRepository;
    private final MonthInvoiceRepository monthInvoiceRepository;
    private final FirebaseService firebaseService;

    public NotificationAdminServiceImpl(NotificationRepository notificationRepository,
                                        CustomerRepository customerRepository,
                                        CustomerDeviceRepository customerDeviceRepository,
                                        MonthInvoiceRepository monthInvoiceRepository,
                                        FirebaseService firebaseService) {
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
        this.customerDeviceRepository = customerDeviceRepository;
        this.monthInvoiceRepository = monthInvoiceRepository;
        this.firebaseService = firebaseService;
    }

    @Override
    public ResultPaginationDTO getNotifications(NotificationAdminFilterRequest filter, Pageable pageable) {
        List<Integer> targetCustomerIds = null;
        if (filter.roadId() != null) {
            targetCustomerIds = monthInvoiceRepository.findDistinctCustomerIdsByRoadId(filter.roadId());
            if (targetCustomerIds.isEmpty()) {
                return new ResultPaginationDTO(
                        new ResultPaginationDTO.Meta(pageable.getPageNumber() + 1, pageable.getPageSize(), 0, 0),
                        Collections.emptyList());
            }
        }

        if (filter.customerDigiCode() != null && !filter.customerDigiCode().isBlank()) {
            List<Customer> customers = customerRepository.searchByNameOrDigiCode(filter.customerDigiCode().trim());
            List<Integer> searchedIds = customers.stream().map(Customer::getCustomerId).toList();
            if (searchedIds.isEmpty()) {
                return new ResultPaginationDTO(
                        new ResultPaginationDTO.Meta(pageable.getPageNumber() + 1, pageable.getPageSize(), 0, 0),
                        Collections.emptyList());
            }
            if (targetCustomerIds == null) {
                targetCustomerIds = searchedIds;
            } else {
                List<Integer> intersection = new ArrayList<>(targetCustomerIds);
                intersection.retainAll(searchedIds);
                if (intersection.isEmpty()) {
                    return new ResultPaginationDTO(
                            new ResultPaginationDTO.Meta(pageable.getPageNumber() + 1, pageable.getPageSize(), 0, 0),
                            Collections.emptyList());
                }
                targetCustomerIds = intersection;
            }
        }

        Page<Notification> page = notificationRepository.findAll(
                NotificationSpecification.withFilters(
                        filter.type(), filter.deliveryStatus(),
                        filter.customerId(), targetCustomerIds,
                        filter.createdFrom(), filter.createdTo()
                ), pageable);

        // Batch resolve customer names
        Set<Integer> customerIds = page.getContent().stream()
                .map(Notification::getCustomerId)
                .collect(Collectors.toSet());
        Map<Integer, String[]> customerMap = resolveCustomerNames(customerIds);

        List<NotificationAdminResponse> responses = page.getContent().stream()
                .map(n -> toResponse(n, customerMap.get(n.getCustomerId())))
                .toList();

        Page<NotificationAdminResponse> mappedPage = new org.springframework.data.domain.PageImpl<>(
                responses, pageable, page.getTotalElements());
        return ResultPaginationDTO.fromPage(mappedPage);
    }

    @Override
    @Transactional(value = "primaryTransactionManager", readOnly = true)
    public NotificationAdminResponse getNotification(Long id) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n == null) return null;
        Map<Integer, String[]> customerMap = resolveCustomerNames(Set.of(n.getCustomerId()));
        return toResponse(n, customerMap.get(n.getCustomerId()));
    }

    @Override
    public NotificationStatisticsResponse getStatistics() {
        long total = notificationRepository.count();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : notificationRepository.countGroupByDeliveryStatus()) {
            if (row[0] == null) continue;          // bỏ qua row có status = NULL
            DeliveryStatus status = (DeliveryStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status.name(), count);
        }

        Map<String, Long> byType = new LinkedHashMap<>();
        for (Object[] row : notificationRepository.countGroupByType()) {
            String type = (String) row[0];
            Long count = (Long) row[1];
            byType.put(type, count);
        }

        long last7 = notificationRepository.countByCreatedAtBetween(now.minusDays(7), now);
        long last30 = notificationRepository.countByCreatedAtBetween(now.minusDays(30), now);

        return new NotificationStatisticsResponse(
                total,
                byStatus.getOrDefault("DELIVERED", 0L),
                byStatus.getOrDefault("FAILED", 0L),
                byStatus.getOrDefault("PENDING", 0L),
                byStatus.getOrDefault("NO_DEVICE", 0L),
                byStatus.getOrDefault("PARTIAL", 0L),
                byType,
                byStatus,
                last7,
                last30
        );
    }

    @Override
    @Transactional("primaryTransactionManager")
    public NotificationAdminResponse resendNotification(Long id) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n == null) return null;

        List<String> tokens = customerDeviceRepository.findByCustomerId(n.getCustomerId())
                .stream().map(CustomerDevice::getDeviceToken).toList();

        if (tokens.isEmpty()) {
            n.setDeliveryStatus(DeliveryStatus.NO_DEVICE);
            notificationRepository.save(n);
        } else {
            Map<String, String> data = new HashMap<>();
            data.put("type", n.getType());
            if (n.getReferenceId() != null) data.put("referenceId", n.getReferenceId().toString());

            FCMBatchResult result = firebaseService.sendToMultipleTokensWithData(tokens, n.getTitle(), n.getContent(), data);
            if (result.successCount() > 0 && result.failureCount() == 0) {
                n.setDeliveryStatus(DeliveryStatus.DELIVERED);
                n.setDeliveredAt(LocalDateTime.now());
                n.setFailureReason(null);
            } else if (result.successCount() > 0) {
                n.setDeliveryStatus(DeliveryStatus.PARTIAL);
                n.setDeliveredAt(LocalDateTime.now());
                n.setFailureReason(null);
            } else {
                n.setDeliveryStatus(DeliveryStatus.FAILED);
                n.setFailureReason("Resend failed: 0 success");
            }
            notificationRepository.save(n);
        }

        Map<Integer, String[]> customerMap = resolveCustomerNames(Set.of(n.getCustomerId()));
        return toResponse(n, customerMap.get(n.getCustomerId()));
    }

    private Map<Integer, String[]> resolveCustomerNames(Set<Integer> customerIds) {
        if (customerIds.isEmpty()) return Map.of();
        Map<Integer, String[]> map = new HashMap<>();
        customerRepository.findAllById(customerIds).forEach(c ->
                map.put(c.getCustomerId(), new String[]{c.getName(), c.getDigiCode()})
        );
        return map;
    }

    private NotificationAdminResponse toResponse(Notification n, String[] customerInfo) {
        return new NotificationAdminResponse(
                n.getId(),
                n.getCustomerId(),
                customerInfo != null ? customerInfo[0] : null,
                customerInfo != null ? customerInfo[1] : null,
                n.getTitle(),
                n.getType(),
                n.getReferenceId(),
                n.getIsRead(),
                n.getDeliveryStatus() != null ? n.getDeliveryStatus().name() : null,
                n.getDeliveredAt(),
                n.getFailureReason(),
                n.getCreatedAt()
        );
    }
}

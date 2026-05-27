package vn.hoidanit.springrestwithai.qlkh.notification.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.feature.notification.CustomerDeviceRepository;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;
import vn.hoidanit.springrestwithai.qlkh.invoice.MonthInvoiceRepository;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.CustomerDeviceStatusFilterRequest;
import vn.hoidanit.springrestwithai.qlkh.notification.admin.dto.CustomerDeviceStatusResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerDeviceAdminServiceImpl implements CustomerDeviceAdminService {

    private final CustomerRepository customerRepository;
    private final CustomerDeviceRepository customerDeviceRepository;
    private final MonthInvoiceRepository monthInvoiceRepository;

    public CustomerDeviceAdminServiceImpl(CustomerRepository customerRepository,
                                          CustomerDeviceRepository customerDeviceRepository,
                                          MonthInvoiceRepository monthInvoiceRepository) {
        this.customerRepository = customerRepository;
        this.customerDeviceRepository = customerDeviceRepository;
        this.monthInvoiceRepository = monthInvoiceRepository;
    }

    @Override
    @Transactional(value = "qlkhTransactionManager", readOnly = true)
    public ResultPaginationDTO getCustomerDeviceStatus(CustomerDeviceStatusFilterRequest filter, Pageable pageable) {
        String status = filter.status();
        String keyword = filter.keyword();

        List<Integer> registeredIds = customerDeviceRepository.findAllRegisteredCustomerIds();

        // Compute includeIds and excludeIds from status + roadId filters
        Set<Integer> includeSet = null;
        Set<Integer> excludeSet = null;

        if ("REGISTERED".equalsIgnoreCase(status)) {
            includeSet = new HashSet<>(registeredIds);
        } else if ("UNREGISTERED".equalsIgnoreCase(status)) {
            excludeSet = new HashSet<>(registeredIds);
        }

        if (filter.roadId() != null) {
            List<Integer> roadCustomerIds = monthInvoiceRepository.findDistinctCustomerIdsByRoadId(filter.roadId());
            Set<Integer> roadSet = new HashSet<>(roadCustomerIds);
            if (includeSet != null) {
                includeSet.retainAll(roadSet);
            } else if (excludeSet != null) {
                // UNREGISTERED + roadId: exclude registered but must be on this road
                includeSet = roadSet;
                includeSet.removeAll(excludeSet);
                excludeSet = null;
            } else {
                includeSet = roadSet;
            }
        }

        // Convert null sets to null lists (JPQL IS NULL check)
        List<Integer> includeIds = includeSet != null ? new ArrayList<>(includeSet) : null;
        List<Integer> excludeIds = excludeSet != null ? new ArrayList<>(excludeSet) : null;

        // Handle empty include set (no results possible)
        if (includeIds != null && includeIds.isEmpty()) {
            Page<CustomerDeviceStatusResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            return ResultPaginationDTO.fromPage(emptyPage);
        }

        Page<Customer> customerPage = customerRepository.findWithFilters(
                keyword, filter.isActive(), includeIds, excludeIds, pageable);

        List<Integer> pageCustomerIds = customerPage.getContent().stream()
                .map(Customer::getCustomerId)
                .toList();

        Map<Integer, DeviceStats> statsMap = buildDeviceStats(pageCustomerIds);

        List<CustomerDeviceStatusResponse> responses = customerPage.getContent().stream()
                .map(c -> toResponse(c, statsMap.get(c.getCustomerId())))
                .toList();

        Page<CustomerDeviceStatusResponse> mappedPage = new PageImpl<>(
                responses, pageable, customerPage.getTotalElements());
        return ResultPaginationDTO.fromPage(mappedPage);
    }

    private Map<Integer, DeviceStats> buildDeviceStats(List<Integer> customerIds) {
        if (customerIds.isEmpty()) return Map.of();

        Map<Integer, DeviceStats> map = new HashMap<>();

        List<Object[]> statsRows = customerDeviceRepository.findDeviceStatsByCustomerIds(customerIds);
        for (Object[] row : statsRows) {
            Integer cid = (Integer) row[0];
            Long count = (Long) row[1];
            LocalDateTime lastAt = (LocalDateTime) row[2];
            map.put(cid, new DeviceStats(count.intValue(), lastAt, new ArrayList<>()));
        }

        List<Object[]> platformRows = customerDeviceRepository.findPlatformsByCustomerIds(customerIds);
        for (Object[] row : platformRows) {
            Integer cid = (Integer) row[0];
            String platform = (String) row[1];
            DeviceStats stats = map.get(cid);
            if (stats != null && platform != null) {
                stats.platforms().add(platform);
            }
        }

        return map;
    }

    private CustomerDeviceStatusResponse toResponse(Customer c, DeviceStats stats) {
        boolean registered = stats != null;
        return new CustomerDeviceStatusResponse(
                c.getCustomerId(),
                c.getDigiCode(),
                c.getName(),
                c.getPhone(),
                c.getEmail(),
                c.getIsActive(),
                registered,
                registered ? stats.deviceCount() : 0,
                registered ? stats.platforms().stream().distinct().collect(Collectors.toList()) : List.of(),
                registered ? stats.lastRegisteredAt() : null
        );
    }

    private record DeviceStats(int deviceCount, LocalDateTime lastRegisteredAt, List<String> platforms) {}
}

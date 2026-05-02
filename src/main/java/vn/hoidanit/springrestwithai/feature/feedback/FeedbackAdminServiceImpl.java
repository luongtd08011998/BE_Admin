package vn.hoidanit.springrestwithai.feature.feedback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.springrestwithai.dto.ResultPaginationDTO;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.feedback.dto.*;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;
import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackReply;
import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackStatus;
import vn.hoidanit.springrestwithai.feature.feedback.entity.IssueType;
import vn.hoidanit.springrestwithai.feature.user.User;
import vn.hoidanit.springrestwithai.feature.user.UserRepository;
import vn.hoidanit.springrestwithai.qlkh.CustomerRepository;
import vn.hoidanit.springrestwithai.qlkh.NotificationService;
import vn.hoidanit.springrestwithai.qlkh.entity.Customer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedbackAdminServiceImpl implements FeedbackAdminService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackAdminServiceImpl.class);

    private final FeedbackRepository feedbackRepository;
    private final FeedbackReplyRepository feedbackReplyRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public FeedbackAdminServiceImpl(FeedbackRepository feedbackRepository,
            FeedbackReplyRepository feedbackReplyRepository,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            NotificationService notificationService) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackReplyRepository = feedbackReplyRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public ResultPaginationDTO getAll(FeedbackFilterRequest filter, Pageable pageable) {
        List<Integer> customerIds = resolveCustomerIds(filter.customerSearch());
        Specification<Feedback> spec = FeedbackSpecification.build(filter, customerIds);
        Page<Feedback> page = feedbackRepository.findAll(spec, pageable);

        List<Integer> pageCustomerIds = page.getContent().stream()
                .map(Feedback::getCustomerId)
                .distinct()
                .toList();
        Map<Integer, Customer> customerMap = batchLoadCustomers(pageCustomerIds);

        Page<FeedbackAdminResponse> responsePage = page.map(f -> {
            Customer customer = customerMap.get(f.getCustomerId());
            FeedbackAdminResponse.CustomerInfo customerInfo = buildCustomerInfo(customer);
            int replyCount = feedbackReplyRepository.findByFeedbackIdOrderByCreatedAtAsc(f.getId()).size();
            return FeedbackAdminResponse.fromEntity(f, customerInfo, replyCount);
        });

        return ResultPaginationDTO.fromPage(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackDetailResponse getById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phản ánh", "id", id));

        Customer customer = findCustomerByDigiCode(feedback.getDigiCode());
        FeedbackDetailResponse.CustomerInfo customerInfo = buildDetailCustomerInfo(customer);

        List<FeedbackReplyResponse> replies = feedbackReplyRepository
                .findByFeedbackIdOrderByCreatedAtAsc(id).stream()
                .map(FeedbackReplyResponse::fromEntity)
                .toList();

        return FeedbackDetailResponse.fromEntity(feedback, customerInfo, replies);
    }

    @Override
    @Transactional
    public FeedbackAdminResponse updateStatus(Long id, UpdateFeedbackStatusRequest request) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phản ánh", "id", id));

        validateStatusTransition(feedback.getStatus(), request.status());

        FeedbackStatus oldStatus = feedback.getStatus();
        feedback.setStatus(request.status());
        feedback = feedbackRepository.save(feedback);

        log.info("Cập nhật trạng thái phản ánh {} từ {} thành {}",
                feedback.getTrackingCode(), oldStatus, request.status());

        // Gửi thông báo Push FCM đến khách hàng
        try {
            notificationService.notifyFeedbackStatusChanged(feedback, request.status());
        } catch (Exception e) {
            log.error("Failed to send feedback status notification for feedbackId={}: {}",
                    feedback.getId(), e.getMessage());
        }

        Customer customer = findCustomerByDigiCode(feedback.getDigiCode());
        FeedbackAdminResponse.CustomerInfo customerInfo = buildCustomerInfo(customer);
        int replyCount = feedbackReplyRepository.findByFeedbackIdOrderByCreatedAtAsc(feedback.getId()).size();
        return FeedbackAdminResponse.fromEntity(feedback, customerInfo, replyCount);
    }

    @Override
    @Transactional
    public FeedbackReplyResponse addReply(Long feedbackId, Long userId, CreateFeedbackReplyRequest request) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Phản ánh", "id", feedbackId));

        User staff = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", "id", userId));

        FeedbackReply reply = new FeedbackReply();
        reply.setFeedback(feedback);
        reply.setUser(staff);
        reply.setContent(request.content());
        reply = feedbackReplyRepository.save(reply);

        log.info("Nhân viên {} thêm phản hồi cho phản ánh {}", staff.getEmail(), feedback.getTrackingCode());

        // Gửi thông báo Push FCM đến khách hàng
        try {
            notificationService.notifyFeedbackReply(feedback, request.content());
        } catch (Exception e) {
            log.error("Failed to send feedback reply notification for feedbackId={}: {}",
                    feedbackId, e.getMessage());
        }

        return FeedbackReplyResponse.fromEntity(reply);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackReplyResponse> getReplies(Long feedbackId) {
        return feedbackReplyRepository.findByFeedbackIdOrderByCreatedAtAsc(feedbackId).stream()
                .map(FeedbackReplyResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackStatisticsResponse getStatistics() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (FeedbackStatus status : FeedbackStatus.values()) {
            byStatus.put(status.name(), feedbackRepository.countByStatus(status));
        }

        Map<String, Long> byIssueType = new LinkedHashMap<>();
        for (IssueType type : IssueType.values()) {
            byIssueType.put(type.name(), feedbackRepository.countByIssueType(type));
        }

        long last7Days = feedbackRepository.countByCreatedAtBetween(
                LocalDateTime.now().minusDays(7), LocalDateTime.now());
        long last30Days = feedbackRepository.countByCreatedAtBetween(
                LocalDateTime.now().minusDays(30), LocalDateTime.now());

        List<Object[]> hotspots = feedbackRepository.findHotspotLocations(10);
        List<FeedbackStatisticsResponse.HotspotLocation> hotspotLocations = hotspots.stream()
                .map(row -> new FeedbackStatisticsResponse.HotspotLocation(
                        (String) row[0], (Long) row[1]))
                .toList();

        return new FeedbackStatisticsResponse(
                byStatus, byIssueType,
                new FeedbackStatisticsResponse.TrendCounts(last7Days, last30Days),
                hotspotLocations);
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    private List<Integer> resolveCustomerIds(String customerSearch) {
        if (customerSearch == null || customerSearch.isBlank()) {
            return null;
        }
        return customerRepository.searchByNameOrDigiCode(customerSearch).stream()
                .map(Customer::getCustomerId)
                .toList();
    }

    private Map<Integer, Customer> batchLoadCustomers(List<Integer> customerIds) {
        if (customerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(Customer::getCustomerId, c -> c, (a, b) -> a));
    }

    private Customer findCustomerByDigiCode(String digiCode) {
        if (digiCode == null) return null;
        return customerRepository.findByDigiCode(digiCode).orElse(null);
    }

    private FeedbackAdminResponse.CustomerInfo buildCustomerInfo(Customer customer) {
        if (customer == null) return null;
        return new FeedbackAdminResponse.CustomerInfo(
                customer.getCustomerId(),
                customer.getDigiCode(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail());
    }

    private FeedbackDetailResponse.CustomerInfo buildDetailCustomerInfo(Customer customer) {
        if (customer == null) return null;
        return new FeedbackDetailResponse.CustomerInfo(
                customer.getCustomerId(),
                customer.getDigiCode(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress());
    }

    private void validateStatusTransition(FeedbackStatus current, FeedbackStatus target) {
        if (current == FeedbackStatus.RESOLVED || current == FeedbackStatus.REJECTED) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái của phản ánh đã đóng");
        }
        if (current == FeedbackStatus.PENDING && target != FeedbackStatus.PROCESSING) {
            throw new IllegalArgumentException("Phản ánh đang chờ xử lý, chỉ có thể chuyển sang PROCESSING");
        }
        if (current == FeedbackStatus.PROCESSING
                && target != FeedbackStatus.RESOLVED && target != FeedbackStatus.REJECTED) {
            throw new IllegalArgumentException("Phản ánh đang xử lý, chỉ có thể chuyển sang RESOLVED hoặc REJECTED");
        }
    }
}

package vn.hoidanit.springrestwithai.qlkh;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hoidanit.springrestwithai.dto.ApiResponse;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.feedback.FeedbackAdminService;
import vn.hoidanit.springrestwithai.feature.feedback.FeedbackRepository;
import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;
import vn.hoidanit.springrestwithai.feature.feedback.dto.FeedbackReplyResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/qlkh/customer/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final FeedbackAdminService feedbackAdminService;
    private final FeedbackRepository feedbackRepository;
    private final JwtDecoder jwtDecoder;
    private final CustomerRepository customerRepository;

    public FeedbackController(FeedbackService feedbackService, FeedbackAdminService feedbackAdminService,
            FeedbackRepository feedbackRepository,
            JwtDecoder jwtDecoder, CustomerRepository customerRepository) {
        this.feedbackService = feedbackService;
        this.feedbackAdminService = feedbackAdminService;
        this.feedbackRepository = feedbackRepository;
        this.jwtDecoder = jwtDecoder;
        this.customerRepository = customerRepository;
    }

    /**
     * API gửi phản ánh dịch vụ.
     * Cần JWT Token để biết khách hàng nào đang gửi.
     * Yêu cầu gửi request dưới dạng multipart/form-data.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> createFeedback(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("issueType") String issueType,
            @RequestParam("location") String location,
            @RequestParam("description") String description,
            @RequestParam(value = "images", required = false) List<MultipartFile> paramImages,
            @RequestParam(value = "image", required = false) List<MultipartFile> paramImage,
            @RequestParam(value = "upload_image", required = false) List<MultipartFile> paramUploadImage) {

        // Hỗ trợ bắt file với nhiều tên khác nhau từ FE (images, image, upload_image)
        List<MultipartFile> images = new java.util.ArrayList<>();
        if (paramImages != null) images.addAll(paramImages);
        if (paramImage != null) images.addAll(paramImage);
        if (paramUploadImage != null) images.addAll(paramUploadImage);

        CustomerInfo customerInfo = extractCustomerInfo(authHeader);

        String trackingCode = feedbackService.createFeedback(
                customerInfo.customerId(),
                customerInfo.digiCode(),
                issueType,
                location,
                description,
                images
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Gửi phản ánh thành công",
                Map.of("trackingCode", trackingCode)
        ));
    }

    /**
     * API lấy danh sách phản ánh của khách hàng đã đăng nhập.
     * GET /api/v1/qlkh/customer/feedbacks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getMyFeedbacks(
            @RequestHeader("Authorization") String authHeader) {

        CustomerInfo customerInfo = extractCustomerInfo(authHeader);

        List<FeedbackResponse> feedbacks = feedbackService.getMyFeedbacks(customerInfo.customerId())
                .stream()
                .map(FeedbackResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách phản ánh thành công", feedbacks));
    }

    /**
     * API lấy chi tiết phản ánh kèm danh sách phản hồi từ staff.
     * GET /api/v1/qlkh/customer/feedbacks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackDetailResponse>> getFeedbackById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        CustomerInfo customerInfo = extractCustomerInfo(authHeader);

        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phản ánh", "id", id));

        if (!feedback.getCustomerId().equals(customerInfo.customerId())) {
            throw new ResourceNotFoundException("Phản ánh", "id", id);
        }

        List<FeedbackReplyResponse> replies = feedbackAdminService.getReplies(id);

        FeedbackDetailResponse response = FeedbackDetailResponse.fromEntity(feedback, replies);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết phản ánh thành công", response));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private CustomerInfo extractCustomerInfo(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        var jwt = jwtDecoder.decode(authHeader.substring(7));
        String digiCode = jwt.getClaimAsString("digiCode");
        
        Integer customerId = customerRepository.findByDigiCode(digiCode)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "digiCode", digiCode))
                .getCustomerId();
                
        return new CustomerInfo(customerId, digiCode);
    }

    private record CustomerInfo(Integer customerId, String digiCode) {}

    /** DTO trả về cho Mobile App */
    public record FeedbackResponse(
            Long id,
            String trackingCode,
            String issueType,
            String location,
            String description,
            String status,
            List<String> images,
            LocalDateTime createdAt
    ) {
        public static FeedbackResponse from(Feedback f) {
            return new FeedbackResponse(
                    f.getId(),
                    f.getTrackingCode(),
                    f.getIssueType().name(),
                    f.getLocation(),
                    f.getDescription(),
                    f.getStatus() != null ? f.getStatus().name() : null,
                    f.getImages(),
                    f.getCreatedAt()
            );
        }
    }

    /** DTO chi tiết phản ánh kèm replies cho Mobile App */
    public record FeedbackDetailResponse(
            Long id,
            String trackingCode,
            String issueType,
            String location,
            String description,
            String status,
            List<String> images,
            List<FeedbackReplyResponse> replies,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static FeedbackDetailResponse fromEntity(Feedback f, List<FeedbackReplyResponse> replies) {
            return new FeedbackDetailResponse(
                    f.getId(),
                    f.getTrackingCode(),
                    f.getIssueType().name(),
                    f.getLocation(),
                    f.getDescription(),
                    f.getStatus() != null ? f.getStatus().name() : null,
                    f.getImages(),
                    replies,
                    f.getCreatedAt(),
                    f.getUpdatedAt()
            );
        }
    }
}


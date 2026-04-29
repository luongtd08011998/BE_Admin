package vn.hoidanit.springrestwithai.feature.feedback.dto;

import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;

import java.time.LocalDateTime;
import java.util.List;

public record FeedbackDetailResponse(
        Long id,
        String trackingCode,
        CustomerInfo customer,
        String issueType,
        String location,
        String description,
        String status,
        List<String> images,
        List<FeedbackReplyResponse> replies,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record CustomerInfo(
            Integer customerId,
            String digiCode,
            String name,
            String phone,
            String email,
            String address
    ) {}

    public static FeedbackDetailResponse fromEntity(Feedback f, CustomerInfo customerInfo, List<FeedbackReplyResponse> replies) {
        return new FeedbackDetailResponse(
                f.getId(),
                f.getTrackingCode(),
                customerInfo,
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

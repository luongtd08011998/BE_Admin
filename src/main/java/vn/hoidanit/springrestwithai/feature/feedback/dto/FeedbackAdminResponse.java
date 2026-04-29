package vn.hoidanit.springrestwithai.feature.feedback.dto;

import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;

import java.time.LocalDateTime;
import java.util.List;

public record FeedbackAdminResponse(
        Long id,
        String trackingCode,
        CustomerInfo customer,
        String issueType,
        String location,
        String description,
        String status,
        int replyCount,
        List<String> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record CustomerInfo(
            Integer customerId,
            String digiCode,
            String name,
            String phone,
            String email
    ) {}

    public static FeedbackAdminResponse fromEntity(Feedback f, CustomerInfo customerInfo, int replyCount) {
        return new FeedbackAdminResponse(
                f.getId(),
                f.getTrackingCode(),
                customerInfo,
                f.getIssueType().name(),
                f.getLocation(),
                f.getDescription(),
                f.getStatus() != null ? f.getStatus().name() : null,
                replyCount,
                f.getImages(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}

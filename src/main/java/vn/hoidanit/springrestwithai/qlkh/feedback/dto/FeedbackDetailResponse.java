package vn.hoidanit.springrestwithai.qlkh.feedback.dto;

import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;
import vn.hoidanit.springrestwithai.feature.feedback.dto.FeedbackReplyResponse;

import java.time.LocalDateTime;
import java.util.List;

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

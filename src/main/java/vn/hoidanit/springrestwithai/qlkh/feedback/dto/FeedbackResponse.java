package vn.hoidanit.springrestwithai.qlkh.feedback.dto;

import vn.hoidanit.springrestwithai.feature.feedback.entity.Feedback;

import java.time.LocalDateTime;
import java.util.List;

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

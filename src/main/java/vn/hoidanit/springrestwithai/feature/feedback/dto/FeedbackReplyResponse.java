package vn.hoidanit.springrestwithai.feature.feedback.dto;

import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackReply;

import java.time.LocalDateTime;

public record FeedbackReplyResponse(
        Long id,
        StaffInfo staff,
        String content,
        LocalDateTime createdAt
) {
    public record StaffInfo(
            Long id,
            String name,
            String email,
            String avatar
    ) {}

    public static FeedbackReplyResponse fromEntity(FeedbackReply reply) {
        StaffInfo staffInfo = reply.getUser() != null
                ? new StaffInfo(
                        reply.getUser().getId(),
                        reply.getUser().getName(),
                        reply.getUser().getEmail(),
                        reply.getUser().getAvatar())
                : null;

        return new FeedbackReplyResponse(
                reply.getId(),
                staffInfo,
                reply.getContent(),
                reply.getCreatedAt()
        );
    }
}

package vn.hoidanit.springrestwithai.feature.feedback.dto;

import jakarta.validation.constraints.NotNull;
import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackStatus;

public record UpdateFeedbackStatusRequest(
        @NotNull(message = "Trạng thái không được để trống")
        FeedbackStatus status
) {}

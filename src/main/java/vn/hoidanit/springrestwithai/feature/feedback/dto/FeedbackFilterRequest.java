package vn.hoidanit.springrestwithai.feature.feedback.dto;

import vn.hoidanit.springrestwithai.feature.feedback.entity.FeedbackStatus;
import vn.hoidanit.springrestwithai.feature.feedback.entity.IssueType;

import java.time.LocalDateTime;

public record FeedbackFilterRequest(
        String keyword,
        FeedbackStatus status,
        IssueType issueType,
        String customerSearch,
        LocalDateTime createdFrom,
        LocalDateTime createdTo
) {}

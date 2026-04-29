package vn.hoidanit.springrestwithai.feature.feedback.entity;

public enum FeedbackStatus {
    PENDING,
    PROCESSING,
    RESOLVED,
    REJECTED;

    public static FeedbackStatus fromString(String text) {
        if (text != null) {
            for (FeedbackStatus status : FeedbackStatus.values()) {
                if (text.equalsIgnoreCase(status.name())) {
                    return status;
                }
            }
        }
        return null;
    }
}

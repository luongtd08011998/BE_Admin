package vn.hoidanit.springrestwithai.feature.feedback.entity;

public enum IssueType {
    LEAK,
    QUALITY,
    PRESSURE,
    OUTAGE,
    BILLING,
    METER,
    OTHER;

    public static IssueType fromString(String text) {
        if (text != null) {
            for (IssueType type : IssueType.values()) {
                if (text.equalsIgnoreCase(type.name())) {
                    return type;
                }
            }
        }
        return null;
    }
}

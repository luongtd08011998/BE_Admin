package vn.hoidanit.springrestwithai.feature.feedback.dto;

import java.util.List;
import java.util.Map;

public record FeedbackStatisticsResponse(
        Map<String, Long> byStatus,
        Map<String, Long> byIssueType,
        TrendCounts trendCounts,
        List<HotspotLocation> hotspotLocations
) {
    public record TrendCounts(
            long last7Days,
            long last30Days
    ) {}

    public record HotspotLocation(
            String location,
            long count
    ) {}
}

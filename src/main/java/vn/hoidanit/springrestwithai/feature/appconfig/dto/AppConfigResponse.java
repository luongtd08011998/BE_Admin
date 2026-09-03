package vn.hoidanit.springrestwithai.feature.appconfig.dto;

import vn.hoidanit.springrestwithai.feature.appconfig.AppConfig;

import java.time.Instant;

public record AppConfigResponse(
        Long id,
        String platform,
        String latestVersion,
        Integer latestVersionCode,
        String minRequiredVersion,
        Integer minRequiredVersionCode,
        String storeUrl,
        String releaseNotes,
        Integer active,
        Instant createdAt,
        Instant updatedAt
) {
    public static AppConfigResponse fromEntity(AppConfig config) {
        return new AppConfigResponse(
                config.getId(),
                config.getPlatform(),
                config.getLatestVersion(),
                config.getLatestVersionCode(),
                config.getMinRequiredVersion(),
                config.getMinRequiredVersionCode(),
                config.getStoreUrl(),
                config.getReleaseNotes(),
                config.getActive(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}

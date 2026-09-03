package vn.hoidanit.springrestwithai.feature.appconfig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateAppConfigRequest(
        @NotNull(message = "ID is required")
        Long id,
        @NotBlank(message = "Platform is required")
        @Pattern(regexp = "^(IOS|ANDROID)$", message = "Platform must be either IOS or ANDROID")
        String platform,
        String latestVersion,
        Integer latestVersionCode,
        String minRequiredVersion,
        Integer minRequiredVersionCode,
        String storeUrl,
        String releaseNotes,
        @NotNull(message = "Active status is required")
        Byte active
) {}

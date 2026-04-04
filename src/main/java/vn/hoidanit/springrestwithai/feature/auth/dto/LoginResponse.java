package vn.hoidanit.springrestwithai.feature.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}

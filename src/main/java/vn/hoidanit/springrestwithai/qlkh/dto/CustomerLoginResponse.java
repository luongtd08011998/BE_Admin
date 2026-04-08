package vn.hoidanit.springrestwithai.qlkh.dto;

/**
 * Thông tin khách hàng sau đăng nhập — khớp các trường public trong databaseqlkh.md.
 */
public record CustomerLoginResponse(
        String digiCode,
        String name,
        String address,
        String phone,
        String email,
        String sms,
        String taxCode,
        Short isActive,
        Short isWaterCut) {
}

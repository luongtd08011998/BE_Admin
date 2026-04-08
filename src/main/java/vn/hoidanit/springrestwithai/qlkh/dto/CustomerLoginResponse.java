package vn.hoidanit.springrestwithai.qlkh.dto;

public record CustomerLoginResponse(
        Integer customerId,
        String code,
        String digiCode,
        String name,
        String shortName,
        String phone,
        String address,
        String email,
        String contactName,
        String contactPhone,
        Double balance,
        Integer status,
        String accessToken
) {}

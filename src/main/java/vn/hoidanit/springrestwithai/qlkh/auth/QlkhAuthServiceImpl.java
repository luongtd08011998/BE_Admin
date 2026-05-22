package vn.hoidanit.springrestwithai.qlkh.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import vn.hoidanit.springrestwithai.exception.InvalidTokenException;
import vn.hoidanit.springrestwithai.exception.ResourceNotFoundException;
import vn.hoidanit.springrestwithai.feature.auth.CustomerRefreshToken;
import vn.hoidanit.springrestwithai.feature.auth.CustomerRefreshTokenRepository;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.CustomerLoginRequest;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.CustomerLoginResponse;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.RefreshTokenRequest;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.TokenResponse;
import vn.hoidanit.springrestwithai.qlkh.customer.Customer;
import vn.hoidanit.springrestwithai.qlkh.customer.CustomerRepository;

@Service
public class QlkhAuthServiceImpl implements QlkhAuthService {

    private final CustomerRepository customerRepository;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final CustomerRefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.qlkh-refresh-token-expiration}")
    private long qlkhRefreshTokenExpiration;

    public QlkhAuthServiceImpl(CustomerRepository customerRepository,
                               JwtEncoder jwtEncoder,
                               JwtDecoder jwtDecoder,
                               CustomerRefreshTokenRepository refreshTokenRepository) {
        this.customerRepository = customerRepository;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public TokenResponse login(CustomerLoginRequest request) {
        Customer customer = customerRepository
                .findByDigiCodeAndPhone(request.digiCode(), request.phone())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "mã KH/SĐT",
                        request.digiCode()));
        String accessToken = generateToken(customer);
        String refreshToken = createAndSaveRefreshToken(customer);
        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional("primaryTransactionManager")
    public TokenResponse refresh(RefreshTokenRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new InvalidTokenException("refreshToken không được để trống");
        }
        CustomerRefreshToken stored = refreshTokenRepository
                .findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh Token không hợp lệ"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new InvalidTokenException("Refresh Token đã hết hạn, vui lòng đăng nhập lại");
        }
        Customer customer = customerRepository.findByDigiCode(stored.getDigiCode())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "digiCode", stored.getDigiCode()));
        String newAccessToken = generateToken(customer);
        return new TokenResponse(newAccessToken, request.refreshToken());
    }

    @Override
    @Transactional("primaryTransactionManager")
    public void logout(String authHeader) {
        Customer customer = getCustomerFromToken(authHeader);
        refreshTokenRepository.deleteByCustomerId(customer.getCustomerId());
    }

    @Override
    public CustomerLoginResponse getCustomer(String authHeader) {
        Customer customer = getCustomerFromToken(authHeader);
        return toCustomerResponse(customer);
    }

    private String generateToken(Customer customer) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(customer.getDigiCode())
                .issuedAt(now)
                .expiresAt(now.plusMillis(accessTokenExpiration))
                .claim("customerId", customer.getCustomerId())
                .claim("digiCode", customer.getDigiCode())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String createAndSaveRefreshToken(Customer customer) {
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        CustomerRefreshToken entity = new CustomerRefreshToken(
                token,
                customer.getCustomerId(),
                customer.getDigiCode(),
                now.plusMillis(qlkhRefreshTokenExpiration),
                now);
        refreshTokenRepository.save(entity);
        return token;
    }

    private Customer getCustomerFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        Jwt jwt = jwtDecoder.decode(token);
        String digiCode = jwt.getClaimAsString("digiCode");
        return customerRepository.findByDigiCode(digiCode)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "digiCode", digiCode));
    }

    private CustomerLoginResponse toCustomerResponse(Customer c) {
        return new CustomerLoginResponse(
                c.getDigiCode(),
                c.getName(),
                c.getAddress(),
                c.getPhone(),
                c.getEmail(),
                c.getSms(),
                c.getTaxCode(),
                c.getIsActive(),
                c.getIsWaterCut());
    }
}

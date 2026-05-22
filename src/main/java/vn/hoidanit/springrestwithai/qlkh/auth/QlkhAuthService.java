package vn.hoidanit.springrestwithai.qlkh.auth;

import vn.hoidanit.springrestwithai.qlkh.auth.dto.CustomerLoginRequest;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.CustomerLoginResponse;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.RefreshTokenRequest;
import vn.hoidanit.springrestwithai.qlkh.auth.dto.TokenResponse;

public interface QlkhAuthService {

    TokenResponse login(CustomerLoginRequest request);

    TokenResponse refresh(RefreshTokenRequest request);

    void logout(String authHeader);

    CustomerLoginResponse getCustomer(String authHeader);
}

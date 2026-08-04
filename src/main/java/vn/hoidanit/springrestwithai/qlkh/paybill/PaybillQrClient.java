package vn.hoidanit.springrestwithai.qlkh.paybill;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import vn.hoidanit.springrestwithai.qlkh.paybill.dto.PaybillQrRequest;
import vn.hoidanit.springrestwithai.qlkh.paybill.dto.PaybillQrResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class PaybillQrClient {

    private static final Logger log = LoggerFactory.getLogger(PaybillQrClient.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private final RestTemplate restTemplate;
    private final PaybillProperties props;

    public PaybillQrClient(@Qualifier("paybillRestTemplate") RestTemplate restTemplate,
                           PaybillProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    public PaybillQrResponse createQrCode(PaybillQrRequest request) {
        String url = props.getBaseUrl() + "/CreateQRCode";

        try {
            // Serialize request thủ công
            String requestBody = mapper.writeValueAsString(request);
            log.info("[Paybill] Request Body: {}", requestBody);

            // Dùng execute() với ResponseExtractor để đọc raw bytes, bypass toàn bộ converter
            String rawBody = restTemplate.execute(
                URI.create(url),
                HttpMethod.POST,
                (ClientHttpRequest clientReq) -> {
                    clientReq.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    clientReq.getBody().write(requestBody.getBytes(StandardCharsets.UTF_8));
                },
                (ClientHttpResponse clientResp) -> {
                    byte[] bytes = StreamUtils.copyToByteArray(clientResp.getBody());
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            );

            log.info("[Paybill] Raw Response Body: {}", rawBody);

            if (rawBody == null || rawBody.isBlank()) {
                throw new RuntimeException("VietUnion trả về response body rỗng");
            }

            return mapper.readValue(rawBody, PaybillQrResponse.class);

        } catch (Exception e) {
            log.error("[Paybill] Lỗi gọi API: {}", e.getMessage(), e);
            throw new RuntimeException("Error calling Paybill CreateQRCode API: " + e.getMessage(), e);
        }
    }
}

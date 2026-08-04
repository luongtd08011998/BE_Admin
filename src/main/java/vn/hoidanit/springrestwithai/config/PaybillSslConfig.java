package vn.hoidanit.springrestwithai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import vn.hoidanit.springrestwithai.qlkh.paybill.PaybillProperties;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

@Configuration
public class PaybillSslConfig {

    private final PaybillProperties props;

    public PaybillSslConfig(PaybillProperties props) {
        this.props = props;
    }

    @Bean(name = "paybillRestTemplate")
    public RestTemplate paybillRestTemplate() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HostnameVerifier allHostsValid = (hostname, session) -> true;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected HttpURLConnection openConnection(URL url, Proxy proxy) throws IOException {
                HttpURLConnection connection = super.openConnection(url, proxy);
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
                    ((HttpsURLConnection) connection).setHostnameVerifier(allHostsValid);
                }
                return connection;
            }
        };
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);

        return new RestTemplate(factory);
    }
}

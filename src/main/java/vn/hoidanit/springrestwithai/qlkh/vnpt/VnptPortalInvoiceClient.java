package vn.hoidanit.springrestwithai.qlkh.vnpt;

import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.xml.sax.InputSource;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Gọi VNPT PortalService (SOAP 1.1).
 *
 * Hiện dùng operation {@code downloadInvZipFkey} (có {@code checkPayment=false}) theo yêu cầu tích hợp.
 */
@Component
public class VnptPortalInvoiceClient {

    private static final Logger log = LoggerFactory.getLogger(VnptPortalInvoiceClient.class);
    private static final String TEMPURI = "http://tempuri.org/";
    private static final String SOAP_ACTION_DOWNLOAD_INV_ZIP_FKEY = "\"http://tempuri.org/downloadInvZipFkey\"";
    private static final String SOAP_ACTION_GET_INV_VIEW_FKEY = "\"http://tempuri.org/getInvViewFkey\"";
    private static final String SOAP_ACTION_GET_INV_VIEW_FKEY_NO_PAY = "\"http://tempuri.org/getInvViewFkeyNoPay\"";
    private static final String SOAP_ACTION_LIST_INV_BY_CUS = "\"http://tempuri.org/listInvByCus\"";
    private static final String ERR_NOT_PAID = "ERR:11";

    private final VnptPortalProperties properties;
    private final RestClient restClient;

    public record VnptDebugResult(
            String serviceUrl,
            String usedUsernameMasked,
            String fkeyMasked,
            String resultPrefix) {
    }

    public VnptPortalInvoiceClient(VnptPortalProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    /**
     * @return nội dung chuỗi trả về từ VNPT (zip base64 hoặc tiền tố {@code ERR:})
     */
    public String downloadInvZipFkey(String fkey, boolean checkPayment) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa cấu hình vnpt.portal (service-url, username, password).");
        }
        return callDownloadInvZipFkey(fkey, checkPayment, properties.getUsername(), properties.getPassword(), "primary");
    }

    public String getInvView(String fkey) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa cấu hình vnpt.portal (service-url, username, password).");
        }
        String result = callGetInvViewFkey(fkey, properties.getUsername(), properties.getPassword());
        if (ERR_NOT_PAID.equals(result.trim())) {
            log.info("VNPT: hóa đơn chưa thanh toán (ERR:11), fallback sang getInvViewFkeyNoPay");
            result = callGetInvViewFkeyNoPay(fkey, properties.getUsername(), properties.getPassword());
        }
        return result;
    }

    public VnptDebugResult debugGetInvView(String fkey) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa cấu hình vnpt.portal (service-url, username, password).");
        }
        String fkeyMasked = maskFkey(fkey);
        String url = properties.getServiceUrl();
        String r1 = callGetInvViewFkey(fkey, properties.getUsername(), properties.getPassword());
        return new VnptDebugResult(url, maskUser(properties.getUsername()), fkeyMasked, resultPrefix(r1));
    }

    public String listInvByCus(String cusCode, String fromDate, String toDate) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa cấu hình vnpt.portal (service-url, username, password).");
        }
        return callListInvByCus(cusCode, fromDate, toDate,
                properties.getUsername(), properties.getPassword());
    }

    public VnptDebugResult debugDownloadInvZipFkey(String fkey, boolean checkPayment) {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa cấu hình vnpt.portal (service-url, username, password).");
        }
        String fkeyMasked = maskFkey(fkey);
        String url = properties.getServiceUrl();

        String r1 = callDownloadInvZipFkey(fkey, checkPayment, properties.getUsername(), properties.getPassword(), "primary");
        return new VnptDebugResult(url, maskUser(properties.getUsername()), fkeyMasked, resultPrefix(r1));
    }

    private String callDownloadInvZipFkey(
            String fkey,
            boolean checkPayment,
            String userName,
            String userPass,
            String label) {
        log.info("VNPT call {}: op=downloadInvZipFkey, url={}, user={}, fkey={}, checkPayment={}",
                label,
                properties.getServiceUrl(),
                maskUser(userName),
                maskFkey(fkey),
                checkPayment);
        String soap = buildSoap11RequestDownloadInvZipFkey(
                xmlEscape(fkey),
                xmlEscape(userName),
                xmlEscape(userPass),
                checkPayment);
        String responseXml;
        try {
            responseXml = restClient
                    .post()
                    .uri(properties.getServiceUrl())
                    .contentType(MediaType.parseMediaType("text/xml; charset=utf-8"))
                    .header("SOAPAction", SOAP_ACTION_DOWNLOAD_INV_ZIP_FKEY)
                    .body(soap)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            log.warn("VNPT PortalService gọi thất bại: {}", ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Không kết nối được máy chủ hóa đơn điện tử VNPT.",
                    ex);
        }
        if (responseXml == null || responseXml.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "VNPT trả về rỗng.");
        }
        return extractResultByLocalName(responseXml, "downloadInvZipFkeyResult");
    }

    private String callGetInvViewFkey(String fkey, String userName, String userPass) {
        log.info("VNPT call: op=getInvViewFkey, url={}, user={}, fkey={}",
                properties.getServiceUrl(), maskUser(userName), maskFkey(fkey));
        String soap = buildSoap11RequestGetInvViewFkey(xmlEscape(fkey), xmlEscape(userName), xmlEscape(userPass));
        String responseXml = callSoap(SOAP_ACTION_GET_INV_VIEW_FKEY, soap);
        return extractResultByLocalName(responseXml, "getInvViewFkeyResult");
    }

    private String callGetInvViewFkeyNoPay(String fkey, String userName, String userPass) {
        log.info("VNPT call: op=getInvViewFkeyNoPay, url={}, user={}, fkey={}",
                properties.getServiceUrl(), maskUser(userName), maskFkey(fkey));
        String soap = buildSoap11RequestGetInvViewFkeyNoPay(xmlEscape(fkey), xmlEscape(userName), xmlEscape(userPass));
        String responseXml = callSoap(SOAP_ACTION_GET_INV_VIEW_FKEY_NO_PAY, soap);
        return extractResultByLocalName(responseXml, "getInvViewFkeyNoPayResult");
    }

    private String callListInvByCus(String cusCode, String fromDate, String toDate,
                                    String userName, String userPass) {
        log.info("VNPT call: op=listInvByCus, url={}, user={}, cusCode={}, fromDate={}, toDate={}",
                properties.getServiceUrl(), maskUser(userName), cusCode, fromDate, toDate);
        String soap = buildSoap11RequestListInvByCus(
                xmlEscape(cusCode),
                fromDate != null ? xmlEscape(fromDate) : "",
                toDate != null ? xmlEscape(toDate) : "",
                xmlEscape(userName),
                xmlEscape(userPass));
        String responseXml = callSoap(SOAP_ACTION_LIST_INV_BY_CUS, soap);
        return extractResultByLocalName(responseXml, "listInvByCusResult");
    }

    private String callSoap(String soapAction, String soapBody) {
        try {
            String responseXml = restClient
                    .post()
                    .uri(properties.getServiceUrl())
                    .contentType(MediaType.parseMediaType("text/xml; charset=utf-8"))
                    .header("SOAPAction", soapAction)
                    .body(soapBody)
                    .retrieve()
                    .body(String.class);
            if (responseXml == null || responseXml.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "VNPT trả về rỗng.");
            }
            return responseXml;
        } catch (RestClientException ex) {
            log.warn("VNPT PortalService gọi thất bại: {}", ex.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Không kết nối được máy chủ hóa đơn điện tử VNPT.", ex);
        }
    }

    private static String resultPrefix(String result) {
        if (result == null) {
            return "null";
        }
        String t = result.trim();
        if (t.isEmpty()) {
            return "empty";
        }
        if (t.startsWith("ERR:")) {
            int end = Math.min(t.length(), 16);
            return t.substring(0, end);
        }
        // Thành công thường là XML invoice; không trả toàn bộ trong debug.
        return "OK";
    }

    private static String maskFkey(String fkey) {
        if (fkey == null) {
            return "null";
        }
        String t = fkey.trim();
        if (t.isEmpty()) {
            return "empty";
        }
        if (t.length() <= 8) {
            return "****" + t;
        }
        return t.substring(0, 4) + "****" + t.substring(t.length() - 4);
    }

    private static String maskUser(String user) {
        if (user == null) {
            return "null";
        }
        String t = user.trim();
        if (t.isEmpty()) {
            return "empty";
        }
        if (t.length() <= 4) {
            return t.charAt(0) + "***";
        }
        return t.substring(0, 2) + "***" + t.substring(t.length() - 2);
    }

    private static String buildSoap11RequestDownloadInvZipFkey(
            String fkey,
            String userName,
            String userPass,
            boolean checkPayment) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                <downloadInvZipFkey xmlns="http://tempuri.org/">
                <fkey>%s</fkey>
                <userName>%s</userName>
                <userPass>%s</userPass>
                <checkPayment>%s</checkPayment>
                </downloadInvZipFkey>
                </soap:Body>
                </soap:Envelope>
                """
                .formatted(fkey, userName, userPass, checkPayment);
    }

    private static String buildSoap11RequestGetInvViewFkey(String fkey, String userName, String userPass) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                <getInvViewFkey xmlns="http://tempuri.org/">
                <fkey>%s</fkey>
                <userName>%s</userName>
                <userPass>%s</userPass>
                </getInvViewFkey>
                </soap:Body>
                </soap:Envelope>
                """
                .formatted(fkey, userName, userPass);
    }

    private static String buildSoap11RequestGetInvViewFkeyNoPay(String fkey, String userName, String userPass) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                <getInvViewFkeyNoPay xmlns="http://tempuri.org/">
                <fkey>%s</fkey>
                <userName>%s</userName>
                <userPass>%s</userPass>
                </getInvViewFkeyNoPay>
                </soap:Body>
                </soap:Envelope>
                """
                .formatted(fkey, userName, userPass);
    }

    private static String buildSoap11RequestListInvByCus(String cusCode, String fromDate,
                                                         String toDate, String userName, String userPass) {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                <soap:Body>
                <listInvByCus xmlns="http://tempuri.org/">
                <cusCode>%s</cusCode>
                <fromDate>%s</fromDate>
                <toDate>%s</toDate>
                <userName>%s</userName>
                <userPass>%s</userPass>
                </listInvByCus>
                </soap:Body>
                </soap:Envelope>
                """
                .formatted(cusCode, fromDate, toDate, userName, userPass);
    }

    private static String xmlEscape(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String extractResultByLocalName(String soapXml, String localName) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Document doc = dbf.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(soapXml)));
            // VNPT có thể trả SOAP 1.1/1.2 và namespace khác; dùng XPath theo local-name để chắc chắn bắt được.
            XPath xpath = XPathFactory.newInstance().newXPath();
            String result = (String) xpath.evaluate(
                    "string(//*[local-name()='" + localName + "'])",
                    doc,
                    XPathConstants.STRING);
            if (result != null && !result.isBlank()) {
                return result;
            }

            // Fallback: thử theo namespace tempuri (tài liệu mẫu).
            NodeList ns = doc.getElementsByTagNameNS(TEMPURI, localName);
            if (ns.getLength() > 0) {
                return ns.item(0).getTextContent();
            }
            NodeList legacy = doc.getElementsByTagName(localName);
            if (legacy.getLength() > 0) {
                return legacy.item(0).getTextContent();
            }
        } catch (Exception e) {
            log.warn("Không parse được SOAP VNPT: {}", e.getMessage());
        }
        String head = soapXml == null ? "" : soapXml.strip();
        String preview = head.length() > 500 ? head.substring(0, 500) + "..." : head;
        log.warn("SOAP VNPT không đúng định dạng mong đợi. Preview: {}", preview);
        throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Phản hồi VNPT không đúng định dạng SOAP mong đợi.");
    }
}

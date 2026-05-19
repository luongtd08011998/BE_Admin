package vn.hoidanit.springrestwithai.qlkh.qrpayment;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class VietQrService {

    private final VietQrProperties props;
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public VietQrService(VietQrProperties props) {
        this.props = props;
    }

    /**
     * Build VietQR URL. Trả null nếu totalAmount <= 0.
     */
    public String buildQrUrl(String digiCode, String yearMonth, double totalAmount) {
        if (totalAmount <= 0) return null;
        long amount = Math.round(totalAmount);
        
        String addInfo = generateAddInfo(digiCode, yearMonth);
        String base = String.format("%s/%s-%s-%s.png",
            props.getBaseUrl(), props.getBankId(),
            props.getAccountNo(), props.getTemplate());
        
        return base
            + "?amount=" + amount
            + "&addInfo=" + percentEncode(addInfo)
            + "&accountName=" + percentEncode(props.getAccountName());
    }

    /**
     * Generate standard addInfo string.
     */
    public String generateAddInfo(String digiCode, String yearMonth) {
        return (props.getAddInfoPrefix()
            + " " + nullToEmpty(digiCode)
            + " " + nullToEmpty(yearMonth)).trim();
    }

    /** Overload: tính tổng từ 3 thành phần. */
    public String buildQrUrl(String digiCode, String yearMonth,
                             Double amount, Double envFee, Double taxFee) {
        return buildQrUrl(digiCode, yearMonth,
            coalesce(amount) + coalesce(envFee) + coalesce(taxFee));
    }

    public String getAddInfoPrefix() {
        return props.getAddInfoPrefix();
    }

    // RFC 3986 percent-encoding — port 1:1 từ Kotlin mobile app
    private String percentEncode(String input) {
        if (input == null || input.isEmpty()) return "";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            int u = b & 0xFF;
            if ((u >= 0x41 && u <= 0x5A) || (u >= 0x61 && u <= 0x7A)
                || (u >= 0x30 && u <= 0x39)
                || u == 0x2D || u == 0x5F || u == 0x2E || u == 0x7E) {
                sb.append((char) u);
            } else {
                sb.append('%').append(HEX[u >> 4]).append(HEX[u & 0x0F]);
            }
        }
        return sb.toString();
    }

    private static String nullToEmpty(String s) {
        return s != null ? s.trim() : "";
    }

    private static double coalesce(Double v) {
        return v != null ? v : 0d;
    }
}
